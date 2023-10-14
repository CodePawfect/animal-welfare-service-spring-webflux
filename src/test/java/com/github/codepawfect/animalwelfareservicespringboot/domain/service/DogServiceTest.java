package com.github.codepawfect.animalwelfareservicespringboot.domain.service;

import java.util.List;
import java.util.UUID;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.DogRepository;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model.DogEntity;
import com.github.codepawfect.animalwelfareservicespringboot.domain.service.mapper.DogMapper;
import com.github.codepawfect.animalwelfareservicespringboot.domain.service.model.Dog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DogServiceTest {

  @Mock
  private DogRepository dogRepository;

  @Mock
  private DogMapper dogMapper;

  @InjectMocks
  private DogService dogService;

  @Test
  void getDogs() {
    // Arrange
    var dog = new Dog(UUID.randomUUID(), "Buddy", "Labrador", 5);
    var dogEntityFlux = Flux.fromIterable(
        List.of(new DogEntity(UUID.randomUUID(), "Buddy", "Labrador", 5)));
    var dogFlux = Flux.fromIterable(List.of(dog));

    when(dogRepository.findAll()).thenReturn(dogEntityFlux);
    when(dogMapper.map(dogEntityFlux)).thenReturn(dogFlux);

    // Act & Assert
    StepVerifier.create(dogService.getDogs())
        .expectNext(dog) // Assert that the expected dog is emitted
        .expectComplete() // Assert that the Flux completes successfully
        .verify();
  }
}