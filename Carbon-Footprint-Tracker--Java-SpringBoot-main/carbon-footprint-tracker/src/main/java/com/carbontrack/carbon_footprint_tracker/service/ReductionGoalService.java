package com.carbontrack.carbon_footprint_tracker.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carbontrack.carbon_footprint_tracker.entity.ReductionGoal;
import com.carbontrack.carbon_footprint_tracker.entity.User;
import com.carbontrack.carbon_footprint_tracker.repository.ReductionGoalRepository;

@Service
@Transactional
public class ReductionGoalService {

    private final ReductionGoalRepository reductionGoalRepository;

    // Constructor - no @Autowired needed for single constructor
    public ReductionGoalService(ReductionGoalRepository reductionGoalRepository) {
        this.reductionGoalRepository = reductionGoalRepository;
    }

    public ReductionGoal saveReductionGoal(ReductionGoal goal) {
        return reductionGoalRepository.save(goal);
    }

    public List<ReductionGoal> getUserGoals(User user) {
        return reductionGoalRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<ReductionGoal> getActiveGoals(User user) {
        return reductionGoalRepository.findByUserAndStatus(user, ReductionGoal.Status.ACTIVE);
    }

    public Optional<ReductionGoal> findById(Long id) {
        return reductionGoalRepository.findById(id);
    }

    public void deleteGoal(Long id) {
        reductionGoalRepository.deleteById(id);
    }

    public void updateGoalProgress(User user, double reductionAmount) {
        List<ReductionGoal> activeGoals = getActiveGoals(user);
        for (ReductionGoal goal : activeGoals) {
            goal.setCurrentReduction(goal.getCurrentReduction() + reductionAmount);
            if (goal.getCurrentReduction() >= goal.getTargetReduction()) {
                goal.setStatus(ReductionGoal.Status.COMPLETED);
            }
            saveReductionGoal(goal);
        }
    }

    public List<ReductionGoal> getOverdueGoals(User user) {
        return reductionGoalRepository.findByUserAndTargetDateBefore(user, LocalDate.now());
    }
}