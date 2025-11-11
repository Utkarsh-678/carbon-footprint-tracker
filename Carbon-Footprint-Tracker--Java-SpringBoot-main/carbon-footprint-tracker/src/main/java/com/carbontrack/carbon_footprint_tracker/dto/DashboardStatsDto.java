package com.carbontrack.carbon_footprint_tracker.dto;

import java.util.List;

import com.carbontrack.carbon_footprint_tracker.entity.CarbonFootprint;
import com.carbontrack.carbon_footprint_tracker.entity.ReductionGoal;

public class DashboardStatsDto {
    
    private List<CarbonFootprint> recentFootprints;
    private Double monthlyEmissions;
    private Double averageEmissions;
    private Double yearlyEmissions;
    private List<ReductionGoal> activeGoals;

    public DashboardStatsDto() {}

    public DashboardStatsDto(List<CarbonFootprint> recentFootprints, 
                           Double monthlyEmissions, 
                           Double averageEmissions, 
                           Double yearlyEmissions, 
                           List<ReductionGoal> activeGoals) {
        this.recentFootprints = recentFootprints;
        this.monthlyEmissions = monthlyEmissions;
        this.averageEmissions = averageEmissions;
        this.yearlyEmissions = yearlyEmissions;
        this.activeGoals = activeGoals;
    }
    public List<CarbonFootprint> getRecentFootprints() {
        return recentFootprints;
    }

    public void setRecentFootprints(List<CarbonFootprint> recentFootprints) {
        this.recentFootprints = recentFootprints;
    }

    public Double getMonthlyEmissions() {
        return monthlyEmissions;
    }

    public void setMonthlyEmissions(Double monthlyEmissions) {
        this.monthlyEmissions = monthlyEmissions;
    }

    public Double getAverageEmissions() {
        return averageEmissions;
    }

    public void setAverageEmissions(Double averageEmissions) {
        this.averageEmissions = averageEmissions;
    }

    public Double getYearlyEmissions() {
        return yearlyEmissions;
    }

    public void setYearlyEmissions(Double yearlyEmissions) {
        this.yearlyEmissions = yearlyEmissions;
    }

    public List<ReductionGoal> getActiveGoals() {
        return activeGoals;
    }

    public void setActiveGoals(List<ReductionGoal> activeGoals) {
        this.activeGoals = activeGoals;
    }
}