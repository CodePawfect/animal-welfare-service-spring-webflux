package com.github.codepawfect.animalwelfareservicespringboot.domain.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.github.codepawfect.animalwelfareservicespringboot.core.service.BlobStorageService;
import com.github.codepawfect.animalwelfareservicespringboot.data.TestData;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.DogRepository;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model.DogEntity;
import com.github.codepawfect.animalwelfareservicespringboot.domain.service.mapper.DogMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class DogServiceTest {
  @Mock private DogRepository dogRepository;
  @Mock private DogMapper dogMapper;
  @Mock private BlobStorageService blobStorageService;
  @InjectMocks private DogService dogService;

  @Test
  void getDogs() {
    // Arrange
    when(dogRepository.findAll()).thenReturn(Flux.fromIterable(List.of(TestData.DOG_ENTITY_BUDDY)));
    when(dogMapper.mapToModel(TestData.DOG_ENTITY_BUDDY)).thenReturn(TestData.DOG_BUDDY);

    // Act & Assert
    StepVerifier.create(dogService.getDogs())
        .expectNext(TestData.DOG_BUDDY)
        .expectComplete()
        .verify();
  }

  @Test
  void getDog() {
    // Arrange
    when(dogRepository.findById(TestData.DOG_ENTITY_BUDDY.getId()))
        .thenReturn(Mono.just(TestData.DOG_ENTITY_BUDDY));
    when(dogMapper.mapToModel(TestData.DOG_ENTITY_BUDDY)).thenReturn(TestData.DOG_BUDDY);

    // Act & Assert
    StepVerifier.create(dogService.getDog(TestData.DOG_ENTITY_BUDDY.getId().toString()))
        .expectNext(TestData.DOG_BUDDY)
        .expectComplete()
        .verify();
  }

  @Test
  void addDog() throws IOException {
    // Arrange
    MultipartFile mockFile = mock(MultipartFile.class);
    when(mockFile.getContentType()).thenReturn(MediaType.IMAGE_JPEG_VALUE);
    when(mockFile.getOriginalFilename()).thenReturn("test-image.jpg");
    when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
    List<MultipartFile> files = Collections.singletonList(mockFile);

    String mockUrl = "http://mockstorage.com/test-image.jpg";

    when(dogMapper.mapToEntity(TestData.DOG_BUDDY)).thenReturn(TestData.DOG_ENTITY_BUDDY);
    when(dogRepository.save(any(DogEntity.class))).thenReturn(Mono.just(TestData.DOG_ENTITY_BUDDY));
    when(dogMapper.mapToModel(TestData.DOG_ENTITY_BUDDY)).thenReturn(TestData.DOG_BUDDY);
    when(blobStorageService.uploadToBlob(anyString(), anyString(), any(InputStream.class)))
        .thenReturn(mockUrl);

    // Act & Assert
    StepVerifier.create(dogService.addDog(TestData.DOG_BUDDY, files))
        .expectNext(TestData.DOG_BUDDY)
        .expectComplete()
        .verify();

    verify(blobStorageService, times(1)).uploadToBlob(any(), anyString(), any(InputStream.class));
  }
}
