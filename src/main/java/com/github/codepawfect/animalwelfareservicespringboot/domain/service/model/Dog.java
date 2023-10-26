package com.github.codepawfect.animalwelfareservicespringboot.domain.service.model;

import java.util.UUID;

public record Dog(UUID id, String name, String breed, Integer age) {}
