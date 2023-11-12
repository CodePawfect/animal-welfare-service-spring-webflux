package com.github.codepawfect.animalwelfareservicespringboot.integrationtest;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.codepawfect.animalwelfareservicespringboot.data.TestData;
import com.github.codepawfect.animalwelfareservicespringboot.domain.controller.model.DogCreateResource;
import com.github.codepawfect.animalwelfareservicespringboot.domain.controller.model.DogResource;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.DogRepository;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model.DogEntity;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
  void createDog_returns_201_with_expected_dog() throws IOException {
    byte[] sampleImage1 = Files.readAllBytes(Paths.get("src/test/resources/test.jpg"));
    byte[] sampleImage2 = Files.readAllBytes(Paths.get("src/test/resources/test.jpg"));

    DogCreateResource dogCreateResource = new DogCreateResource( "Bello", "Mix", "Description", 2);
    String dogCreateResourceJson = new ObjectMapper().writeValueAsString(dogCreateResource);


    given()
        .contentType(MULTIPART_FORM_DATA_VALUE)
        .multiPart("dogCreateResource", dogCreateResourceJson, MediaType.APPLICATION_JSON_VALUE)
        .multiPart("files", "test.jpg", sampleImage1, "image/jpeg")
        .multiPart("files", "test.jpg", sampleImage2, "image/jpeg")
        .when()
        .post("v1/dog")
        .then()
        .statusCode(201)
        .log()
        .ifValidationFails()
        .body("id", notNullValue())
        .body("name", equalTo("Bello"))
        .body("breed", equalTo("Mix"))
        .body("description", equalTo("Description"))
        .body("age", equalTo(2))
        .body("imageUris[0]", notNullValue());
  }
}
