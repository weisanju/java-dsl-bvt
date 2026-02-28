package com.example.bvt.repository;

import com.example.bvt.domain.TestEnvironmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TestEnvironmentRepository extends JpaRepository<TestEnvironmentEntity, Long> {
    List<TestEnvironmentEntity> findByProjectIdOrderByIdAsc(Long projectId);

    Optional<TestEnvironmentEntity> findByProjectIdAndEnvCode(Long projectId, String envCode);
}
