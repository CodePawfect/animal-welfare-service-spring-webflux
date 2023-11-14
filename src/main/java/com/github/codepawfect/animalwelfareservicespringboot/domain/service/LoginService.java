package com.github.codepawfect.animalwelfareservicespringboot.domain.service;

import com.github.codepawfect.animalwelfareservicespringboot.core.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class LoginService {
  private final ReactiveUserDetailsService reactiveUserDetailsService;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;

  public Mono<String> authenticate(String username, String password) {
    return reactiveUserDetailsService
        .findByUsername(username)
        .flatMap(
            userDetails -> {
              if (passwordEncoder.matches(password, userDetails.getPassword())) {
                return Mono.just(userDetails);
              } else {
                return Mono.error(new BadCredentialsException("Invalid username or password"));
              }
            })
        .map(userDetails -> jwtService.generateToken(userDetails.getUsername()));
  }
}
