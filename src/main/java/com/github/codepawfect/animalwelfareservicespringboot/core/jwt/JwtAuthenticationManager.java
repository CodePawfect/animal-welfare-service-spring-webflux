package com.github.codepawfect.animalwelfareservicespringboot.core.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {
  private final JwtService jwtService;
  private final ReactiveUserDetailsService reactiveUserDetailsService;

  @Override
  public Mono<Authentication> authenticate(Authentication authentication) {
    return Mono.just(authentication)
        .map(Authentication::getCredentials)
        .cast(String.class)
        .filter(token -> !jwtService.isExpired(token))
        .flatMap(
            token ->
                Mono.justOrEmpty(jwtService.getUsername(token))
                    .flatMap(
                        username ->
                            reactiveUserDetailsService
                                .findByUsername(username)
                                .map(
                                    userDetails ->
                                        new UsernamePasswordAuthenticationToken(
                                            userDetails.getUsername(),
                                            userDetails.getPassword(),
                                            userDetails.getAuthorities())))
                    .cast(Authentication.class))
        .onErrorResume(e -> Mono.error(new BadCredentialsException("invalid token")));
  }
}
