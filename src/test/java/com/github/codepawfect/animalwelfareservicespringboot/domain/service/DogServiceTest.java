package com.github.codepawfect.animalwelfareservicespringboot.domain.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.github.codepawfect.animalwelfareservicespringboot.core.service.BlobStorageService;
import com.github.codepawfect.animalwelfareservicespringboot.data.TestData;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.DogImageRepository;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.DogRepository;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model.DogEntity;
import com.github.codepawfect.animalwelfareservicespringboot.domain.service.mapper.DogMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
  @Mock private DogMapper dogMapper;
  @Mock private BlobStorageService blobStorageService;
  @InjectMocks private DogService dogService;

  @Test
  void getDogs() {
    // Arrange
    when(dogRepository.findAll()).thenReturn(Flux.fromIterable(List.of(TestData.DOG_ENTITY_BUDDY)));
    when(dogImageRepository.findAllByDogId(TestData.DOG_ENTITY_BUDDY.getId()))
        .thenReturn(Flux.fromIterable(List.of(TestData.DOG_IMAGE_ENTITY)));
    when(dogMapper.mapToModel(TestData.DOG_ENTITY_BUDDY)).thenReturn(TestData.DOG_BUDDY);

    // Act & Assert
    StepVerifier.create(dogService.getDogs())
        .expectNext(
            TestData.DOG_BUDDY.toBuilder()
                .imageUris(List.of(TestData.DOG_IMAGE_ENTITY.getUri()))
                .build())
        .expectComplete()
        .verify();
  }

  @Test
  void getDog() {
    // Arrange
    when(dogRepository.findById(TestData.DOG_ENTITY_BUDDY.getId()))
        .thenReturn(Mono.just(TestData.DOG_ENTITY_BUDDY));
    when(dogImageRepository.findAllByDogId(TestData.DOG_ENTITY_BUDDY.getId()))
        .thenReturn(Flux.fromIterable(List.of(TestData.DOG_IMAGE_ENTITY)));
    when(dogMapper.mapToModel(TestData.DOG_ENTITY_BUDDY)).thenReturn(TestData.DOG_BUDDY);

    // Act & Assert
    StepVerifier.create(dogService.getDog(TestData.DOG_ENTITY_BUDDY.getId().toString()))
        .expectNext(
            TestData.DOG_BUDDY.toBuilder()
                .imageUris(List.of(TestData.DOG_IMAGE_ENTITY.getUri()))
                .build())
        .expectComplete()
        .verify();
  }

  @Test
  void addDog() throws IOException {
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

    when(dogMapper.mapToEntity(TestData.DOG_BUDDY)).thenReturn(TestData.DOG_ENTITY_BUDDY);
    when(dogRepository.save(any(DogEntity.class))).thenReturn(Mono.just(TestData.DOG_ENTITY_BUDDY));
    when(dogMapper.mapToModel(TestData.DOG_ENTITY_BUDDY)).thenReturn(TestData.DOG_BUDDY);
    when(blobStorageService.uploadToBlob(anyString(), anyString(), any(InputStream.class)))
        .thenReturn(mockUrl);

    // Act & Assert
    StepVerifier.create(dogService.addDog(TestData.DOG_BUDDY, filePartsFlux))
        .expectNext(TestData.DOG_BUDDY)
        .expectComplete()
        .verify();

    verify(blobStorageService, times(1)).uploadToBlob(any(), anyString(), any(InputStream.class));
  }
}
