package com.github.codepawfect.animalwelfareservicespringboot.core.configuration;

import com.github.codepawfect.animalwelfareservicespringboot.core.jwt.JwtAuthenticationConverter;
import com.github.codepawfect.animalwelfareservicespringboot.core.jwt.JwtAuthenticationManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;

@EnableWebFluxSecurity
@Configuration
public class ApiSecurityConfiguration {

  @Value("${management.username}")
  private String username;

  @Value("${management.password}")
  private String password;

  @Bean
  SecurityWebFilterChain springWebFilterChain(
      ServerHttpSecurity http,
      JwtAuthenticationConverter jwtAuthenticationConverter,
      JwtAuthenticationManager jwtAuthenticationManager) {
    AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(jwtAuthenticationManager);
    jwtFilter.setServerAuthenticationConverter(jwtAuthenticationConverter);

    http.csrf(ServerHttpSecurity.CsrfSpec::disable)
        .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
        .authorizeExchange(
            authorize ->
                authorize
                    .pathMatchers(HttpMethod.GET, "/v1/dogs")
                    .permitAll()
                    .pathMatchers(HttpMethod.GET, "/v1/dog/{id}")
                    .permitAll()
                    .pathMatchers(HttpMethod.POST, "/v1/dog")
                    .hasRole("ADMIN")
                    .pathMatchers("/api-documentation/**")
                    .permitAll()
                    .pathMatchers("/login")
                    .permitAll()
                    .anyExchange()
                    .denyAll());
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public MapReactiveUserDetailsService authentication() {
    UserDetails adminUser =
        User.builder()
            .username(username)
            .password(passwordEncoder().encode(password))
            .roles("ADMIN")
            .build();
    return new MapReactiveUserDetailsService(adminUser);
  }
}
