package com.dentis.DENTIS.controller;

import com.dentis.DENTIS.model.Patient;
import com.dentis.DENTIS.service.ChartRequestService;
import com.dentis.DENTIS.service.PatientService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PatientController {

    private final PatientService patientService;
    private final ChartRequestService chartRequestService;

    public PatientController(PatientService patientService, ChartRequestService chartRequestService) {
        this.patientService = patientService;
        this.chartRequestService = chartRequestService;
    }

    @GetMapping("/dashboard-clinicmanager")
    public String dashboard(Model model) {
        model.addAttribute("patients", patientService.getAllPatients());
        model.addAttribute("requests", chartRequestService.getPendingRequests());
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
        Patient patient = patientService.getPatientById(id);
        model.addAttribute("patient", patient);
        model.addAttribute("facultyOptions", patientService.getFacultyBySection(patient.getServiceCode()));
        return "chartsview-clinicmanager";
    }

    @PostMapping("/chartsview-clinicmanager/{id}/assign-faculty")
    public String assignFaculty(@PathVariable Long id, @RequestParam Long facultyId) {
        patientService.assignFaculty(id, facultyId);
        return "redirect:/chartsview-clinicmanager/" + id;
    }

    @GetMapping("/admitting-view-clinicmanager/{id}")
    public String admittingView(@PathVariable Long id, Model model) {
        model.addAttribute("patient", patientService.getPatientById(id));
        return "admitting-view-clinicmanager";
    }

    @GetMapping("/admitting-edit-clinician/{id}")
    public String admittingEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("patient", patientService.getPatientById(id));
        return "admitting-edit-clinician";
    }

    @PostMapping("/admitting-edit-clinician/{id}")
    public String admittingEditSave(@PathVariable Long id, @ModelAttribute Patient patient) {
        patientService.update(id, patient);
        return "redirect:/chartsview-clinician/" + id;
    }
}
