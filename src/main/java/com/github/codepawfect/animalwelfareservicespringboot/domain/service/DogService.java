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
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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

  public Flux<Dog> getDogs() {
    return dogRepository.findAll().map(dogMapper::mapToModel);
  }

  public Mono<Dog> getDog(String id) {
    return dogRepository.findById(UUID.fromString(id)).map(dogMapper::mapToModel);
  }

  public Mono<Dog> addDog(Dog dog, Flux<FilePart> filePartFlux) {
    var dogEntity = dogMapper.mapToEntity(dog);
    dogEntity.setId(UUID.randomUUID());

    return filePartFlux
        .filter(this::isSupportedImage)
        .flatMap(this::saveToBlobStorage)
        .onErrorResume(
            e -> {
              log.error("Failed to upload file", e);
              return Mono.empty();
            })
        .collectList()
        .flatMap(
            blobUrls -> {
              List<Mono<DogImageEntity>> imageUriMonos =
                  blobUrls.stream()
                      .map(
                          blobUrl ->
                              dogImageRepository.save(
                                  DogImageEntity.builder()
                                      .id(UUID.randomUUID())
                                      .dogId(dogEntity.getId())
                                      .uri(blobUrl)
                                      .build()))
                      .toList();

              return Mono.zip(
                  dogRepository.save(dogEntity), Flux.merge(imageUriMonos).collectList());
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

  private Mono<String> saveToBlobStorage(FilePart file) {
    String blobName = UUID.randomUUID() + "-" + file.filename();

    return file.content()
        .flatMap(
            dataBuffer -> {
              InputStream inputStream = dataBuffer.asInputStream(true);
              return Mono.fromCallable(
                      () -> blobStorageService.uploadToBlob(containerName, blobName, inputStream))
                  .subscribeOn(Schedulers.boundedElastic())
                  .doFinally(
                      signalType ->
                          DataBufferUtils.release(dataBuffer)); // Ensure releasing the data buffer
            })
        .next() // Since we are expecting only one FilePart, we can use next() to get Mono
        .onErrorMap(
            IOException.class,
            e -> new IllegalArgumentException("Failed to upload file: " + file.filename(), e));
  }

  private boolean isSupportedImage(FilePart file) {
    MediaType contentType = file.headers().getContentType();
    return contentType != null && SUPPORTED_IMAGE_CONTENT_TYPES.contains(contentType.toString());
  }
}
