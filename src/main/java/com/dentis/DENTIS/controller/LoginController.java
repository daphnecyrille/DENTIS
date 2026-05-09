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

    // --- Clinician routes ---
    @GetMapping("/clinician-dashboard")
    public String clinicianDashboard() { return "dashboard-clinician"; }

    @GetMapping("/chartrequest-clinician")
    public String chartRequestClinician() { return "chartrequest-clinician"; }

    @GetMapping("/patientlist-clinician")
    public String patientListClinician() { return "patientlist-clinician"; }

    @GetMapping("/chartsview-clinician")
    public String chartsviewClinician() { return "chartsview-clinician"; }

    @GetMapping("/admitting-view-clinician")
    public String admittingViewClinician() { return "admitting-view-clinician"; }

    @GetMapping("/oralsurgery-clinician")
    public String oralSurgeryClinician() { return "oralsurgery-clinician"; }

    @GetMapping("/oralsurgery2-clinician")
    public String oralSurgery2Clinician() { return "oralsurgery2-clinician"; }

    // --- Clinic Manager routes ---
    @GetMapping("/requestlist-clinicmanager")
    public String requestListClinicManager() { return "requestlist-clinicmanager"; }

    // --- Faculty routes ---
    @GetMapping("/faculty-dashboard")
    public String facultyDashboard() { return "dashboard-clinician"; } // placeholder

    // --- Admin routes ---
    @GetMapping("/admin-dashboard")
    public String adminDashboard() { return "dashboard-clinician"; } // placeholder
}
