package com.github.codepawfect.animalwelfareservicespringboot.domain.repository;

import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model.DogEntity;
import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface DogRepository extends R2dbcRepository<DogEntity, UUID> {}
