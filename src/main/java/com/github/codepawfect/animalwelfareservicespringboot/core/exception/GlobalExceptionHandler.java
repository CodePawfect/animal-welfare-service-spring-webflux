package com.github.codepawfect.animalwelfareservicespringboot.core.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public Mono<ResponseEntity<Object>> handleIllegalArgumentException(
      IllegalArgumentException exception) {
    log.warn("Illegal argument exception: {}", exception.getMessage());
    return Mono.just(ResponseEntity.badRequest().body(exception.getMessage()));
  }

  @ExceptionHandler(ResponseStatusException.class)
  public Mono<ResponseEntity<Object>> handleResponseStatusException(Exception exception) {
    log.error("An unexpected error occurred: {}", exception.getMessage(), exception);
    return Mono.just(
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(exception.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public Mono<ResponseEntity<Object>> handleGeneralException(Exception exception) {
    log.error("An unexpected error occurred: {}", exception.getMessage(), exception);
    return Mono.just(
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage()));
  }
}
