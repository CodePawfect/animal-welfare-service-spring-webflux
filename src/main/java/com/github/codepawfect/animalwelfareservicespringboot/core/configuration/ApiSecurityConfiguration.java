package com.github.codepawfect.animalwelfareservicespringboot.core.configuration;

import com.github.codepawfect.animalwelfareservicespringboot.core.jwt.JwtAuthenticationConverter;
import com.github.codepawfect.animalwelfareservicespringboot.core.jwt.JwtAuthenticationManager;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
@EnableWebFluxSecurity
@Configuration
public class ApiSecurityConfiguration {

  @Value("${management.username}")
  private String username;

  @Value("${management.password}")
  private String password;

  private static final String ADMIN_ROLE = "ADMIN";

  @Bean
  SecurityWebFilterChain springWebFilterChain(
      ServerHttpSecurity http,
      JwtAuthenticationConverter jwtAuthenticationConverter,
      JwtAuthenticationManager jwtAuthenticationManager) {
    AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(jwtAuthenticationManager);
    jwtFilter.setServerAuthenticationConverter(jwtAuthenticationConverter);

    http.csrf(ServerHttpSecurity.CsrfSpec::disable)
        .cors(ServerHttpSecurity.CorsSpec::disable)
        .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
        .authorizeExchange(
            authorize ->
                authorize
                    // Grouping paths that are accessible to everyone
                    .pathMatchers(HttpMethod.GET, "/v1/dogs", "/v1/dog/{id}")
                    .permitAll()
                    .pathMatchers("/api-documentation/**", "/login")
                    .permitAll()

                    // Grouping paths that require ADMIN_ROLE
                    .pathMatchers(HttpMethod.POST, "/v1/dog")
                    .hasRole(ADMIN_ROLE)
                    .pathMatchers(HttpMethod.DELETE, "/v1/dog/{id}", "/v1/dog/image/{id}")
                    .hasRole(ADMIN_ROLE)
                    .pathMatchers(HttpMethod.PUT, "/v1/dog/{id}")
                    .hasRole(ADMIN_ROLE)

                    // Default rule for any other requests
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
            .roles(ADMIN_ROLE)
            .build();
    return new MapReactiveUserDetailsService(adminUser);
  }
}
