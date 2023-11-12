package com.github.codepawfect.animalwelfareservicespringboot.integrationtest;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class AbstractIntegrationTest {

  @LocalServerPort int randomServerPort;

  private static final String CONNECTION_STRING =
      "DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;"
          + "AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50u"
          + "SRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://localhost:";
  private static final String ACCOUNT = "devstoreaccount1";

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgreSql = new PostgreSQLContainer<>("postgres:latest");

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "spring.cloud.azure.storage.blob.connection-string",
        () -> CONNECTION_STRING + azurite.getMappedPort(10000) + "/" + ACCOUNT + ";");
  }

  @Container
  static GenericContainer<?> azurite =
      new GenericContainer<>("mcr.microsoft.com/azure-storage/azurite")
          .withExposedPorts(10000)
          .withCommand("azurite-blob --blobHost 0.0.0.0 --loose --skipApiVersionCheck");

  @BeforeAll
  static void setup() {
    String connectionString =
        CONNECTION_STRING + azurite.getMappedPort(10000) + "/" + ACCOUNT + ";";

    BlobServiceClient blobServiceClient =
        new BlobServiceClientBuilder().connectionString(connectionString).buildClient();

    blobServiceClient.createBlobContainer("dog-images");
  }

  @BeforeEach
  void init() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = randomServerPort;
  }
}
