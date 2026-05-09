package com.dentis.DENTIS.controller;

import com.dentis.DENTIS.model.Patient;
import com.dentis.DENTIS.service.PatientService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/dashboard-clinicmanager")
    public String dashboard(Model model) {
        model.addAttribute("patients", patientService.getAllPatients());
        return "dashboard-clinicmanager";
    }

    @GetMapping("/admitting-clinicmanager")
    public String showAdmittingForm(Model model) {
        model.addAttribute("patient", new Patient());
        return "admitting-clinicmanager";
    }

    @PostMapping("/admitting-clinicmanager")
    public String submitAdmittingForm(@ModelAttribute Patient patient) {
        patientService.save(patient);
        return "redirect:/patientlist-clinicmanager";
    }

    @GetMapping("/patientlist-clinicmanager")
    public String patientList(Model model) {
        model.addAttribute("patients", patientService.getAllPatients());
        return "patientlist-clinicmanager";
    }

    @GetMapping("/chartsview-clinicmanager/{id}")
    public String chartsView(@PathVariable Long id, Model model) {
        model.addAttribute("patient", patientService.getPatientById(id));
        return "chartsview-clinicmanager";
    }

    @GetMapping("/admitting-view-clinicmanager/{id}")
    public String admittingView(@PathVariable Long id, Model model) {
        model.addAttribute("patient", patientService.getPatientById(id));
        return "admitting-view-clinicmanager";
    }
}
