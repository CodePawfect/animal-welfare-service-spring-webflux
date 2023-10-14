package com.github.codepawfect.animalwelfareservicespringboot.domain.controller;

import com.github.codepawfect.animalwelfareservicespringboot.domain.controller.mapper.DogGetResourceMapper;
import com.github.codepawfect.animalwelfareservicespringboot.domain.controller.model.DogResources;
import com.github.codepawfect.animalwelfareservicespringboot.domain.service.DogService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
public class DogController {

  private final DogService dogService;
  private final DogGetResourceMapper dogGetResourceMapper;

  @Operation(
      summary = "Get all dogs",
      description = "Returns a list of all dogs",
      tags = "dog")
  @GetMapping("/v1/dogs")
    public Mono<ResponseEntity<DogResources>> getDogs() {
    return dogService.getDogs()
        .collectList()
        .map(dogGetResourceMapper::map)
        .map(ResponseEntity::ok);
    }
}
