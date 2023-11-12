package com.github.codepawfect.animalwelfareservicespringboot.domain.controller.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Model to create a new dog")
public record DogCreateResource(
    @Schema(description = "Name of the dog", example = "Buddy")
    String name,
    @Schema(description = "Breed of the dog", example = "German Shepard Mix")
    String breed,
    @Schema(description = "Description of the dog", example = "He is a really good boy!")
    String description,
    @Schema(description = "Age of the dog", example = "4")
    Integer age){
}
