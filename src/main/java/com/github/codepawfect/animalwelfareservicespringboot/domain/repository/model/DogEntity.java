package com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("t_dog")
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class DogEntity extends BaseEntity {

  @Id
  private UUID id;
  private String name;
  private String breed;
  private Integer age;
}
