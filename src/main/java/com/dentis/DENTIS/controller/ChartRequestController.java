package com.dentis.DENTIS.controller;

import com.dentis.DENTIS.model.ChartRequest;
import com.dentis.DENTIS.service.ChartRequestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ChartRequestController {

    private final ChartRequestService chartRequestService;

    public ChartRequestController(ChartRequestService chartRequestService) {
        this.chartRequestService = chartRequestService;
    }

    @GetMapping("/clinician-dashboard")
    public String clinicianDashboard(Model model) {
        model.addAttribute("patients", chartRequestService.getApprovedPatients());
        model.addAttribute("requests", chartRequestService.getAllRequests());
        return "dashboard-clinician";
    }

    @GetMapping("/chartrequest-clinician")
    public String showForm(Model model) {
        model.addAttribute("chartRequest", new ChartRequest());
        model.addAttribute("requests", chartRequestService.getAllRequests());
        model.addAttribute("clinicians", chartRequestService.getClinicians());
        return "chartrequest-clinician";
    }

    @PostMapping("/chartrequest-clinician")
    public String submitRequest(@ModelAttribute ChartRequest chartRequest,
                                @RequestParam(required = false) Long clinicianId) {
        chartRequestService.submit(chartRequest, clinicianId);
        return "redirect:/chartrequest-clinician";
    }

    @GetMapping("/requestlist-clinicmanager")
    public String requestList(Model model) {
        model.addAttribute("requests", chartRequestService.getAllRequests());
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
    public String patientList(Model model) {
        model.addAttribute("patients", chartRequestService.getApprovedPatients());
        return "patientlist-clinician";
    }

    @GetMapping("/admitting-view-clinician/{id}")
    public String admittingViewClinician(@PathVariable Long id, Model model) {
        model.addAttribute("patient", chartRequestService.getPatientById(id));
        return "admitting-view-clinician";
    }
}
