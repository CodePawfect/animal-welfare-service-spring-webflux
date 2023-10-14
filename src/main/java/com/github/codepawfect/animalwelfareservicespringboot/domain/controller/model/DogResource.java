package com.github.codepawfect.animalwelfareservicespringboot.domain.controller.model;

import java.util.UUID;

public record DogResource(UUID id, String name, String breed, Integer age) {}
