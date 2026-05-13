package com.dentis.DENTIS.controller;

import com.dentis.DENTIS.model.Patient;
import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.service.ChartRequestService;
import com.dentis.DENTIS.service.ClinicianFacultyAssignmentService;
import com.dentis.DENTIS.service.PatientService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class PatientController {

    private final PatientService patientService;
    private final ChartRequestService chartRequestService;
    private final ClinicianFacultyAssignmentService assignmentService;

    public PatientController(PatientService patientService, ChartRequestService chartRequestService,
                              ClinicianFacultyAssignmentService assignmentService) {
        this.patientService = patientService;
        this.chartRequestService = chartRequestService;
        this.assignmentService = assignmentService;
    }

    @GetMapping("/dashboard-clinicmanager")
    public String dashboard(Model model) {
        model.addAttribute("patients", patientService.getAllPatients().stream().limit(5).toList());
        model.addAttribute("requests", chartRequestService.getPendingRequests().stream().limit(5).toList());
        model.addAttribute("statTotalPatients", patientService.countAll());
        model.addAttribute("statOS", patientService.countBySection("OS"));
        model.addAttribute("statENDO", patientService.countBySection("ENDO"));
        model.addAttribute("statPERIO", patientService.countBySection("PERIO"));
        model.addAttribute("statRESTO", patientService.countBySection("Resto"));
        model.addAttribute("statPendingRequests", chartRequestService.getPendingRequests().size());
        model.addAttribute("statClinicians", patientService.getAllClinicians().size());
        return "dashboard-clinicmanager";
    }

    @GetMapping("/assign-clinicmanager")
    public String assignClinicians(Model model) {
        model.addAttribute("assignmentRows", assignmentService.getAssignmentRows());
        model.addAttribute("osFaculty", patientService.getFacultyBySection("OS"));
        model.addAttribute("endoFaculty", patientService.getFacultyBySection("ENDO"));
        model.addAttribute("perioFaculty", patientService.getFacultyBySection("PERIO"));
        model.addAttribute("restoFaculty", patientService.getFacultyBySection("Resto"));
        return "assign-clinicmanager";
    }

    @PostMapping("/assign-clinicmanager/{clinicianId}")
    public String saveAssignment(@PathVariable Long clinicianId,
                                  @RequestParam(required = false) Long osFacultyId,
                                  @RequestParam(required = false) Long endoFacultyId,
                                  @RequestParam(required = false) Long perioFacultyId,
                                  @RequestParam(required = false) Long restoFacultyId) {
        assignmentService.saveAssignment(clinicianId, osFacultyId, endoFacultyId, perioFacultyId, restoFacultyId);
        return "redirect:/assign-clinicmanager";
    }

    private com.dentis.DENTIS.model.User getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            String email = oAuth2User.getAttribute("email");
            return patientService.getUserByEmail(email);
        }
        return null;
    }

    @GetMapping("/clinician-profile/{id}")
    public String clinicianProfile(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("currentUser", getCurrentUser(authentication));
        User clinician = patientService.getUserById(id);
        List<Patient> allPatients = patientService.getPatientsForClinician(clinician);
        model.addAttribute("clinician", clinician);
        model.addAttribute("assignmentRow", assignmentService.getAssignmentRowForClinician(clinician));
        model.addAttribute("osPatients", allPatients.stream()
                .filter(p -> "OS".equalsIgnoreCase(p.getServiceCode())).toList());
        model.addAttribute("endoPatients", allPatients.stream()
                .filter(p -> "ENDO".equalsIgnoreCase(p.getServiceCode())).toList());
        model.addAttribute("perioPatients", allPatients.stream()
                .filter(p -> "PERIO".equalsIgnoreCase(p.getServiceCode())).toList());
        model.addAttribute("restoPatients", allPatients.stream()
                .filter(p -> "Resto".equalsIgnoreCase(p.getServiceCode())).toList());
        model.addAttribute("allPatients", allPatients);
        return "clinician-profile-clinicmanager";
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
    public String admittingEditForm(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("patient", patientService.getPatientById(id));
        return "admitting-edit-clinician";
    }

    @PostMapping("/admitting-edit-clinician/{id}")
    public String admittingEditSave(@PathVariable Long id, @ModelAttribute Patient patient) {
        patientService.update(id, patient);
        return "redirect:/chartsview-clinician/" + id;
    }
}
