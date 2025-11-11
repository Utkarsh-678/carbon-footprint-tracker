package com.carbontrack.carbon_footprint_tracker.controller.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carbontrack.carbon_footprint_tracker.entity.ReductionGoal;
import com.carbontrack.carbon_footprint_tracker.entity.User;
import com.carbontrack.carbon_footprint_tracker.service.ReductionGoalService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/goals")
@CrossOrigin(origins = "http://localhost:4200")
public class GoalsRestController {

    private static final Logger log = LoggerFactory.getLogger(GoalsRestController.class);
    private final ReductionGoalService reductionGoalService;

    public GoalsRestController(ReductionGoalService reductionGoalService) {
        this.reductionGoalService = reductionGoalService;
    }

    @GetMapping
    public ResponseEntity<List<ReductionGoal>> getUserGoals(@AuthenticationPrincipal User user) {
        try {
            List<ReductionGoal> goals = reductionGoalService.getUserGoals(user);
            return ResponseEntity.ok(goals);
        } catch (Exception e) {
            log.error("Error fetching goals for user: {}", user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/active")
    public ResponseEntity<List<ReductionGoal>> getActiveGoals(@AuthenticationPrincipal User user) {
        try {
            List<ReductionGoal> activeGoals = reductionGoalService.getActiveGoals(user);
            return ResponseEntity.ok(activeGoals);
        } catch (Exception e) {
            log.error("Error fetching active goals for user: {}", user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReductionGoal> getGoal(@PathVariable Long id, @AuthenticationPrincipal User user) {
        try {
            Optional<ReductionGoal> goal = reductionGoalService.findById(id);
            if (goal.isEmpty() || !goal.get().getUser().getId().equals(user.getId())) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(goal.get());
        } catch (Exception e) {
            log.error("Error fetching goal with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createGoal(@Valid @RequestBody ReductionGoal goal, 
                                      @AuthenticationPrincipal User user) {
        try {
            goal.setUser(user);
            ReductionGoal savedGoal = reductionGoalService.saveReductionGoal(goal);
            log.info("Goal created successfully for user: {}", user.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedGoal);
        } catch (Exception e) {
            log.error("Error creating goal for user: {}", user.getUsername(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to create goal: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateGoal(@PathVariable Long id,
                                      @Valid @RequestBody ReductionGoal goal,
                                      @AuthenticationPrincipal User user) {
        try {
            Optional<ReductionGoal> existingGoal = reductionGoalService.findById(id);
            if (existingGoal.isEmpty() || !existingGoal.get().getUser().getId().equals(user.getId())) {
                return ResponseEntity.notFound().build();
            }

            goal.setId(id);
            goal.setUser(user);
            goal.setCreatedAt(existingGoal.get().getCreatedAt());
            goal.setCurrentReduction(existingGoal.get().getCurrentReduction());
            ReductionGoal updatedGoal = reductionGoalService.saveReductionGoal(goal);
            
            log.info("Goal updated successfully for user: {}", user.getUsername());
            return ResponseEntity.ok(updatedGoal);
        } catch (Exception e) {
            log.error("Error updating goal with id: {}", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to update goal: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGoal(@PathVariable Long id, @AuthenticationPrincipal User user) {
        try {
            Optional<ReductionGoal> goal = reductionGoalService.findById(id);
            if (goal.isEmpty() || !goal.get().getUser().getId().equals(user.getId())) {
                return ResponseEntity.notFound().build();
            }

            reductionGoalService.deleteGoal(id);
            log.info("Goal deleted successfully for user: {}", user.getUsername());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Goal deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting goal with id: {}", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to delete goal: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleGoalStatus(@PathVariable Long id, @AuthenticationPrincipal User user) {
        try {
            Optional<ReductionGoal> goalOpt = reductionGoalService.findById(id);
            if (goalOpt.isEmpty() || !goalOpt.get().getUser().getId().equals(user.getId())) {
                return ResponseEntity.notFound().build();
            }

            ReductionGoal goal = goalOpt.get();
            if (goal.getStatus() == ReductionGoal.Status.ACTIVE) {
                goal.setStatus(ReductionGoal.Status.PAUSED);
            } else if (goal.getStatus() == ReductionGoal.Status.PAUSED) {
                goal.setStatus(ReductionGoal.Status.ACTIVE);
            }
            
            ReductionGoal updatedGoal = reductionGoalService.saveReductionGoal(goal);
            log.info("Goal status toggled successfully for user: {}", user.getUsername());
            
            return ResponseEntity.ok(updatedGoal);
        } catch (Exception e) {
            log.error("Error toggling goal status with id: {}", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to toggle goal status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}