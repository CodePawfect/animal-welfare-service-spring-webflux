package com.github.codepawfect.animalwelfareservicespringboot.domain.controller.mapper;

import com.github.codepawfect.animalwelfareservicespringboot.domain.controller.model.DogResource;
import com.github.codepawfect.animalwelfareservicespringboot.domain.controller.model.DogResources;
import com.github.codepawfect.animalwelfareservicespringboot.domain.service.model.Dog;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DogResourceMapper {

  public DogResources map(List<Dog> dogs) {
    List<DogResource> dogResources = dogs.stream().map(this::map).toList();
    return new DogResources(dogResources);
  }

  public DogResource map(Dog dog) {
    return new DogResource(
        dog.getId(), dog.getName(), dog.getBreed(), dog.getAge(), dog.getImageUris());
  }

  public Dog map(DogResource dogResource) {
    return Dog.builder()
        .id(dogResource.id())
        .breed(dogResource.breed())
        .age(dogResource.age())
        .name(dogResource.name())
        .imageUris(dogResource.imageUris())
        .build();
  }
}
