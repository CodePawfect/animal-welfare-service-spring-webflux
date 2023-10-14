package com.github.codepawfect.animalwelfareservicespringboot.domain.controller.mapper;

import java.util.List;
import com.github.codepawfect.animalwelfareservicespringboot.domain.service.model.Dog;
import com.github.codepawfect.animalwelfareservicespringboot.domain.controller.model.DogResource;
import com.github.codepawfect.animalwelfareservicespringboot.domain.controller.model.DogResources;
import org.springframework.stereotype.Component;

@Component
public class DogGetResourceMapper {

  public DogResources map(List<Dog> dogs) {
    List<DogResource> dogResources = dogs.stream()
        .map(this::map)
        .toList();
    return new DogResources(dogResources);
  }

  public DogResource map(Dog dog) {
    return new DogResource(
        dog.id(),
        dog.name(),
        dog.breed(),
        dog.age()
    );
  }
}
