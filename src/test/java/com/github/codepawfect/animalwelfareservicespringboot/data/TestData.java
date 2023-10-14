package com.github.codepawfect.animalwelfareservicespringboot.data;

import java.util.UUID;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model.DogEntity;

public class TestData {
  public static final DogEntity DOG_ENTITY_BUDDY = new DogEntity(UUID.randomUUID(), "Buddy", "Labrador", 5);
}
