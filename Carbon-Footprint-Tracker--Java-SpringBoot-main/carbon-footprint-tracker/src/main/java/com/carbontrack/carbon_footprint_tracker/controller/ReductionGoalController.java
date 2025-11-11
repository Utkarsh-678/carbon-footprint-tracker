package com.carbontrack.carbon_footprint_tracker.controller;

import com.carbontrack.carbon_footprint_tracker.entity.ReductionGoal;
import com.carbontrack.carbon_footprint_tracker.entity.User;
import com.carbontrack.carbon_footprint_tracker.service.ReductionGoalService;
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
@RequestMapping("/goals")
public class ReductionGoalController {

    private final ReductionGoalService reductionGoalService;

    public ReductionGoalController(ReductionGoalService reductionGoalService) {
        this.reductionGoalService = reductionGoalService;
    }

    @GetMapping
    public String listGoals(@AuthenticationPrincipal User user, Model model) {
        List<ReductionGoal> goals = reductionGoalService.getUserGoals(user);
        model.addAttribute("goals", goals);
        return "goals/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        ReductionGoal goal = new ReductionGoal();
        goal.setTargetDate(LocalDate.now().plusMonths(3));
        model.addAttribute("goal", goal);
        return "goals/form";
    }

    @PostMapping("/add")
    public String addGoal(@Valid @ModelAttribute ReductionGoal goal,
                         BindingResult result,
                         @AuthenticationPrincipal User user,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "goals/form";
        }

        goal.setUser(user);
        reductionGoalService.saveReductionGoal(goal);

        redirectAttributes.addFlashAttribute("successMessage", "Reduction goal created successfully!");
        return "redirect:/goals";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, @AuthenticationPrincipal User user, Model model) {
        Optional<ReductionGoal> goal = reductionGoalService.findById(id);
        if (goal.isEmpty() || !goal.get().getUser().getId().equals(user.getId())) {
            return "redirect:/goals";
        }

        model.addAttribute("goal", goal.get());
        model.addAttribute("isEdit", true);
        return "goals/form";
    }

    @PostMapping("/edit/{id}")
    public String updateGoal(@PathVariable Long id,
                           @Valid @ModelAttribute ReductionGoal goal,
                           BindingResult result,
                           @AuthenticationPrincipal User user,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "goals/form";
        }

        Optional<ReductionGoal> existingGoal = reductionGoalService.findById(id);
        if (existingGoal.isEmpty() || !existingGoal.get().getUser().getId().equals(user.getId())) {
            return "redirect:/goals";
        }

        goal.setId(id);
        goal.setUser(user);
        goal.setCreatedAt(existingGoal.get().getCreatedAt());
        goal.setCurrentReduction(existingGoal.get().getCurrentReduction());
        reductionGoalService.saveReductionGoal(goal);

        redirectAttributes.addFlashAttribute("successMessage", "Reduction goal updated successfully!");
        return "redirect:/goals";
    }

    @PostMapping("/delete/{id}")
    public String deleteGoal(@PathVariable Long id,
                           @AuthenticationPrincipal User user,
                           RedirectAttributes redirectAttributes) {
        Optional<ReductionGoal> goal = reductionGoalService.findById(id);
        if (goal.isPresent() && goal.get().getUser().getId().equals(user.getId())) {
            reductionGoalService.deleteGoal(id);
            redirectAttributes.addFlashAttribute("successMessage", "Reduction goal deleted successfully!");
        }
        return "redirect:/goals";
    }

    @PostMapping("/toggle-status/{id}")
    public String toggleGoalStatus(@PathVariable Long id,
                                 @AuthenticationPrincipal User user,
                                 RedirectAttributes redirectAttributes) {
        Optional<ReductionGoal> goalOpt = reductionGoalService.findById(id);
        if (goalOpt.isPresent() && goalOpt.get().getUser().getId().equals(user.getId())) {
            ReductionGoal goal = goalOpt.get();
            if (goal.getStatus() == ReductionGoal.Status.ACTIVE) {
                goal.setStatus(ReductionGoal.Status.PAUSED);
            } else if (goal.getStatus() == ReductionGoal.Status.PAUSED) {
                goal.setStatus(ReductionGoal.Status.ACTIVE);
            }
            reductionGoalService.saveReductionGoal(goal);
            redirectAttributes.addFlashAttribute("successMessage", "Goal status updated successfully!");
        }
        return "redirect:/goals";
    }
}
