package com.github.codepawfect.animalwelfareservicespringboot.core.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public Mono<ResponseEntity<String>> handleException(Exception exception) {
    log.error("An exception occurred:", exception);
    log.info("Exception message: {}", exception.getMessage());
    return Mono.just(ResponseEntity.badRequest().body(exception.getMessage()));
  }
}
