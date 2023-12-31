package com.github.codepawfect.animalwelfareservicespringboot.domain.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.github.codepawfect.animalwelfareservicespringboot.core.service.BlobStorageService;
import com.github.codepawfect.animalwelfareservicespringboot.data.TestData;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.DogImageRepository;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.DogRepository;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model.DogEntity;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model.DogImageEntity;
import com.github.codepawfect.animalwelfareservicespringboot.domain.service.mapper.DogMapper;
import com.github.codepawfect.animalwelfareservicespringboot.domain.service.model.Dog;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class DogServiceTest {
  @Mock private DogRepository dogRepository;
  @Mock private DogImageRepository dogImageRepository;
  @Spy private DogMapper dogMapper;
  @Mock private BlobStorageService blobStorageService;
  @InjectMocks private DogService dogService;

  @Test
  void getDogs() {
    // Arrange
    when(dogRepository.findAll()).thenReturn(Flux.fromIterable(List.of(TestData.DOG_ENTITY_BUDDY)));
    when(dogImageRepository.findAllByDogId(TestData.DOG_ENTITY_BUDDY.getId()))
        .thenReturn(Flux.fromIterable(List.of(TestData.DOG_IMAGE_ENTITY)));

    // Act & Assert
    StepVerifier.create(dogService.getDogs())
        .expectNext(
            TestData.DOG_BUDDY.toBuilder()
                .imageUris(List.of(TestData.DOG_IMAGE_ENTITY.getUri()))
                .build())
        .expectComplete()
        .verify();

    // Verify interactions
    verify(dogRepository, times(1)).findAll();
    verify(dogImageRepository, times(1)).findAllByDogId(TestData.DOG_ENTITY_BUDDY.getId());
    verify(dogMapper, times(1)).mapToModel(TestData.DOG_ENTITY_BUDDY);
  }

  @Test
  void getDog() {
    // Arrange
    when(dogRepository.findById(TestData.DOG_ENTITY_BUDDY.getId()))
        .thenReturn(Mono.just(TestData.DOG_ENTITY_BUDDY));
    when(dogImageRepository.findAllByDogId(TestData.DOG_ENTITY_BUDDY.getId()))
        .thenReturn(Flux.fromIterable(List.of(TestData.DOG_IMAGE_ENTITY)));

    // Act & Assert
    StepVerifier.create(dogService.getDog(TestData.DOG_ENTITY_BUDDY.getId().toString()))
        .expectNext(
            TestData.DOG_BUDDY.toBuilder()
                .imageUris(List.of(TestData.DOG_IMAGE_ENTITY.getUri()))
                .build())
        .expectComplete()
        .verify();

    // Verify interactions
    verify(dogRepository, times(1)).findById(TestData.DOG_ENTITY_BUDDY.getId());
    verify(dogImageRepository, times(1)).findAllByDogId(TestData.DOG_ENTITY_BUDDY.getId());
    verify(dogMapper, times(1)).mapToModel(TestData.DOG_ENTITY_BUDDY);
  }

  @Test
  void addDog() {
    // Arrange
    FilePart mockFile = mock(FilePart.class);
    when(mockFile.filename()).thenReturn("test-image.jpg");
    when(mockFile.headers()).thenReturn(HttpHeaders.EMPTY);
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.IMAGE_JPEG);
    when(mockFile.headers()).thenReturn(httpHeaders);
    DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap(new byte[0]);
    Flux<DataBuffer> dataBufferFlux = Flux.just(dataBuffer);
    when(mockFile.content()).thenReturn(dataBufferFlux);

    Flux<FilePart> filePartsFlux = Flux.just(mockFile);
    String mockUrl = "http://mockstorage.com/test-image.jpg";

    when(dogRepository.save(any(DogEntity.class))).thenReturn(Mono.just(TestData.DOG_ENTITY_BUDDY));
    when(blobStorageService.uploadToBlob(anyString(), anyString(), any(InputStream.class)))
        .thenReturn(Mono.just(mockUrl));

    // Act & Assert
    StepVerifier.create(dogService.addDog(TestData.DOG_BUDDY, filePartsFlux))
        .expectNext(TestData.DOG_BUDDY)
        .expectComplete()
        .verify();

    // Verify interactions
    verify(dogMapper, times(1)).mapToEntity(TestData.DOG_BUDDY);
    verify(dogRepository, times(1)).save(any(DogEntity.class));
    verify(dogMapper, times(1)).mapToModel(TestData.DOG_ENTITY_BUDDY);
    verify(blobStorageService, times(1)).uploadToBlob(any(), anyString(), any(InputStream.class));
  }

  @Test
  void deleteDogSuccess() {
    // Arrange
    UUID dogId = TestData.DOG_ENTITY_BUDDY.getId();
    when(dogRepository.findById(dogId)).thenReturn(Mono.just(TestData.DOG_ENTITY_BUDDY));
    when(dogImageRepository.findAllByDogId(dogId))
        .thenReturn(Flux.fromIterable(List.of(TestData.DOG_IMAGE_ENTITY)));
    when(dogImageRepository.deleteById(dogId)).thenReturn(Mono.empty());
    when(blobStorageService.deleteBlobs(any(), any())).thenReturn(Mono.empty());
    when(dogRepository.deleteById(dogId)).thenReturn(Mono.empty());

    // Act & Assert
    StepVerifier.create(dogService.deleteDog(dogId.toString())).expectComplete().verify();

    // Verify interactions
    verify(dogRepository, times(1)).deleteById(dogId);
    verify(dogImageRepository, times(1)).findAllByDogId(dogId);
    verify(dogImageRepository, times(1)).deleteById(dogId);
    verify(blobStorageService, times(1)).deleteBlobs(any(), any());
    verify(dogRepository, times(1)).deleteById(dogId);
  }

  @Test
  void deleteDogNotFound() {
    // Arrange
    when(dogRepository.findById(any(UUID.class))).thenReturn(Mono.empty());

    // Act & Assert
    StepVerifier.create(dogService.deleteDog(String.valueOf(UUID.randomUUID())))
        .expectError(IllegalStateException.class)
        .verify();

    // Verify interactions
    verify(dogRepository, never()).deleteById(any(UUID.class));
    verify(dogImageRepository, never()).findAllByDogId(any(UUID.class));
    verify(blobStorageService, never()).deleteBlob(any(), anyString());
    verify(dogImageRepository, never()).delete(any(DogImageEntity.class));
  }

  @Test
  void updateDogInformation_returns_expected_dog_and_200() {
    // Arrange
    UUID dogId = UUID.randomUUID();

    var dogEntity =
        DogEntity.builder()
            .id(dogId)
            .name("Babu")
            .age(2)
            .description("A good dog!")
            .breed("Mix")
            .build();

    Dog dog = Dog.builder().name("Babu").age(4).description("A good dog!").breed("Mix").build();

    when(dogRepository.findById(dogId)).thenReturn(Mono.just(dogEntity));
    when(dogRepository.save(any(DogEntity.class)))
        .thenAnswer(invocation -> Mono.just((DogEntity) invocation.getArgument(0)));

    // Act & Assert
    StepVerifier.create(dogService.updateDogInformation(dogId.toString(), dog))
        .expectNext(
            Dog.builder()
                .id(dogId)
                .name("Babu")
                .age(4)
                .description("A good dog!")
                .breed("Mix")
                .build())
        .expectComplete()
        .verify();

    // Verify interactions
    verify(dogRepository, times(1)).findById(dogId);
    verify(dogRepository, times(1)).save(any(DogEntity.class));
    verify(dogMapper, times(1)).mapToModel(any(DogEntity.class));
  }

  @Test
  void deleteDogImage() {
    // Arrange
    var dogId = UUID.randomUUID();
    var dogImageEntity =
        DogImageEntity.builder().id(dogId).uri(UUID.randomUUID() + "dog.png").build();

    when(dogImageRepository.findById(dogId)).thenReturn(Mono.just(dogImageEntity));
    when(dogImageRepository.delete(dogImageEntity)).thenReturn(Mono.empty());
    when(blobStorageService.deleteBlob(null, dogImageEntity.getUri())).thenReturn(Mono.empty());

    // Act & Assert
    StepVerifier.create(dogService.deleteDogImage(dogId.toString())).expectComplete().verify();

    // Verify interactions
    verify(dogImageRepository, times(1)).findById(dogId);
    verify(dogImageRepository, times(1)).delete(dogImageEntity);
    verify(blobStorageService, times(1)).deleteBlob(null, dogImageEntity.getUri());
  }
}
