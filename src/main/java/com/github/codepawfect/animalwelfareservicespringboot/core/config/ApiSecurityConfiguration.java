package com.github.codepawfect.animalwelfareservicespringboot.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
@Configuration
public class ApiSecurityConfiguration {

  @Bean
  SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {
    http
        .authorizeExchange(authorize -> authorize
            .pathMatchers(HttpMethod.GET,"/v1/dogs").permitAll()
            .pathMatchers(HttpMethod.GET,"/v1/dog/**").permitAll()
            .pathMatchers("/v1/dog/**").hasRole("ADMIN")
            .anyExchange().denyAll()
        );
    return http.build();
  }
}
