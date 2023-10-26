package com.github.codepawfect.animalwelfareservicespringboot.integrationtest;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class AbstractIntegrationTest {

  @LocalServerPort int randomServerPort;

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgreSql = new PostgreSQLContainer<>("postgres:latest");

  @BeforeEach
  void init() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = randomServerPort;
  }
}
