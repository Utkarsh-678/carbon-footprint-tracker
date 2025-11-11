package com.carbontrack.carbon_footprint_tracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Carbon Footprint Tracker");
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

@GetMapping("/api-test")
public String apiTest() {
    return "api-test";
}
}