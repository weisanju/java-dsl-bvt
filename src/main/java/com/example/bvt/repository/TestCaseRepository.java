package com.example.bvt.repository;

import com.example.bvt.domain.TestCaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TestCaseRepository extends JpaRepository<TestCaseEntity, Long> {

    List<TestCaseEntity> findByProjectIdOrderByIdAsc(Long projectId);

    Optional<TestCaseEntity> findByProjectIdAndCaseCode(Long projectId, String caseCode);
}
