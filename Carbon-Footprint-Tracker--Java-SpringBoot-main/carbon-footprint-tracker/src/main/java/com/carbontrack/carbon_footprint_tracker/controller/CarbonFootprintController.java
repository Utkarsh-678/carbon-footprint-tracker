package com.carbontrack.carbon_footprint_tracker.controller;

import com.carbontrack.carbon_footprint_tracker.entity.CarbonFootprint;
import com.carbontrack.carbon_footprint_tracker.entity.User;
import com.carbontrack.carbon_footprint_tracker.service.CarbonFootprintService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/footprint")
public class CarbonFootprintController {

    private final CarbonFootprintService carbonFootprintService;

    public CarbonFootprintController(CarbonFootprintService carbonFootprintService) {
        this.carbonFootprintService = carbonFootprintService;
    }

    @GetMapping
    public String listFootprints(@AuthenticationPrincipal User user, Model model) {
        List<CarbonFootprint> footprints = carbonFootprintService.getUserCarbonFootprints(user);
        model.addAttribute("footprints", footprints);
        return "footprint/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        CarbonFootprint footprint = new CarbonFootprint();
        footprint.setDate(LocalDate.now());
        model.addAttribute("footprint", footprint);
        return "footprint/form";
    }

    @PostMapping("/add")
    public String addFootprint(@Valid @ModelAttribute CarbonFootprint footprint,
                             BindingResult result,
                             @AuthenticationPrincipal User user,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "footprint/form";
        }

        // Check if footprint already exists for this date
        Optional<CarbonFootprint> existing = carbonFootprintService.findByUserAndDate(user, footprint.getDate());
        if (existing.isPresent()) {
            model.addAttribute("errorMessage", "Carbon footprint entry already exists for this date. Please edit the existing entry or choose a different date.");
            return "footprint/form";
        }

        footprint.setUser(user);
        carbonFootprintService.saveCarbonFootprint(footprint);

        redirectAttributes.addFlashAttribute("successMessage", "Carbon footprint entry added successfully!");
        return "redirect:/footprint";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, @AuthenticationPrincipal User user, Model model) {
        Optional<CarbonFootprint> footprint = carbonFootprintService.findById(id);
        if (footprint.isEmpty() || !footprint.get().getUser().getId().equals(user.getId())) {
            return "redirect:/footprint";
        }

        model.addAttribute("footprint", footprint.get());
        model.addAttribute("isEdit", true);
        return "footprint/form";
    }

    @PostMapping("/edit/{id}")
    public String updateFootprint(@PathVariable Long id,
                                @Valid @ModelAttribute CarbonFootprint footprint,
                                BindingResult result,
                                @AuthenticationPrincipal User user,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "footprint/form";
        }

        Optional<CarbonFootprint> existingFootprint = carbonFootprintService.findById(id);
        if (existingFootprint.isEmpty() || !existingFootprint.get().getUser().getId().equals(user.getId())) {
            return "redirect:/footprint";
        }

        footprint.setId(id);
        footprint.setUser(user);
        footprint.setCreatedAt(existingFootprint.get().getCreatedAt());
        carbonFootprintService.saveCarbonFootprint(footprint);

        redirectAttributes.addFlashAttribute("successMessage", "Carbon footprint entry updated successfully!");
        return "redirect:/footprint";
    }

    @GetMapping("/view/{id}")
    public String viewFootprint(@PathVariable Long id, @AuthenticationPrincipal User user, Model model) {
        Optional<CarbonFootprint> footprint = carbonFootprintService.findById(id);
        if (footprint.isEmpty() || !footprint.get().getUser().getId().equals(user.getId())) {
            return "redirect:/footprint";
        }

        List<String> recommendations = carbonFootprintService.getRecommendations(footprint.get());
        model.addAttribute("footprint", footprint.get());
        model.addAttribute("recommendations", recommendations);
        return "footprint/view";
    }

    @PostMapping("/delete/{id}")
    public String deleteFootprint(@PathVariable Long id,
                                @AuthenticationPrincipal User user,
                                RedirectAttributes redirectAttributes) {
        Optional<CarbonFootprint> footprint = carbonFootprintService.findById(id);
        if (footprint.isPresent() && footprint.get().getUser().getId().equals(user.getId())) {
            carbonFootprintService.deleteCarbonFootprint(id);
            redirectAttributes.addFlashAttribute("successMessage", "Carbon footprint entry deleted successfully!");
        }
        return "redirect:/footprint";
    }
}
