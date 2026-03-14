package com.project.back_end.mvc;

import com.project.back_end.services.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class DashboardController {

    @Autowired
    private Service service;

    @GetMapping("/admin/login")
    public String adminLoginPage() {
        return "redirect:/";
    }

    @GetMapping("/doctor/login")
    public String doctorLoginPage() {
        return "redirect:/";
    }

    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        Map<String, Object> validationResult = service.validateTokenAsMap(token, "admin");
        if (validationResult.isEmpty()) {
            return "admin/adminDashboard";
        }
        return "redirect:/";
    }

    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable String token) {
        Map<String, Object> validationResult = service.validateTokenAsMap(token, "doctor");
        if (validationResult.isEmpty()) {
            return "doctor/doctorDashboard";
        }
        return "redirect:/";
    }
}
