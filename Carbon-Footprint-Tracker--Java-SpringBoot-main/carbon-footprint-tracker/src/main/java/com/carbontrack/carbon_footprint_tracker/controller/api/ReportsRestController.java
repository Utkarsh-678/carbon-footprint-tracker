package com.carbontrack.carbon_footprint_tracker.controller.api;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.carbontrack.carbon_footprint_tracker.entity.CarbonFootprint;
import com.carbontrack.carbon_footprint_tracker.entity.User;
import com.carbontrack.carbon_footprint_tracker.service.CarbonFootprintService;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "http://localhost:4200")
public class ReportsRestController {

    private static final Logger log = LoggerFactory.getLogger(ReportsRestController.class);
    private final CarbonFootprintService carbonFootprintService;

    public ReportsRestController(CarbonFootprintService carbonFootprintService) {
        this.carbonFootprintService = carbonFootprintService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getReports(@AuthenticationPrincipal User user,
                                                         @RequestParam(defaultValue = "monthly") String period) {
        try {
            List<CarbonFootprint> footprints = carbonFootprintService.getUserCarbonFootprints(user);
            Map<String, Object> reportData;

            switch (period) {
                case "monthly" -> reportData = generateMonthlyReport(footprints);
                case "yearly" -> reportData = generateYearlyReport(footprints);
                case "weekly" -> reportData = generateWeeklyReport(footprints);
                default -> reportData = generateMonthlyReport(footprints);
            }

            reportData.put("period", period);
            reportData.put("totalEntries", footprints.size());

            return ResponseEntity.ok(reportData);
        } catch (Exception e) {
            log.error("Error generating reports for user: {}", user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Map<String, Object> generateMonthlyReport(List<CarbonFootprint> footprints) {
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

        Map<String, Object> result = new HashMap<>();
        result.put("chartLabels", months);
        result.put("chartData", emissions);
        result.put("reportTitle", "Monthly Carbon Emissions Report");
        return result;
    }

    private Map<String, Object> generateYearlyReport(List<CarbonFootprint> footprints) {
        Map<Integer, List<CarbonFootprint>> yearlyData = footprints.stream()
                .collect(Collectors.groupingBy(f -> f.getDate().getYear()));

        List<String> years = new ArrayList<>();
        List<Double> emissions = new ArrayList<>();

        yearlyData.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    years.add(entry.getKey().toString());
                    double totalEmissions = entry.getValue().stream()
                            .mapToDouble(CarbonFootprint::getTotalEmissions)
                            .sum();
                    emissions.add(totalEmissions);
                });

        Map<String, Object> result = new HashMap<>();
        result.put("chartLabels", years);
        result.put("chartData", emissions);
        result.put("reportTitle", "Yearly Carbon Emissions Report");
        return result;
    }

    private Map<String, Object> generateWeeklyReport(List<CarbonFootprint> footprints) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusWeeks(8);

        List<CarbonFootprint> recentFootprints = footprints.stream()
                .filter(f -> f.getDate().isAfter(startDate) && f.getDate().isBefore(endDate.plusDays(1)))
                .collect(Collectors.toList());

        Map<String, List<CarbonFootprint>> weeklyData = recentFootprints.stream()
                .collect(Collectors.groupingBy(f -> {
                    long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, f.getDate());
                    int weekNumber = (int) (daysBetween / 7) + 1;
                    return "Week " + weekNumber;
                }));

        List<String> weeks = new ArrayList<>();
        List<Double> emissions = new ArrayList<>();
        weeklyData.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    weeks.add(entry.getKey());
                    double totalEmissions = entry.getValue().stream()
                            .mapToDouble(CarbonFootprint::getTotalEmissions)
                            .sum();
                    emissions.add(totalEmissions);
                });

        Map<String, Object> result = new HashMap<>();
        result.put("chartLabels", weeks);
        result.put("chartData", emissions);
        result.put("reportTitle", "Weekly Carbon Emissions Report (Last 8 Weeks)");
        return result;
    }
}