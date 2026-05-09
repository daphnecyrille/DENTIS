package com.dentis.DENTIS.controller;

import com.dentis.DENTIS.model.ChartRequest;
import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.repository.UserRepository;
import com.dentis.DENTIS.service.ChartRequestService;
import com.dentis.DENTIS.service.PatientService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ChartRequestController {

    private final ChartRequestService chartRequestService;
    private final PatientService patientService;
    private final UserRepository userRepository;

    public ChartRequestController(ChartRequestService chartRequestService,
                                  PatientService patientService,
                                  UserRepository userRepository) {
        this.chartRequestService = chartRequestService;
        this.patientService = patientService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            String email = oAuth2User.getAttribute("email");
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }

    @GetMapping("/clinician-dashboard")
    public String clinicianDashboard(Model model, Authentication authentication) {
        User clinician = getCurrentUser(authentication);
        model.addAttribute("currentUser", clinician);
        model.addAttribute("assignedPatients", clinician != null
                ? patientService.getPatientsForClinician(clinician)
                : List.of());
        model.addAttribute("requests", clinician != null
                ? chartRequestService.getClinicianRequests(clinician)
                : List.of());
        return "dashboard-clinician";
    }

    @GetMapping("/chartrequest-clinician")
    public String showForm(Model model, Authentication authentication) {
        User clinician = getCurrentUser(authentication);
        model.addAttribute("chartRequest", new ChartRequest());
        model.addAttribute("requests", clinician != null
                ? chartRequestService.getClinicianRequests(clinician)
                : List.of());
        return "chartrequest-clinician";
    }

    @PostMapping("/chartrequest-clinician")
    public String submitRequest(@ModelAttribute ChartRequest chartRequest,
                                Authentication authentication) {
        User clinician = getCurrentUser(authentication);
        chartRequestService.submit(chartRequest, clinician != null ? clinician.getId() : null);
        return "redirect:/chartrequest-clinician";
    }

    @GetMapping("/requestlist-clinicmanager")
    public String requestList(Model model) {
        model.addAttribute("requests", chartRequestService.getPendingRequests());
        return "requestlist-clinicmanager";
    }

    @PostMapping("/requestlist-clinicmanager/{id}/approve")
    public String approve(@PathVariable Long id) {
        chartRequestService.approve(id);
        return "redirect:/requestlist-clinicmanager";
    }

    @PostMapping("/requestlist-clinicmanager/{id}/deny")
    public String deny(@PathVariable Long id) {
        chartRequestService.deny(id);
        return "redirect:/requestlist-clinicmanager";
    }

    @GetMapping("/patientlist-clinician")
    public String patientList(Model model, Authentication authentication) {
        User clinician = getCurrentUser(authentication);
        model.addAttribute("currentUser", clinician);
        model.addAttribute("assignedPatients", clinician != null
                ? patientService.getPatientsForClinician(clinician)
                : List.of());
        return "patientlist-clinician";
    }

    @GetMapping("/admitting-view-clinician/{id}")
    public String admittingViewClinician(@PathVariable Long id, Model model) {
        model.addAttribute("patient", chartRequestService.getPatientById(id));
        return "admitting-view-clinician";
    }

    @GetMapping("/chartsview-clinician/{id}")
    public String chartsViewClinician(@PathVariable Long id, Model model, Authentication authentication) {
        User clinician = getCurrentUser(authentication);
        model.addAttribute("patient", chartRequestService.getPatientById(id));
        model.addAttribute("approvedCharts", clinician != null
                ? chartRequestService.getApprovedRequestsForClinicianAndPatient(clinician, id)
                : List.of());
        return "chartsview-clinician";
    }
}
