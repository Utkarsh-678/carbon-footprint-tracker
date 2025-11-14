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
import java.time.temporal.IsoFields;
import java.util.*;
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

        List<CarbonFootprint> footprints =
                carbonFootprintService.getUserCarbonFootprints(user);

        switch (period) {
            case "weekly" -> generateWeeklyReport(model, footprints);
            case "yearly" -> generateYearlyReport(model, footprints);
            default -> generateMonthlyReport(model, footprints);
        }

        model.addAttribute("period", period);
        model.addAttribute("totalEntries", footprints.size());

        System.out.println("FOOTPRINTS SIZE = " + footprints.size());


        return "reports/index";
    }

    /** ----------------------------- MONTHLY REPORT ----------------------------- **/
    private void generateMonthlyReport(Model model, List<CarbonFootprint> footprints) {

        Map<YearMonth, List<CarbonFootprint>> monthlyData = footprints.stream()
                .collect(Collectors.groupingBy(f -> YearMonth.from(f.getDate())));

        List<String> labels = new ArrayList<>();
        List<Double> data = new ArrayList<>();

        monthlyData.entrySet().stream()
                .sorted(Map.Entry.<YearMonth, List<CarbonFootprint>>comparingByKey().reversed())
                .limit(12)
                .forEach(entry -> {
                    labels.add(entry.getKey().format(DateTimeFormatter.ofPattern("MMM yyyy")));
                    data.add(entry.getValue().stream().mapToDouble(CarbonFootprint::getTotalEmissions).sum());
                });

        Collections.reverse(labels);
        Collections.reverse(data);

        addCommonStats(model, labels, data, "Monthly Carbon Emissions Report");
    }

    /** ----------------------------- YEARLY REPORT ----------------------------- **/
    private void generateYearlyReport(Model model, List<CarbonFootprint> footprints) {

        Map<Integer, List<CarbonFootprint>> yearlyData = footprints.stream()
                .collect(Collectors.groupingBy(f -> f.getDate().getYear()));

        List<String> labels = new ArrayList<>();
        List<Double> data = new ArrayList<>();

        yearlyData.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // ascending
                .forEach(entry -> {
                    labels.add(entry.getKey().toString());
                    data.add(entry.getValue().stream().mapToDouble(CarbonFootprint::getTotalEmissions).sum());
                });

        addCommonStats(model, labels, data, "Yearly Carbon Emissions Report");
    }

    /** ----------------------------- WEEKLY REPORT ----------------------------- **/
    private void generateWeeklyReport(Model model, List<CarbonFootprint> footprints) {

        Map<String, List<CarbonFootprint>> weeklyData =
                footprints.stream().collect(Collectors.groupingBy(f -> {

                    int week = f.getDate().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                    int year = f.getDate().get(IsoFields.WEEK_BASED_YEAR);

                    return "Week " + week + " (" + year + ")";
                }));

        List<String> labels = new ArrayList<>();
        List<Double> data = new ArrayList<>();

        weeklyData.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // chronological
                .forEach(entry -> {
                    labels.add(entry.getKey());
                    data.add(entry.getValue().stream()
                            .mapToDouble(CarbonFootprint::getTotalEmissions)
                            .sum());
                });

        addCommonStats(model, labels, data, "Weekly Carbon Emissions Report");
    }


    /** ----------------------------- COMMON STATS FOR ALL REPORTS ----------------------------- **/
    private void addCommonStats(Model model, List<String> labels, List<Double> data, String title) {

        double average = data.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);

        Double latest = data.size() > 0 ? data.get(data.size() - 1) : 0;
        Double prev = data.size() > 1 ? data.get(data.size() - 2) : 0;

        model.addAttribute("chartLabels", labels);
        model.addAttribute("chartData", data);
        model.addAttribute("average", average);
        model.addAttribute("latestValue", latest);
        model.addAttribute("prevValue", prev);
        model.addAttribute("reportTitle", title);
    }
}
