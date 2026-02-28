package com.example.bvt.repository;

import com.example.bvt.domain.TestSuiteCaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestSuiteCaseRepository extends JpaRepository<TestSuiteCaseEntity, Long> {

    List<TestSuiteCaseEntity> findBySuiteIdAndEnabledTrueOrderByExecOrderAscIdAsc(Long suiteId);
}
