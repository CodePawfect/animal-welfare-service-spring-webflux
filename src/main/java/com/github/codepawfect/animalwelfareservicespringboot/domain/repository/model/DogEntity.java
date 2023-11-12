package com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model;

import java.util.UUID;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("t_dog")
@Getter
@Setter
@Builder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
public class DogEntity extends BaseEntity {

  @Id private UUID id;
  private String name;
  private String breed;
  private String description;
  private Integer age;
}
