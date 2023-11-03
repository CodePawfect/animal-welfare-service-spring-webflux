package com.github.codepawfect.animalwelfareservicespringboot.domain.repository;

import com.github.codepawfect.animalwelfareservicespringboot.domain.repository.model.ImageUriEntity;
import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface ImageUriRepository extends R2dbcRepository<ImageUriEntity, UUID> {}
