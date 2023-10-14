package com.github.codepawfect.animalwelfareservicespringboot.domain.service;

import com.github.codepawfect.animalwelfareservicespringboot.domain.service.mapper.DogMapper;
import com.github.codepawfect.animalwelfareservicespringboot.domain.service.model.Dog;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.DogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Service
public class DogService {

  private final DogRepository dogRepository;
  private final DogMapper dogMapper;

  public Flux<Dog> getDogs() {
    return dogMapper.map(dogRepository.findAll());
  }
}
