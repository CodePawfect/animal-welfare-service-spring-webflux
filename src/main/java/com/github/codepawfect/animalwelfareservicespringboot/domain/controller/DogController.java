package com.github.codepawfect.animalwelfareservicespringboot.domain.controller;

import com.github.codepawfect.animalwelfareservicespringboot.domain.controller.mapper.DogResourceMapper;
import com.github.codepawfect.animalwelfareservicespringboot.domain.controller.model.DogCreateResource;
import com.github.codepawfect.animalwelfareservicespringboot.domain.controller.model.DogResource;
import com.github.codepawfect.animalwelfareservicespringboot.domain.controller.model.DogResources;
import com.github.codepawfect.animalwelfareservicespringboot.domain.service.DogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
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
  @PostMapping(value = "/v1/dog", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Mono<ResponseEntity<DogResource>> addDog(
      @RequestPart("dogCreateResource") DogCreateResource dogCreateResource,
      @RequestPart("files")
          @Parameter(description = "Dog Images to be uploaded. Must be JPEG or PNG.")
          Flux<FilePart> filePartFlux) {
    return dogService
        .addDog(dogResourceMapper.map(dogCreateResource), filePartFlux)
        .map(dog -> ResponseEntity.status(HttpStatus.CREATED).body(dogResourceMapper.map(dog)));
  }

  @Operation(summary = "Delete a dog", description = "Delete a dog", tags = "dog")
  @DeleteMapping(value = "/v1/dog/{id}")
  public Mono<ResponseEntity<Void>> deleteDog(@PathVariable String id) {
    return dogService.deleteDog(id).then(Mono.just(ResponseEntity.noContent().build()));
  }

  @Operation(
      summary = "Delete a dog related Image",
      description = "Delete a dog related Image",
      tags = "dog-image")
  @DeleteMapping(value = "/v1/dog/image/{id}")
  public Mono<ResponseEntity<Void>> deleteDogImage(@PathVariable String id) {
    return dogService.deleteDogImage(id).then(Mono.just(ResponseEntity.noContent().build()));
  }

  @Operation(summary = "Update a dog", description = "Update a dog", tags = "dog")
  @PutMapping("/v1/dog/{id}")
  public Mono<ResponseEntity<DogResource>> updateDog(
      @PathVariable String id,
      @RequestPart("dogCreateResource") DogCreateResource dogCreateResource,
      @RequestPart("files") Flux<FilePart> filePartFlux) {
    return null;
  }
}
