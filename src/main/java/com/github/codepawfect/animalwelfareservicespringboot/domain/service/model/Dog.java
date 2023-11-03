package com.github.codepawfect.animalwelfareservicespringboot.domain.service.model;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Builder(toBuilder = true)
@Data
public class Dog {
  private UUID id;
  private String name;
  private String breed;
  private Integer age;
  private List<String> imageUris;
}
