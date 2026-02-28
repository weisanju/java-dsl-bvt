package com.example.bvt.repository;

import com.example.bvt.domain.TestSuiteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TestSuiteRepository extends JpaRepository<TestSuiteEntity, Long> {

    List<TestSuiteEntity> findByProjectIdOrderByIdAsc(Long projectId);

    Optional<TestSuiteEntity> findByProjectIdAndSuiteCode(Long projectId, String suiteCode);
}
