package com.github.codepawfect.animalwelfareservicespringboot;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(
    properties =
        "spring.autoconfigure.exclude=com.azure.spring.cloud.autoconfigure.implementation.storage.blob.AzureStorageBlobAutoConfiguration")
class AnimalWelfareServiceSpringBootApplicationTests {

  @Test
  void contextLoads() {}
}
