package com.github.codepawfect.animalwelfareservicespringboot.data;

import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model.DogEntity;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model.DogImageEntity;
import com.github.codepawfect.animalwelfareservicespringboot.domain.service.model.Dog;
import java.util.UUID;

public class TestData {
  private static final String NAME = "Buddy";
  private static final String BREED = "Labrador";
  private static final String DESCRIPTION = "Description";
  private static final Integer AGE = 5;
  private static final UUID DOG_ID = UUID.randomUUID();
  public static final DogEntity DOG_ENTITY_BUDDY =
      new DogEntity(DOG_ID, NAME, BREED, DESCRIPTION, AGE);
  public static final DogImageEntity DOG_IMAGE_ENTITY = DogImageEntity.builder()
      .id(UUID.randomUUID())
      .dogId(DOG_ID)
      .uri("https://animalwelfareservice.blob.core.windows.net/dog-images/1.jpg")
      .build();
  public static final Dog DOG_BUDDY =
      Dog.builder()
          .id(UUID.randomUUID())
          .name(NAME)
          .breed(BREED)
          .age(AGE)
          .description(DESCRIPTION)
          .build();
}
