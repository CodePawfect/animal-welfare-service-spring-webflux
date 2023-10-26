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
  private Integer age;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    DogEntity dogEntity = (DogEntity) o;

    if (!id.equals(dogEntity.id)) return false;
    if (!name.equals(dogEntity.name)) return false;
    if (!breed.equals(dogEntity.breed)) return false;
    return age.equals(dogEntity.age);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + id.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + breed.hashCode();
    result = 31 * result + age.hashCode();
    return result;
  }
}
