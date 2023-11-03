package com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model;

import java.util.UUID;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("t_image_uri")
@Getter
@Setter
@Builder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
public class ImageUriEntity extends BaseEntity {

  @Id private UUID id;
  private UUID dogId;
  private String uri;
}
