package com.github.codepawfect.animalwelfareservicespringboot.domain.service;

import com.github.codepawfect.animalwelfareservicespringboot.core.service.BlobStorageService;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.DogImageRepository;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.DogRepository;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model.DogEntity;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model.DogImageEntity;
import com.github.codepawfect.animalwelfareservicespringboot.domain.service.mapper.DogMapper;
import com.github.codepawfect.animalwelfareservicespringboot.domain.service.model.Dog;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.retry.Retry;

/** Service class for managing dogs and their images. */
@Slf4j
@RequiredArgsConstructor
@Service
public class DogService {

  private final DogRepository dogRepository;
  private final DogImageRepository dogImageRepository;
  private final DogMapper dogMapper;
  private final BlobStorageService blobStorageService;

  @Value("${spring.cloud.azure.storage.blob.container-name}")
  private String containerName;

  private static final Set<String> SUPPORTED_IMAGE_CONTENT_TYPES =
      Set.of(MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE);

  /**
   * Retrieve a list of all dogs with their associated images.
   *
   * @return A Flux of Dog objects with image URIs.
   */
  @Cacheable(value = "dogs")
  public Flux<Dog> getDogs() {
    return dogRepository.findAll().flatMap(findImagesAssociatedWithDogEntity());
  }

  private Function<DogEntity, Mono<Dog>> findImagesAssociatedWithDogEntity() {
    return dogEntity ->
        dogImageRepository
            .findAllByDogId(dogEntity.getId())
            .collectList()
            .map(
                dogImageEntities -> {
                  Dog dog = dogMapper.mapToModel(dogEntity);
                  dog.setImageUris(dogImageEntities.stream().map(DogImageEntity::getUri).toList());
                  return dog;}
            ).retryWhen(Retry.backoff(3, Duration.ofMillis(1000)))
                .onErrorMap(throwable -> this.handleError(throwable, "Unable to retrieve dogs at this time: %s"));
  }

  private Throwable handleError(Throwable throwable, String message) {
    log.error(message.formatted(throwable));

    return new ResponseStatusException(
            HttpStatus.SERVICE_UNAVAILABLE, message.formatted(throwable)
    );
  }

  /**
   * Retrieve a specific dog by its ID along with its associated images.
   *
   * @param id The UUID of the dog to retrieve.
   * @return A Mono containing the Dog object with image URIs, or empty if not found.
   */
  public Mono<Dog> getDog(String id) {
    return dogRepository.findById(UUID.fromString(id))
            .flatMap(findImagesAssociatedWithDogEntity())
            .retryWhen(Retry.backoff(3, Duration.ofMillis(1000)))
            .onErrorMap(throwable -> this.handleError(throwable, "Unable to retrieve dog at this time: %s"));
  }

  /**
   * Add a new dog along with its images.
   *
   * @param dog The Dog object to be added.
   * @param filePartFlux A Flux of FilePart objects representing image files.
   * @return A Mono containing the added Dog object with image URIs.
   */
  @CacheEvict("dogs")
  public Mono<Dog> addDog(Dog dog, Flux<FilePart> filePartFlux) {
    var dogEntity = dogMapper.mapToEntity(dog);
    dogEntity.setId(UUID.randomUUID());

    return filePartFlux
        .filter(this::isSupportedImage)
        .switchIfEmpty(
            Mono.error(
                new IllegalArgumentException(
                    "No supported image files found in request. Supported image types are: "
                        + SUPPORTED_IMAGE_CONTENT_TYPES)))
        .flatMap(this::saveToBlobStorage)
        .onErrorResume(
            e -> {
              log.error("Failed to upload file to blob storage", e);
              return Mono.empty();
            })
        .collectList()
        .flatMap(saveDogAndAssociatedImages(dogEntity))
        .flatMap(mapToModelAndSetImageUris());
  }

  private Function<Tuple2<DogEntity, List<DogImageEntity>>, Mono<? extends Dog>>
      mapToModelAndSetImageUris() {
    return tuple -> {
      DogEntity savedDogEntity = tuple.getT1();
      List<DogImageEntity> savedImageUris = tuple.getT2();

      Dog dogModel = dogMapper.mapToModel(savedDogEntity);
      dogModel.setImageUris(savedImageUris.stream().map(DogImageEntity::getUri).toList());

      return Mono.just(dogModel);
    };
  }

