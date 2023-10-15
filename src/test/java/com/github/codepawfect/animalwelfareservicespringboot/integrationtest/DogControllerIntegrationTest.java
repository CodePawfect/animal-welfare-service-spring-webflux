package com.github.codepawfect.animalwelfareservicespringboot.integrationtest;

import java.util.UUID;
import com.github.codepawfect.animalwelfareservicespringboot.data.TestData;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.DogRepository;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model.DogEntity;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;

class DogControllerIntegrationTest extends AbstractIntegrationTest {

  @Autowired
  private DogRepository dogRepository;

  private DogEntity dogEntity;


  @BeforeEach
  void init() {
    super.init();
    UUID randomDogId = UUID.randomUUID();
    this.dogEntity = TestData.DOG_ENTITY_BUDDY.toBuilder().id(randomDogId).build();
    dogRepository.save(dogEntity).block();
  }

  @AfterEach
  void cleanUp() {
    dogRepository.deleteAll().block();
  }

  @Test
  void getDogs_returns_200_with_expected_dogs() {
    when()
        .get("/v1/dogs")
        .then()
        .statusCode(200)
        .log().ifValidationFails()
        .body("dogResources[0].id", equalTo(dogEntity.getId().toString()));
  }

  @Test
  void getDog_returns_200_with_expected_dog() {
    when()
        .get("/v1/dog/" + dogEntity.getId())
        .then()
        .statusCode(200)
        .log().ifValidationFails()
        .body("id", equalTo(dogEntity.getId().toString()));
  }
}
