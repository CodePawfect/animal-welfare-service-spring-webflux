package com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model;

import java.util.UUID;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("t_dog_image")
@Getter
@Setter
@Builder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
public class DogImageEntity extends BaseEntity {

  @Id private UUID id;
  private UUID dogId;
  private String uri;
}
