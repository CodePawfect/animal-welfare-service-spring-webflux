package com.github.codepawfect.animalwelfareservicespringboot.domain.controller.model;

import java.util.List;
import java.util.UUID;

public record DogResource(
    UUID id, String name, String breed, String description, Integer age, List<String> imageUris) {}
