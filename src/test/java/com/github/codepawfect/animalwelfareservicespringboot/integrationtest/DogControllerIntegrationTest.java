package com.github.codepawfect.animalwelfareservicespringboot.integrationtest;

import com.github.codepawfect.animalwelfareservicespringboot.data.TestData;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.DogRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.when;

class DogControllerIntegrationTest extends AbstractIntegrationTest {

  @Autowired
  private DogRepository dogRepository;

  @BeforeEach
  void init() {
    super.init();
    dogRepository.save(TestData.DOG_ENTITY_BUDDY).block();
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
        .body("dogResources[0].name", org.hamcrest.Matchers.equalTo("Buddy"));
  }
}
