package com.example.bvt.repository;

import com.example.bvt.domain.TestPlanEntity;
import com.example.bvt.domain.enums.PlanStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TestPlanRepository extends JpaRepository<TestPlanEntity, Long> {

    List<TestPlanEntity> findByStatus(PlanStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from TestPlanEntity p where p.id = :id")
    Optional<TestPlanEntity> lockById(@Param("id") Long id);
}
