package com.dentis.DENTIS.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "msg", required = false) String msg,
                            Model model) {
        if (error != null) {
            String display = (msg != null && !msg.isBlank()) ? msg
                    : "Access denied. Your Google account is not registered in the system. Please contact your administrator.";
            model.addAttribute("errorMessage", display);
        }
        return "login";
    }

    @GetMapping("/oralsurgery-clinician")
    public String oralSurgeryClinician() { return "oralsurgery-clinician"; }

    @GetMapping("/oralsurgery2-clinician")
    public String oralSurgery2Clinician() { return "oralsurgery2-clinician"; }

    @GetMapping("/admin-dashboard")
    public String adminDashboard() { return "dashboard-clinician"; } // placeholder
}
