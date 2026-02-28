package com.example.bvt.repository;

import com.example.bvt.domain.TestCaseResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestCaseResultRepository extends JpaRepository<TestCaseResultEntity, Long> {

    List<TestCaseResultEntity> findByRunIdOrderByIdAsc(Long runId);
}
