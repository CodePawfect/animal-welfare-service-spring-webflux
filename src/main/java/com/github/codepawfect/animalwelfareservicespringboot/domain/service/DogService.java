package com.github.codepawfect.animalwelfareservicespringboot.domain.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.github.codepawfect.animalwelfareservicespringboot.core.service.BlobStorageService;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.DogImageRepository;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.DogRepository;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model.DogEntity;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model.DogImageEntity;
import com.github.codepawfect.animalwelfareservicespringboot.domain.service.mapper.DogMapper;
import com.github.codepawfect.animalwelfareservicespringboot.domain.service.model.Dog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service class for managing dogs and their images.
 */
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
  public Flux<Dog> getDogs() {
    return dogRepository
        .findAll()
        .flatMap(
            dogEntity ->
                dogImageRepository
                    .findAllByDogId(dogEntity.getId())
                    .collectList()
                    .map(
                        dogImageEntities -> {
                          Dog dog = dogMapper.mapToModel(dogEntity);
                          dog.setImageUris(
                              dogImageEntities.stream().map(DogImageEntity::getUri).toList());
                          return dog;
                        }));
  }

  /**
   * Retrieve a specific dog by its ID along with its associated images.
   *
   * @param id The UUID of the dog to retrieve.
   * @return A Mono containing the Dog object with image URIs, or empty if not found.
   */
  public Mono<Dog> getDog(String id) {
    return dogRepository
        .findById(UUID.fromString(id))
        .flatMap(
            dogEntity ->
                dogImageRepository
                    .findAllByDogId(dogEntity.getId())
                    .collectList()
                    .map(
                        dogImageEntities -> {
                          Dog dog = dogMapper.mapToModel(dogEntity);
                          dog.setImageUris(
                              dogImageEntities.stream().map(DogImageEntity::getUri).toList());
                          return dog;
                        }));
  }

  /**
   * Add a new dog along with its images.
   *
   * @param dog           The Dog object to be added.
   * @param filePartFlux  A Flux of FilePart objects representing image files.
   * @return A Mono containing the added Dog object with image URIs.
   */
  public Mono<Dog> addDog(Dog dog, Flux<FilePart> filePartFlux) {
    var dogEntity = dogMapper.mapToEntity(dog);
    dogEntity.setId(UUID.randomUUID());

    return filePartFlux
        .filter(this::isSupportedImage)
        .switchIfEmpty(
            Mono.error(
                new IllegalArgumentException(
                    "No supported image files found in request. Supported image types are JPEG and PNG.")))
        .flatMap(this::saveToBlobStorage)
        .onErrorResume(
            e -> {
              log.error("Failed to upload file to blob storage", e);
              return Mono.empty();
            })
        .collectList()
        .flatMap(
            blobUrls -> {
              List<Mono<DogImageEntity>> dogImageEntities =
                  blobUrls.stream()
                      .map(blobUrl -> saveDogImage(dogEntity.getId(), blobUrl))
                      .toList();

              return Mono.zip(
                  dogRepository.save(dogEntity), Flux.merge(dogImageEntities).collectList());
            })
        .flatMap(
            tuple -> {
              DogEntity savedDogEntity = tuple.getT1();
              List<DogImageEntity> savedImageUris = tuple.getT2();

              Dog dogModel = dogMapper.mapToModel(savedDogEntity);
              dogModel.setImageUris(savedImageUris.stream().map(DogImageEntity::getUri).toList());

              return Mono.just(dogModel);
            });
  }

  /**
   * Delete a dog and its associated images by ID.
   *
   * @param id The UUID of the dog to be deleted.
   * @return A Mono indicating the completion of the deletion operation.
   */
  @Transactional
  public Mono<Void> deleteDog(String id) {
    return dogRepository
        .findById(UUID.fromString(id))
        .switchIfEmpty(Mono.error(new IllegalStateException("Dog not found with ID: " + id)))
        .flatMap(
            dogEntity -> {
              Flux<String> uris =
                  dogImageRepository.findAllByDogId(dogEntity.getId()).map(DogImageEntity::getUri);
              return dogImageRepository
                  .deleteById(dogEntity.getId())
                  .then(blobStorageService.deleteBlobs(this.containerName, extractBlobNames(uris)))
                  .then(dogRepository.deleteById(dogEntity.getId()));
            });
  }

  /**
   * Deletes a dog image from the repository and associated blob storage.
   *
   * @param imageId The unique identifier of the dog image to delete.
   * @return A Mono that completes when the deletion is finished, emitting no result.
   */
  public Mono<Void> deleteDogImage(String imageId) {
     return dogImageRepository.findById(UUID.fromString(imageId))
         .flatMap(dogImageEntity -> dogImageRepository.delete(dogImageEntity)
             .then(blobStorageService.deleteBlob(this.containerName, dogImageEntity.getUri())));
  }

  /**
   * Update an existing dog's information by its ID.
   *
   * @param dogId The UUID of the dog to be updated.
   * @param dog   The updated Dog object with new information.
   * @return A Mono containing the updated Dog object.
   */
  public Mono<Dog> updateDogInformation(UUID dogId, Dog dog) {
    return dogRepository.findById(dogId)
        .map(dogEntity -> dogEntity.toBuilder()
            .name(dog.getName() == null ? dogEntity.getName() : dog.getName())
            .description(dog.getDescription() == null ? dogEntity.getDescription() : dog.getDescription())
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
        .flatMap(
            dataBuffer -> {
              InputStream inputStream = dataBuffer.asInputStream(true);
              return blobStorageService
                  .uploadToBlob(containerName, blobName, inputStream)
                  .doFinally(signalType -> DataBufferUtils.release(dataBuffer));
            })
        .onErrorMap(
            IOException.class,
            e -> new IllegalArgumentException("Failed to upload file: " + file.filename(), e));
  }

  private boolean isSupportedImage(FilePart file) {
    MediaType contentType = file.headers().getContentType();
    return contentType != null && SUPPORTED_IMAGE_CONTENT_TYPES.contains(contentType.toString());
  }
}
