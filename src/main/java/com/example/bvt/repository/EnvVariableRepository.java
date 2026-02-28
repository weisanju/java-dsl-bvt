package com.example.bvt.repository;

import com.example.bvt.domain.EnvVariableEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnvVariableRepository extends JpaRepository<EnvVariableEntity, Long> {

    List<EnvVariableEntity> findByProjectIdAndEnvironmentIsNullOrderByIdAsc(Long projectId);

    List<EnvVariableEntity> findByProjectIdAndEnvironmentIdOrderByIdAsc(Long projectId, Long environmentId);

    Optional<EnvVariableEntity> findByProjectIdAndEnvironmentIdAndVarKey(Long projectId, Long environmentId, String varKey);

    Optional<EnvVariableEntity> findByProjectIdAndEnvironmentIsNullAndVarKey(Long projectId, String varKey);
}