  private Function<List<String>, Mono<? extends Tuple2<DogEntity, List<DogImageEntity>>>>
      saveDogAndAssociatedImages(DogEntity dogEntity) {
    return blobUrls -> {
      List<Mono<DogImageEntity>> dogImageEntities =
          blobUrls.stream().map(blobUrl -> saveDogImage(dogEntity.getId(), blobUrl)).toList();

      return Mono.zip(dogRepository.save(dogEntity), Flux.merge(dogImageEntities).collectList());
    };
  }

  /**
   * Delete a dog and its associated images by ID.
   *
   * @param id The UUID of the dog to be deleted.
   * @return A Mono indicating the completion of the deletion operation.
   */
  @Transactional
  @CacheEvict("dogs")
  public Mono<Void> deleteDog(String id) {
    return dogRepository
        .findById(UUID.fromString(id))
        .switchIfEmpty(Mono.error(new IllegalStateException("Dog not found with ID: " + id)))
        .flatMap(deleteDogAndAssociatedImages());
  }

  private Function<DogEntity, Mono<? extends Void>> deleteDogAndAssociatedImages() {
    return dogEntity -> {
      Flux<String> uris =
          dogImageRepository.findAllByDogId(dogEntity.getId()).map(DogImageEntity::getUri);
      return dogImageRepository
          .deleteById(dogEntity.getId())
          .then(blobStorageService.deleteBlobs(this.containerName, extractBlobNames(uris)))
          .then(dogRepository.deleteById(dogEntity.getId()));
    };
  }

  /**
   * Deletes a dog image from the repository and associated blob storage.
   *
   * @param imageId The unique identifier of the dog image to delete.
   * @return A Mono that completes when the deletion is finished, emitting no result.
   */
  @Transactional
  public Mono<Void> deleteDogImage(String imageId) {
    return dogImageRepository
        .findById(UUID.fromString(imageId))
        .flatMap(deleteDogImageAndAssociatedBlob());
  }

  private Function<DogImageEntity, Mono<? extends Void>> deleteDogImageAndAssociatedBlob() {
    return dogImageEntity ->
        dogImageRepository
            .delete(dogImageEntity)
            .then(blobStorageService.deleteBlob(this.containerName, dogImageEntity.getUri()));
  }

  /**
   * Update an existing dog's information by its ID.
   *
   * @param dogId The UUID of the dog to be updated.
   * @param dog The updated Dog object with new information.
   * @return A Mono containing the updated Dog object.
   */
  @CacheEvict("dogs")
  public Mono<Dog> updateDogInformation(String dogId, Dog dog) {
    return dogRepository
        .findById(UUID.fromString(dogId))
        .map(
            dogEntity ->
                dogEntity.toBuilder()
                    .name(dog.getName() == null ? dogEntity.getName() : dog.getName())
                    .description(
                        dog.getDescription() == null
                            ? dogEntity.getDescription()
                            : dog.getDescription())
                    .age(dog.getAge() == null ? dogEntity.getAge() : dog.getAge())
                    .breed(dog.getBreed() == null ? dogEntity.getBreed() : dog.getBreed())
                    .build())
        .flatMap(dogRepository::save)
        .map(dogMapper::mapToModel);
  }

  private Flux<String> extractBlobNames(Flux<String> uris) {
    return uris.map(uri -> uri.substring(uri.lastIndexOf("/") + 1));
  }

  private Mono<DogImageEntity> saveDogImage(UUID dogId, String blobUrl) {
    return dogImageRepository.save(
        DogImageEntity.builder().id(UUID.randomUUID()).dogId(dogId).uri(blobUrl).build());
  }

  private Mono<String> saveToBlobStorage(FilePart file) {
    String blobName = UUID.randomUUID() + "-" + file.filename();

    return DataBufferUtils.join(file.content())
        .flatMap(uploadFileToBlob(blobName))
        .onErrorMap(
            IOException.class, e -> new Exception("Failed to upload file: " + file.filename(), e));
  }

  private Function<DataBuffer, Mono<? extends String>> uploadFileToBlob(String blobName) {
    return dataBuffer -> {
      InputStream inputStream = dataBuffer.asInputStream(true);
      return blobStorageService
          .uploadToBlob(containerName, blobName, inputStream)
          .doFinally(signalType -> DataBufferUtils.release(dataBuffer));
    };
  }

  private boolean isSupportedImage(FilePart file) {
    MediaType contentType = file.headers().getContentType();
    return contentType != null && SUPPORTED_IMAGE_CONTENT_TYPES.contains(contentType.toString());
  }
}
