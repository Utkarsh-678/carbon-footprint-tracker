package com.carbontrack.carbon_footprint_tracker.controller;

import com.carbontrack.carbon_footprint_tracker.entity.CarbonFootprint;
import com.carbontrack.carbon_footprint_tracker.entity.User;
import com.carbontrack.carbon_footprint_tracker.service.CarbonFootprintService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reports")
public class ReportsController {

    private final CarbonFootprintService carbonFootprintService;

    public ReportsController(CarbonFootprintService carbonFootprintService) {
        this.carbonFootprintService = carbonFootprintService;
    }

    @GetMapping
    public String reports(@AuthenticationPrincipal User user,
                         @RequestParam(defaultValue = "monthly") String period,
                         Model model) {
        
        List<CarbonFootprint> footprints = carbonFootprintService.getUserCarbonFootprints(user);
        
        // Convert to rule switch (modern Java syntax)
        switch (period) {
            case "monthly" -> generateMonthlyReport(model, footprints);
            case "yearly" -> generateYearlyReport(model, footprints);
            case "weekly" -> generateWeeklyReport(model, footprints);
            default -> generateMonthlyReport(model, footprints); // Default to monthly
        }
        
        model.addAttribute("period", period);
        model.addAttribute("totalEntries", footprints.size());
        
        return "reports/index";
    }

    // Removed unused 'user' parameter
//    private void generateMonthlyReport(Model model, List<CarbonFootprint> footprints) {
//        Map<YearMonth, List<CarbonFootprint>> monthlyData = footprints.stream()
//                .collect(Collectors.groupingBy(f -> YearMonth.from(f.getDate())));
//
//        List<String> months = new ArrayList<>();
//        List<Double> emissions = new ArrayList<>();
//
//        monthlyData.entrySet().stream()
//                .sorted(Map.Entry.<YearMonth, List<CarbonFootprint>>comparingByKey().reversed())
//                .limit(12)
//                .forEach(entry -> {
//                    months.add(entry.getKey().format(DateTimeFormatter.ofPattern("MMM yyyy")));
//                    double totalEmissions = entry.getValue().stream()
//                            .mapToDouble(CarbonFootprint::getTotalEmissions)
//                            .sum();
//                    emissions.add(totalEmissions);
//                });
//
//        Collections.reverse(months);
//        Collections.reverse(emissions);
//
//        model.addAttribute("chartLabels", months);
//        model.addAttribute("chartData", emissions);
//        model.addAttribute("reportTitle", "Monthly Carbon Emissions Report");
//    }
    private void generateMonthlyReport(Model model, List<CarbonFootprint> footprints) {
        Map<YearMonth, List<CarbonFootprint>> monthlyData = footprints.stream()
                .collect(Collectors.groupingBy(f -> YearMonth.from(f.getDate())));

        List<String> months = new ArrayList<>();
        List<Double> emissions = new ArrayList<>();

        monthlyData.entrySet().stream()
                .sorted(Map.Entry.<YearMonth, List<CarbonFootprint>>comparingByKey().reversed())
                .limit(12)
                .forEach(entry -> {
                    months.add(entry.getKey().format(DateTimeFormatter.ofPattern("MMM yyyy")));
                    double totalEmissions = entry.getValue().stream()
                            .mapToDouble(CarbonFootprint::getTotalEmissions)
                            .sum();
                    emissions.add(totalEmissions);
                });

        Collections.reverse(months);
        Collections.reverse(emissions);

        // Compute average here
        double average = emissions.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);

        model.addAttribute("chartLabels", months);
        model.addAttribute("chartData", emissions);
        model.addAttribute("average", average); // <-- new
        model.addAttribute("reportTitle", "Monthly Carbon Emissions Report");
    }


    // Removed unused 'user' parameter
    private void generateYearlyReport(Model model, List<CarbonFootprint> footprints) {
        Map<Integer, List<CarbonFootprint>> yearlyData = footprints.stream()
                .collect(Collectors.groupingBy(f -> f.getDate().getYear()));

        List<String> years = new ArrayList<>();
        List<Double> emissions = new ArrayList<>();

        yearlyData.entrySet().stream()
                .sorted(Map.Entry.<Integer, List<CarbonFootprint>>comparingByKey())
                .forEach(entry -> {
                    years.add(entry.getKey().toString());
                    double totalEmissions = entry.getValue().stream()
                            .mapToDouble(CarbonFootprint::getTotalEmissions)
                            .sum();
                    emissions.add(totalEmissions);
                });

        model.addAttribute("chartLabels", years);
        model.addAttribute("chartData", emissions);
        model.addAttribute("reportTitle", "Yearly Carbon Emissions Report");
    }

    // Removed unused 'user' parameter
    private void generateWeeklyReport(Model model, List<CarbonFootprint> footprints) {
        // Get last 8 weeks of data
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusWeeks(8);

        List<CarbonFootprint> recentFootprints = footprints.stream()
                .filter(f -> f.getDate().isAfter(startDate) && f.getDate().isBefore(endDate.plusDays(1)))
                .collect(Collectors.toList());

        // Group by week
        Map<String, List<CarbonFootprint>> weeklyData = recentFootprints.stream()
                .collect(Collectors.groupingBy(f -> {
                    // Calculate week number from start date
                    long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, f.getDate());
                    int weekNumber = (int) (daysBetween / 7) + 1;
                    return "Week " + weekNumber;
                }));

        List<String> weeks = new ArrayList<>();
        List<Double> emissions = new ArrayList<>();

        // Sort weeks and calculate emissions
        weeklyData.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    weeks.add(entry.getKey());
                    double totalEmissions = entry.getValue().stream()
                            .mapToDouble(CarbonFootprint::getTotalEmissions)
                            .sum();
                    emissions.add(totalEmissions);
                });

        model.addAttribute("chartLabels", weeks);
        model.addAttribute("chartData", emissions);
        model.addAttribute("reportTitle", "Weekly Carbon Emissions Report (Last 8 Weeks)");
    }
}