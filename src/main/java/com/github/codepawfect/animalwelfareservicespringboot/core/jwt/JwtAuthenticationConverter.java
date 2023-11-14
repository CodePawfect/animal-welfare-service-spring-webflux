package com.github.codepawfect.animalwelfareservicespringboot.core.jwt;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationConverter implements ServerAuthenticationConverter {
  @Override
  public Mono<Authentication> convert(ServerWebExchange exchange) {
    return Mono.justOrEmpty(exchange)
        .flatMap(
            e -> Mono.justOrEmpty(e.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION)))
        .filter(authHeader -> authHeader.startsWith("Bearer "))
        .map(authHeader -> authHeader.substring(7))
        .map(JwtAuthenticationToken::new);
  }
}
