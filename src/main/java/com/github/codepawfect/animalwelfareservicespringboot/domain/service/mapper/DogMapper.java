package com.github.codepawfect.animalwelfareservicespringboot.domain.service.mapper;

import com.github.codepawfect.animalwelfareservicespringboot.domain.service.model.Dog;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model.DogEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class DogMapper {

  public Flux<Dog> map(Flux<DogEntity> dogEntities) {
    return dogEntities.map(this::map);
  }

  public Dog map(DogEntity dogEntity) {
    return new Dog(
        dogEntity.getId(),
        dogEntity.getName(),
        dogEntity.getBreed(),
        dogEntity.getAge()
    );
  }
}
