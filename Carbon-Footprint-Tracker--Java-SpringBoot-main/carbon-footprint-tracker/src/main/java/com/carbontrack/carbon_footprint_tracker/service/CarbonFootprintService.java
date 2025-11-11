package com.carbontrack.carbon_footprint_tracker.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carbontrack.carbon_footprint_tracker.entity.CarbonFootprint;
import com.carbontrack.carbon_footprint_tracker.entity.User;
import com.carbontrack.carbon_footprint_tracker.repository.CarbonFootprintRepository;

@Service
@Transactional
public class CarbonFootprintService {

    private final CarbonFootprintRepository carbonFootprintRepository;

    // Constructor - no @Autowired needed for single constructor
    public CarbonFootprintService(CarbonFootprintRepository carbonFootprintRepository) {
        this.carbonFootprintRepository = carbonFootprintRepository;
    }

    public CarbonFootprint saveCarbonFootprint(CarbonFootprint carbonFootprint) {
        return carbonFootprintRepository.save(carbonFootprint);
    }

    public List<CarbonFootprint> getUserCarbonFootprints(User user) {
        return carbonFootprintRepository.findByUserOrderByDateDesc(user);
    }

    public List<CarbonFootprint> getUserCarbonFootprintsByDateRange(User user, LocalDate startDate, LocalDate endDate) {
        return carbonFootprintRepository.findByUserAndDateBetweenOrderByDateDesc(user, startDate, endDate);
    }

    public Optional<CarbonFootprint> findByUserAndDate(User user, LocalDate date) {
        return carbonFootprintRepository.findByUserAndDate(user, date);
    }

    public Optional<CarbonFootprint> findById(Long id) {
        return carbonFootprintRepository.findById(id);
    }

    public void deleteCarbonFootprint(Long id) {
        carbonFootprintRepository.deleteById(id);
    }

    public Double getTotalEmissionsForPeriod(User user, LocalDate startDate, LocalDate endDate) {
        Double total = carbonFootprintRepository.sumEmissionsByUserAndDateRange(user, startDate, endDate);
        return total != null ? total : 0.0;
    }

    public Double getAverageEmissions(User user) {
        Double average = carbonFootprintRepository.averageEmissionsByUser(user);
        return average != null ? average : 0.0;
    }

    public Double getMonthlyEmissions(User user, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        return getTotalEmissionsForPeriod(user, startDate, endDate);
    }

    public List<String> getRecommendations(CarbonFootprint footprint) {
        List<String> recommendations = new ArrayList<>();

        if (footprint.getElectricityUsage() > 300) {
            recommendations.add("Consider using LED bulbs and energy-efficient appliances to reduce electricity consumption");
        }
        if (footprint.getCarDistance() > 100) {
            recommendations.add("Try carpooling, using public transport, or working from home to reduce vehicle emissions");
        }
        if (footprint.getFlightDistance() > 500) {
            recommendations.add("Consider video conferencing instead of business travel when possible");
        }
        if (footprint.getWasteProduced() > 10) {
            recommendations.add("Increase recycling and composting to reduce waste-related emissions");
        }
        if (footprint.getRecyclingAmount() < footprint.getWasteProduced() * 0.5) {
            recommendations.add("Aim to recycle at least 50% of your waste to maximize environmental benefits");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Great job! Your carbon footprint is relatively low. Keep up the good work!");
        }

        return recommendations;
    }
}