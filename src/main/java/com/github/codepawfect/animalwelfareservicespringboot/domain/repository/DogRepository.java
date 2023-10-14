package com.github.codepawfect.animalwelfareservicespringboot.domain.repository;

import java.util.UUID;
import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model.DogEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface DogRepository extends R2dbcRepository<DogEntity, UUID> {
}
