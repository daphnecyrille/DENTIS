package com.dentis.DENTIS.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage(){
        return "login";
    }

    @GetMapping("/clinician-dashboard")
    public String clinicianDashboard(){
        return "dashboard-clinician";
    }

    @GetMapping("/chartrequest-clinician")
    public String chartRequestClinician(){
        return "chartrequest-clinician";
    }

    @GetMapping("/patientlist-clinician")
    public String patientListClinician(){
        return "patientlist-clinician";
    }

    @GetMapping("/chartsview-clinician")
    public String chartsviewClinician(){
        return "chartsview-clinician";
    }

    @GetMapping("/admitting-clinicmanager")
    public String admittingClinicManager(){
        return "admitting-clinicmanager";
    }

    @GetMapping("/admitting-view-clinician")
    public String admittingViewClinician(){
        return "admitting-view-clinician";
    }

}