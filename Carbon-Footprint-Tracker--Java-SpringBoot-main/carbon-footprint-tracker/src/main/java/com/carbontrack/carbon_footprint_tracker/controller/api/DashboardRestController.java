package com.carbontrack.carbon_footprint_tracker.controller.api;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carbontrack.carbon_footprint_tracker.dto.DashboardStatsDto;
import com.carbontrack.carbon_footprint_tracker.entity.CarbonFootprint;
import com.carbontrack.carbon_footprint_tracker.entity.ReductionGoal;
import com.carbontrack.carbon_footprint_tracker.entity.User;
import com.carbontrack.carbon_footprint_tracker.service.CarbonFootprintService;
import com.carbontrack.carbon_footprint_tracker.service.ReductionGoalService;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:4200")
public class DashboardRestController {

    private static final Logger log = LoggerFactory.getLogger(DashboardRestController.class);
    
    private final CarbonFootprintService carbonFootprintService;
    private final ReductionGoalService reductionGoalService;

    public DashboardRestController(CarbonFootprintService carbonFootprintService, 
                                 ReductionGoalService reductionGoalService) {
        this.carbonFootprintService = carbonFootprintService;
        this.reductionGoalService = reductionGoalService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getDashboardStats(@AuthenticationPrincipal User user) {
        try {
            List<CarbonFootprint> recentFootprints = carbonFootprintService.getUserCarbonFootprints(user).stream().limit(5).toList();
            YearMonth currentMonth = YearMonth.now();
            Double monthlyEmissions = carbonFootprintService.getMonthlyEmissions(user, currentMonth);
            Double averageEmissions = carbonFootprintService.getAverageEmissions(user);
            LocalDate startOfYear = LocalDate.of(LocalDate.now().getYear(), 1, 1);
            LocalDate endOfYear = LocalDate.of(LocalDate.now().getYear(), 12, 31);
            Double yearlyEmissions = carbonFootprintService.getTotalEmissionsForPeriod(user, startOfYear, endOfYear);
            List<ReductionGoal> activeGoals = reductionGoalService.getActiveGoals(user);

            DashboardStatsDto stats = new DashboardStatsDto(
                recentFootprints,
                monthlyEmissions,
                averageEmissions,
                yearlyEmissions,
                activeGoals
            );

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching dashboard stats for user: {}", user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}