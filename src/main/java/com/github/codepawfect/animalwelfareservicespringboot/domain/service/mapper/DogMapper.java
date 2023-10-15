package com.github.codepawfect.animalwelfareservicespringboot.domain.service.mapper;

import com.github.codepawfect.animalwelfareservicespringboot.domain.service.model.Dog;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model.DogEntity;
import org.springframework.stereotype.Component;

@Component
public class DogMapper {

  public Dog mapEntity(DogEntity dogEntity) {
    return new Dog(
        dogEntity.getId(),
        dogEntity.getName(),
        dogEntity.getBreed(),
        dogEntity.getAge()
    );
  }
}
