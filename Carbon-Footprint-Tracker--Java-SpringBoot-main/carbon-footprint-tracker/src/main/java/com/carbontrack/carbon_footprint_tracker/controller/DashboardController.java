package com.carbontrack.carbon_footprint_tracker.controller;

import com.carbontrack.carbon_footprint_tracker.entity.CarbonFootprint;
import com.carbontrack.carbon_footprint_tracker.entity.ReductionGoal;
import com.carbontrack.carbon_footprint_tracker.entity.User;
import com.carbontrack.carbon_footprint_tracker.service.CarbonFootprintService;
import com.carbontrack.carbon_footprint_tracker.service.ReductionGoalService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Controller
public class DashboardController {

    private final CarbonFootprintService carbonFootprintService;
    private final ReductionGoalService reductionGoalService;

    public DashboardController(CarbonFootprintService carbonFootprintService, ReductionGoalService reductionGoalService) {
        this.carbonFootprintService = carbonFootprintService;
        this.reductionGoalService = reductionGoalService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal User user, Model model) {
        List<CarbonFootprint> recentFootprints = carbonFootprintService.getUserCarbonFootprints(user)
                .stream().limit(5).toList();

        YearMonth currentMonth = YearMonth.now();
        Double monthlyEmissions = carbonFootprintService.getMonthlyEmissions(user, currentMonth);

        Double averageEmissions = carbonFootprintService.getAverageEmissions(user);

        List<ReductionGoal> activeGoals = reductionGoalService.getActiveGoals(user);

        LocalDate startOfYear = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        LocalDate endOfYear = LocalDate.of(LocalDate.now().getYear(), 12, 31);
        Double yearlyEmissions = carbonFootprintService.getTotalEmissionsForPeriod(user, startOfYear, endOfYear);
        model.addAttribute("recentFootprints", recentFootprints);
        model.addAttribute("monthlyEmissions", monthlyEmissions);
        model.addAttribute("averageEmissions", averageEmissions);
        model.addAttribute("yearlyEmissions", yearlyEmissions);
        model.addAttribute("activeGoals", activeGoals);
        model.addAttribute("user", user);

        return "dashboard/index";
    }
}