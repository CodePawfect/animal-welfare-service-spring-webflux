package com.github.codepawfect.animalwelfareservicespringboot.integrationtest;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import com.github.codepawfect.animalwelfareservicespringboot.data.TestData;
import com.github.codepawfect.animalwelfareservicespringboot.domain.controller.model.DogResource;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.DogRepository;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model.DogEntity;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

class DogControllerIntegrationTest extends AbstractIntegrationTest {

  @Autowired private DogRepository dogRepository;

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
        .log()
        .ifValidationFails()
        .body("dogResources[0].id", equalTo(dogEntity.getId().toString()));
  }

  @Test
  void getDog_returns_200_with_expected_dog() {
    when()
        .get("/v1/dog/" + dogEntity.getId())
        .then()
        .statusCode(200)
        .log()
        .ifValidationFails()
        .body("id", equalTo(dogEntity.getId().toString()));
  }

  @Test
  @WithMockUser(
      username = "user",
      roles = {"ADMIN"})
  void createDog_returns_201_with_expected_dog() {
    byte[] fileContent = "Test Image Content".getBytes();
    String fileName = "test.jpg";

    given()
        .contentType(MULTIPART_FORM_DATA_VALUE)
        .multiPart("dog", new DogResource(null, "Bello", "Mix", 2, null), "application/json")
        .multiPart("files", fileName, fileContent, "image/jpeg")
        .when()
        .post("v1/dog")
        .then()
        .statusCode(201)
        .log()
        .ifValidationFails()
        .body("id", notNullValue())
        .body("name", equalTo("Bello"))
        .body("breed", equalTo("Mix"))
        .body("age", equalTo(2))
        .body("imageUris[0]", notNullValue());
  }
}
