package com.github.codepawfect.animalwelfareservicespringboot.core.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public Mono<ResponseEntity<String>> handleException(Exception exception) {
    return Mono.just(ResponseEntity.badRequest().body(exception.getMessage()));
  }
}
