package com.github.codepawfect.animalwelfareservicespringboot.data;

import java.util.UUID;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model.DogEntity;
import com.github.codepawfect.animalwelfareservicespringboot.domain.service.model.Dog;

public class TestData {
  public static final DogEntity DOG_ENTITY_BUDDY = new DogEntity(UUID.randomUUID(), "Buddy", "Labrador", 5);
  public static final Dog DOG_BUDDY = new Dog(UUID.randomUUID(), "Buddy", "Labrador", 5);
}
