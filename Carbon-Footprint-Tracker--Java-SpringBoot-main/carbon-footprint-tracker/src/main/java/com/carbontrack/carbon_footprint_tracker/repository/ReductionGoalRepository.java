package com.carbontrack.carbon_footprint_tracker.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.carbontrack.carbon_footprint_tracker.entity.ReductionGoal;
import com.carbontrack.carbon_footprint_tracker.entity.User;

@Repository
public interface ReductionGoalRepository extends JpaRepository<ReductionGoal, Long> {
    List<ReductionGoal> findByUserOrderByCreatedAtDesc(User user);
    List<ReductionGoal> findByUserAndStatus(User user, ReductionGoal.Status status);
    List<ReductionGoal> findByUserAndTargetDateBefore(User user, LocalDate date);
}
