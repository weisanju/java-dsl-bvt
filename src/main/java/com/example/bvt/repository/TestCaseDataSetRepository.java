package com.example.bvt.repository;

import com.example.bvt.domain.TestCaseDataSetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestCaseDataSetRepository extends JpaRepository<TestCaseDataSetEntity, Long> {

    List<TestCaseDataSetEntity> findByTestCaseIdOrderByIdAsc(Long caseId);
}
