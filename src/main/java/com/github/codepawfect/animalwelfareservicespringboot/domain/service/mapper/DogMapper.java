package com.github.codepawfect.animalwelfareservicespringboot.domain.service.mapper;

import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model.DogEntity;
import com.github.codepawfect.animalwelfareservicespringboot.domain.service.model.Dog;
import org.springframework.stereotype.Component;

@Component
public class DogMapper {

  public Dog mapToModel(DogEntity dogEntity) {
    return Dog.builder()
        .id(dogEntity.getId())
        .age(dogEntity.getAge())
        .breed(dogEntity.getBreed())
        .name(dogEntity.getName())
        .build();
  }

  public DogEntity mapToEntity(Dog dog) {
    return DogEntity.builder()
        .id(dog.getId())
        .age(dog.getAge())
        .breed(dog.getBreed())
        .name(dog.getName())
        .build();
  }
}
