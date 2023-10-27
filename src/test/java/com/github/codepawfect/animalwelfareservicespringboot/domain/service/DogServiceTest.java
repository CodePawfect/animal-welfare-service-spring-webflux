package com.github.codepawfect.animalwelfareservicespringboot.domain.service;

import static org.mockito.Mockito.when;

import com.github.codepawfect.animalwelfareservicespringboot.data.TestData;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.DogRepository;
import com.github.codepawfect.animalwelfareservicespringboot.domain.service.mapper.DogMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class DogServiceTest {

  @Mock private DogRepository dogRepository;

  @Mock private DogMapper dogMapper;

  @InjectMocks private DogService dogService;

  @Test
  void getDogs() {
    // Arrange
    when(dogRepository.findAll()).thenReturn(Flux.fromIterable(List.of(TestData.DOG_ENTITY_BUDDY)));
    when(dogMapper.mapEntity(TestData.DOG_ENTITY_BUDDY)).thenReturn(TestData.DOG_BUDDY);

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
    when(dogMapper.mapEntity(TestData.DOG_ENTITY_BUDDY)).thenReturn(TestData.DOG_BUDDY);

    // Act & Assert
    StepVerifier.create(dogService.getDog(TestData.DOG_ENTITY_BUDDY.getId().toString()))
        .expectNext(TestData.DOG_BUDDY)
        .expectComplete()
        .verify();
  }

  @Test
  void addDog() {
    // Arrange
    when(dogMapper.mapModel(TestData.DOG_BUDDY)).thenReturn(TestData.DOG_ENTITY_BUDDY);
    when(dogRepository.save(TestData.DOG_ENTITY_BUDDY))
        .thenReturn(Mono.just(TestData.DOG_ENTITY_BUDDY));
    when(dogMapper.mapEntity(TestData.DOG_ENTITY_BUDDY)).thenReturn(TestData.DOG_BUDDY);

    // Act & Assert
    StepVerifier.create(dogService.addDog(TestData.DOG_BUDDY))
        .expectNext(TestData.DOG_BUDDY)
        .expectComplete()
        .verify();
  }
}
