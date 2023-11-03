package com.github.codepawfect.animalwelfareservicespringboot.domain.controller;

import com.github.codepawfect.animalwelfareservicespringboot.domain.controller.mapper.DogResourceMapper;
import com.github.codepawfect.animalwelfareservicespringboot.domain.controller.model.DogResource;
import com.github.codepawfect.animalwelfareservicespringboot.domain.controller.model.DogResources;
import com.github.codepawfect.animalwelfareservicespringboot.domain.service.DogService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
public class DogController {

  private final DogService dogService;
  private final DogResourceMapper dogResourceMapper;

  @Operation(summary = "Get all dogs", description = "Returns a list of all dogs", tags = "dog")
  @GetMapping("/v1/dogs")
  public Mono<ResponseEntity<DogResources>> getDogs() {
    return dogService.getDogs().collectList().map(dogResourceMapper::map).map(ResponseEntity::ok);
  }

  @Operation(summary = "Get a single dog", description = "Returns a single dog", tags = "dog")
  @GetMapping("/v1/dog/{id}")
  public Mono<ResponseEntity<DogResource>> getDog(@PathVariable String id) {
    return dogService.getDog(id).map(dogResourceMapper::map).map(ResponseEntity::ok);
  }

  @Operation(summary = "Create a new dog", description = "Create a new dog", tags = "dog")
  @PostMapping(
      value = "/v1/dog",
      consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public Mono<ResponseEntity<DogResource>> addDog(
      @RequestPart("dog") DogResource dogResource,
      @RequestPart("files") List<MultipartFile> files) {
    return dogService
        .addDog(dogResourceMapper.map(dogResource), files)
        .map(dog -> ResponseEntity.status(HttpStatus.CREATED).body(dogResourceMapper.map(dog)));
  }
}
