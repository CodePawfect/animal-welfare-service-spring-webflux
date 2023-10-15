package com.github.codepawfect.animalwelfareservicespringboot.data;

import java.util.UUID;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model.DogEntity;
import com.github.codepawfect.animalwelfareservicespringboot.domain.service.model.Dog;

public class TestData {

  private static final String NAME = "Buddy";
  private static final String BREED = "Labrador";
  private static final Integer AGE = 5;
  public static final DogEntity DOG_ENTITY_BUDDY = new DogEntity(UUID.randomUUID(), NAME, BREED, AGE);
  public static final Dog DOG_BUDDY = new Dog(UUID.randomUUID(), NAME, BREED, AGE);
}
