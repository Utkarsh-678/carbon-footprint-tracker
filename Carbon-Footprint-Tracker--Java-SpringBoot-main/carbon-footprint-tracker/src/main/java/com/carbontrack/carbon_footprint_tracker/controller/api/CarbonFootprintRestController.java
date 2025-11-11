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

import com.carbontrack.carbon_footprint_tracker.entity.CarbonFootprint;
import com.carbontrack.carbon_footprint_tracker.entity.User;
import com.carbontrack.carbon_footprint_tracker.service.CarbonFootprintService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/carbon-footprint")
@CrossOrigin(origins = "http://localhost:4200")
public class CarbonFootprintRestController {

    private static final Logger log = LoggerFactory.getLogger(CarbonFootprintRestController.class);
    private final CarbonFootprintService carbonFootprintService;

    public CarbonFootprintRestController(CarbonFootprintService carbonFootprintService) {
        this.carbonFootprintService = carbonFootprintService;
    }

    @GetMapping
    public ResponseEntity<List<CarbonFootprint>> getUserFootprints(@AuthenticationPrincipal User user) {
        try {
            List<CarbonFootprint> footprints = carbonFootprintService.getUserCarbonFootprints(user);
            return ResponseEntity.ok(footprints);
        } catch (Exception e) {
            log.error("Error fetching carbon footprints for user: {}", user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarbonFootprint> getFootprint(@PathVariable Long id, @AuthenticationPrincipal User user) {
        try {
            Optional<CarbonFootprint> footprint = carbonFootprintService.findById(id);
            if (footprint.isEmpty() || !footprint.get().getUser().getId().equals(user.getId())) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(footprint.get());
        } catch (Exception e) {
            log.error("Error fetching carbon footprint with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createFootprint(@Valid @RequestBody CarbonFootprint footprint, 
                                           @AuthenticationPrincipal User user) {
        try {
            // Check if footprint already exists for this date
            Optional<CarbonFootprint> existing = carbonFootprintService.findByUserAndDate(user, footprint.getDate());
            if (existing.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Carbon footprint entry already exists for this date. Please edit the existing entry or choose a different date.");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }

            footprint.setUser(user);
            CarbonFootprint savedFootprint = carbonFootprintService.saveCarbonFootprint(footprint);
            log.info("Carbon footprint created successfully for user: {}", user.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedFootprint);
        } catch (Exception e) {
            log.error("Error creating carbon footprint for user: {}", user.getUsername(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to create carbon footprint: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFootprint(@PathVariable Long id,
                                           @Valid @RequestBody CarbonFootprint footprint,
                                           @AuthenticationPrincipal User user) {
        try {
            Optional<CarbonFootprint> existingFootprint = carbonFootprintService.findById(id);
            if (existingFootprint.isEmpty() || !existingFootprint.get().getUser().getId().equals(user.getId())) {
                return ResponseEntity.notFound().build();
            }

            footprint.setId(id);
            footprint.setUser(user);
            footprint.setCreatedAt(existingFootprint.get().getCreatedAt());
            CarbonFootprint updatedFootprint = carbonFootprintService.saveCarbonFootprint(footprint);
            
            log.info("Carbon footprint updated successfully for user: {}", user.getUsername());
            return ResponseEntity.ok(updatedFootprint);
        } catch (Exception e) {
            log.error("Error updating carbon footprint with id: {}", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to update carbon footprint: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFootprint(@PathVariable Long id, @AuthenticationPrincipal User user) {
        try {
            Optional<CarbonFootprint> footprint = carbonFootprintService.findById(id);
            if (footprint.isEmpty() || !footprint.get().getUser().getId().equals(user.getId())) {
                return ResponseEntity.notFound().build();
            }

            carbonFootprintService.deleteCarbonFootprint(id);
            log.info("Carbon footprint deleted successfully for user: {}", user.getUsername());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Carbon footprint entry deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting carbon footprint with id: {}", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to delete carbon footprint: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{id}/recommendations")
    public ResponseEntity<Map<String, List<String>>> getRecommendations(@PathVariable Long id, 
                                                                        @AuthenticationPrincipal User user) {
        try {
            Optional<CarbonFootprint> footprint = carbonFootprintService.findById(id);
            if (footprint.isEmpty() || !footprint.get().getUser().getId().equals(user.getId())) {
                return ResponseEntity.notFound().build();
            }

            List<String> recommendations = carbonFootprintService.getRecommendations(footprint.get());
            Map<String, List<String>> response = new HashMap<>();
            response.put("recommendations", recommendations);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching recommendations for footprint with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}