package com.github.codepawfect.animalwelfareservicespringboot.domain.service;

import java.util.UUID;
import com.github.codepawfect.animalwelfareservicespringboot.domain.service.mapper.DogMapper;
import com.github.codepawfect.animalwelfareservicespringboot.domain.service.model.Dog;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.DogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class DogService {

  private final DogRepository dogRepository;
  private final DogMapper dogMapper;

  public Flux<Dog> getDogs() {
    return dogRepository.findAll()
        .map(dogMapper::mapEntity);
  }

  public Mono<Dog> getDog(String id) {
    return dogRepository.findById(UUID.fromString(id))
        .map(dogMapper::mapEntity);
  }
}
