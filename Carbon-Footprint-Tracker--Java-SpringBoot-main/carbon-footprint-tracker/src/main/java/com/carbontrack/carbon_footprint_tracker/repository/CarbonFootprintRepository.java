package com.carbontrack.carbon_footprint_tracker.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.carbontrack.carbon_footprint_tracker.entity.CarbonFootprint;
import com.carbontrack.carbon_footprint_tracker.entity.User;

@Repository
public interface CarbonFootprintRepository extends JpaRepository<CarbonFootprint, Long> {
    List<CarbonFootprint> findByUserOrderByDateDesc(User user);
    List<CarbonFootprint> findByUserAndDateBetweenOrderByDateDesc(User user, LocalDate startDate, LocalDate endDate);
    Optional<CarbonFootprint> findByUserAndDate(User user, LocalDate date);

    @Query("SELECT SUM(cf.totalEmissions) FROM CarbonFootprint cf WHERE cf.user = :user AND cf.date BETWEEN :startDate AND :endDate")
    Double sumEmissionsByUserAndDateRange(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT AVG(cf.totalEmissions) FROM CarbonFootprint cf WHERE cf.user = :user")
    Double averageEmissionsByUser(@Param("user") User user);
}

