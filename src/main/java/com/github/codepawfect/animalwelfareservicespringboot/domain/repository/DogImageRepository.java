package com.github.codepawfect.animalwelfareservicespringboot.domain.repository;

import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model.DogImageEntity;
import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface DogImageRepository extends R2dbcRepository<DogImageEntity, UUID> {
  Flux<DogImageEntity> findAllByDogId(UUID dogId);
}
