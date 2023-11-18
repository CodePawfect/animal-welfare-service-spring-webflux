package com.github.codepawfect.animalwelfareservicespringboot.integrationtest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.codepawfect.animalwelfareservicespringboot.data.TestData;
import com.github.codepawfect.animalwelfareservicespringboot.domain.controller.model.DogCreateResource;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.DogImageRepository;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.DogRepository;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model.DogEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

class DogControllerIntegrationTest extends AbstractIntegrationTest {

  @Autowired private DogRepository dogRepository;
  @Autowired private DogImageRepository dogImageRepository;

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
    dogImageRepository.deleteAll().block();
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

    DogCreateResource dogCreateResource = new DogCreateResource("Bello", "Mix", "Description", 2);
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

  @Test
  @WithMockUser(
      username = "user",
      roles = {"ADMIN"})
  void deleteDog_returns_201_with_expected_dog() {
    given()
        .when()
        .delete("v1/dog/" + dogEntity.getId())
        .then()
        .statusCode(204)
        .log()
        .ifValidationFails();
  }

  @Test
  @WithMockUser(
      username = "user",
      roles = {"ADMIN"})
  void deleteDogImage_returns_201() {
    // TODO: implement test
  }

  @Test
  @WithMockUser(
      username = "user",
      roles = {"ADMIN"})
  void updateDogInformation_returns_201() {
    // TODO: implement test
  }

  @Test
  void deleteDogImage_returns_401() {
    // TODO: implement test
  }

  @Test
  void updateDogInformation_returns_401() {
    // TODO: implement test
  }

  @Test
  void createDog_returns_401() {
    given().when().post("v1/dog").then().statusCode(401).log().ifValidationFails();
  }

  @Test
  void deleteDog_returns_401() {
    given().when().delete("v1/dog/123").then().statusCode(401).log().ifValidationFails();
  }
}
