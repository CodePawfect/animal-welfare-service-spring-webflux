package com.github.codepawfect.animalwelfareservicespringboot.domain.service;

import com.github.codepawfect.animalwelfareservicespringboot.core.service.BlobStorageService;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.DogRepository;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.ImageUriRepository;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model.DogEntity;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model.ImageUriEntity;
import com.github.codepawfect.animalwelfareservicespringboot.domain.service.mapper.DogMapper;
import com.github.codepawfect.animalwelfareservicespringboot.domain.service.model.Dog;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class DogService {

  private final DogRepository dogRepository;
  private final ImageUriRepository imageUriRepository;
  private final DogMapper dogMapper;
  private final BlobStorageService blobStorageService;

  @Value("${spring.cloud.azure.storage.blob.container-name}")
  private String containerName;

  private static final Set<String> SUPPORTED_IMAGE_CONTENT_TYPES =
      Set.of(MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_GIF_VALUE);

  public Flux<Dog> getDogs() {
    return dogRepository.findAll().map(dogMapper::mapToModel);
  }

  public Mono<Dog> getDog(String id) {
    return dogRepository.findById(UUID.fromString(id)).map(dogMapper::mapToModel);
  }

  public Mono<Dog> addDog(Dog dog, List<MultipartFile> files) {
    var dogEntity = dogMapper.mapToEntity(dog);
    dogEntity.setId(UUID.randomUUID());

    return Flux.fromIterable(files)
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
              List<Mono<ImageUriEntity>> imageUriMonos =
                  blobUrls.stream()
                      .map(
                          blobUrl ->
                              imageUriRepository.save(
                                  ImageUriEntity.builder()
                                      .dogId(dogEntity.getId())
                                      .uri(blobUrl)
                                      .build()))
                      .collect(Collectors.toList());

              return Mono.zip(
                  dogRepository.save(dogEntity), Flux.merge(imageUriMonos).collectList());
            })
        .flatMap(
            tuple -> {
              DogEntity savedDogEntity = tuple.getT1();
              List<ImageUriEntity> savedImageUris = tuple.getT2();

              Dog dogModel = dogMapper.mapToModel(savedDogEntity);
              dogModel.setImageUris(
                  savedImageUris.stream().map(ImageUriEntity::getUri).collect(Collectors.toList()));

              return Mono.just(dogModel);
            });
  }

  private Mono<String> saveToBlobStorage(MultipartFile file) {
    String blobName = UUID.randomUUID() + "-" + file.getOriginalFilename();

    return Mono.fromCallable(
            () -> {
              try (var inputStream = file.getInputStream()) {
                return blobStorageService.uploadToBlob(containerName, blobName, inputStream);
              }
            })
        .onErrorMap(
            IOException.class,
            e ->
                new IllegalArgumentException(
                    "Failed to upload file: " + file.getOriginalFilename(), e));
  }

  private boolean isSupportedImage(MultipartFile file) {
    return SUPPORTED_IMAGE_CONTENT_TYPES.contains(file.getContentType());
  }
}
