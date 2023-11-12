package com.github.codepawfect.animalwelfareservicespringboot.domain.repository;

import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model.DogImageEntity;
import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface DogImageRepository extends R2dbcRepository<DogImageEntity, UUID> {}
