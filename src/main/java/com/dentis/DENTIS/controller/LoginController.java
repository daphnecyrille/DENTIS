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

    @GetMapping("/endodontics-clinician")
    public String endodonticsClinician() { return "endodontics-clinician"; }

    @GetMapping("/periodonticsa-clinician")
    public String periodonticsaClinician() { return "periodonticsa-clinician"; }

    @GetMapping("/periodonticsb-clinician")
    public String periodonticsbClinician() { return "periodonticsb-clinician"; }

    @GetMapping("/periodonticsc-clinician")
    public String periodonticscClinician() { return "periodonticsc-clinician"; }

    @GetMapping("/admin-dashboard")
    public String adminDashboard() { return "dashboard-clinician"; } // placeholder
}
