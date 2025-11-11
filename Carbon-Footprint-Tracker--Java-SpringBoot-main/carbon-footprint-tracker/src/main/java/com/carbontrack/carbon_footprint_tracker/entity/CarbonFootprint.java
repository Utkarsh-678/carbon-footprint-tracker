package com.carbontrack.carbon_footprint_tracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "carbon_footprints")
public class CarbonFootprint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @PositiveOrZero(message = "Electricity usage must be positive or zero")
    private Double electricityUsage = 0.0;

    @PositiveOrZero(message = "Gas usage must be positive or zero")
    private Double gasUsage = 0.0;

    @PositiveOrZero(message = "Car distance must be positive or zero")
    private Double carDistance = 0.0;

    @PositiveOrZero(message = "Public transport distance must be positive or zero")
    private Double publicTransportDistance = 0.0;

    @PositiveOrZero(message = "Flight distance must be positive or zero")
    private Double flightDistance = 0.0;

    @PositiveOrZero(message = "Waste produced must be positive or zero")
    private Double wasteProduced = 0.0;

    @PositiveOrZero(message = "Recycling amount must be positive or zero")
    private Double recyclingAmount = 0.0;

    private Double totalEmissions;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructors
    public CarbonFootprint() {}

    public CarbonFootprint(User user, LocalDate date) {
        this.user = user;
        this.date = date;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Double getElectricityUsage() { return electricityUsage; }
    public void setElectricityUsage(Double electricityUsage) { this.electricityUsage = electricityUsage; }

    public Double getGasUsage() { return gasUsage; }
    public void setGasUsage(Double gasUsage) { this.gasUsage = gasUsage; }

    public Double getCarDistance() { return carDistance; }
    public void setCarDistance(Double carDistance) { this.carDistance = carDistance; }

    public Double getPublicTransportDistance() { return publicTransportDistance; }
    public void setPublicTransportDistance(Double publicTransportDistance) { this.publicTransportDistance = publicTransportDistance; }

    public Double getFlightDistance() { return flightDistance; }
    public void setFlightDistance(Double flightDistance) { this.flightDistance = flightDistance; }

    public Double getWasteProduced() { return wasteProduced; }
    public void setWasteProduced(Double wasteProduced) { this.wasteProduced = wasteProduced; }

    public Double getRecyclingAmount() { return recyclingAmount; }
    public void setRecyclingAmount(Double recyclingAmount) { this.recyclingAmount = recyclingAmount; }

    public Double getTotalEmissions() { return totalEmissions; }
    public void setTotalEmissions(Double totalEmissions) { this.totalEmissions = totalEmissions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // JPA Lifecycle methods
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        calculateTotalEmissions();
    }

    @PrePersist
    public void prePersist() {
        calculateTotalEmissions();
    }

    private void calculateTotalEmissions() {
        double electricityFactor = 0.4;
        double gasFactor = 2.3;
        double carFactor = 0.2;
        double publicTransportFactor = 0.05;
        double flightFactor = 0.25;
        double wasteFactor = 0.5;
        double recyclingReduction = 0.3;

        totalEmissions = (electricityUsage * electricityFactor) +
                (gasUsage * gasFactor) +
                (carDistance * carFactor) +
                (publicTransportDistance * publicTransportFactor) +
                (flightDistance * flightFactor) +
                (wasteProduced * wasteFactor) -
                (recyclingAmount * recyclingReduction);

        if (totalEmissions < 0) {
            totalEmissions = 0.0;
        }
    }
}
