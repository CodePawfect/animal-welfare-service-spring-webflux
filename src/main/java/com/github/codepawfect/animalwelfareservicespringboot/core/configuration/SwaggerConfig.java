package com.github.codepawfect.animalwelfareservicespringboot.core.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI springShopOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("DOG-SERVICE API")
                .description(
                    "Service providing dog information for the Animal Welfare Service Spring Boot application"));
  }
}
