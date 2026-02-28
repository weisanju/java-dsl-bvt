package com.example.bvt.service;

import com.example.bvt.domain.TestPlanEntity;
import com.example.bvt.domain.enums.PlanStatus;
import com.example.bvt.repository.TestPlanRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Component
public class PlanScheduler {

    private final TestPlanRepository planRepository;
    private final RunService runService;

    public PlanScheduler(TestPlanRepository planRepository, RunService runService) {
        this.planRepository = planRepository;
        this.runService = runService;
    }

    @Scheduled(fixedDelay = 60000)
    public void schedule() {
        List<TestPlanEntity> enabledPlans = planRepository.findByStatus(PlanStatus.ENABLED);
        Instant now = Instant.now();
        for (TestPlanEntity plan : enabledPlans) {
            try {
                if (shouldRun(plan, now) && lockAndMarkRun(plan.getId(), now)) {
                    runService.triggerByPlan(plan.getSuite().getId(), plan.getEnvironment().getId());
                }
            } catch (Exception ignored) {
                // Keep scheduler loop alive for other plans.
            }
        }
    }

    private boolean shouldRun(TestPlanEntity plan, Instant now) {
        CronExpression cron = CronExpression.parse(plan.getCronExpr());
        ZonedDateTime base = (plan.getLastRunAt() == null ? now.minusSeconds(60) : plan.getLastRunAt())
                .atZone(ZoneId.systemDefault());
        ZonedDateTime next = cron.next(base);
        return next != null && !next.toInstant().isAfter(now);
    }

    @Transactional
    public boolean lockAndMarkRun(Long planId, Instant now) {
        TestPlanEntity locked = planRepository.lockById(planId).orElse(null);
        if (locked == null || locked.getStatus() != PlanStatus.ENABLED) {
            return false;
        }
        if (locked.getLastRunAt() != null && now.minusSeconds(30).isBefore(locked.getLastRunAt())) {
            return false;
        }
        locked.setLastRunAt(now);
        planRepository.save(locked);
        return true;
    }
}
