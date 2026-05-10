package com.dentis.DENTIS.controller;

import com.dentis.DENTIS.model.OralSurgeryChart;
import com.dentis.DENTIS.model.Patient;
import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.repository.UserRepository;
import com.dentis.DENTIS.service.OralSurgeryChartService;
import com.dentis.DENTIS.service.PatientService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class FacultyController {

    private final PatientService patientService;
    private final OralSurgeryChartService oralSurgeryChartService;
    private final UserRepository userRepository;

    public FacultyController(PatientService patientService,
                             OralSurgeryChartService oralSurgeryChartService,
                             UserRepository userRepository) {
        this.patientService = patientService;
        this.oralSurgeryChartService = oralSurgeryChartService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            String email = oAuth2User.getAttribute("email");
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }

    @GetMapping("/faculty-dashboard")
    public String dashboard(Model model, Authentication authentication) {
        User faculty = getCurrentUser(authentication);
        model.addAttribute("currentUser", faculty);
        model.addAttribute("osCharts", faculty != null
                ? oralSurgeryChartService.findByFacultyOrderByCreatedAtDesc(faculty).stream().limit(3).toList()
                : List.of());
        model.addAttribute("awaitingApprovalCharts", faculty != null
                ? oralSurgeryChartService.findAwaitingApprovalByFaculty(faculty)
                : List.of());
        model.addAttribute("unassignedPatients", faculty != null
                ? patientService.getUnassignedPatientsForFaculty(faculty).stream().limit(3).toList()
                : List.of());
        model.addAttribute("clinicians", patientService.getAllClinicians());
        return "dashboard-faculty";
    }

    @GetMapping("/faculty-patientlist")
    public String patientList(Model model, Authentication authentication) {
        User faculty = getCurrentUser(authentication);
        model.addAttribute("currentUser", faculty);
        model.addAttribute("osCharts", faculty != null
                ? oralSurgeryChartService.findByFacultyOrderByCreatedAtDesc(faculty)
                : List.of());
        return "patientlist-faculty";
    }

    @GetMapping("/faculty-assign")
    public String assignPatients(Model model, Authentication authentication) {
        User faculty = getCurrentUser(authentication);
        model.addAttribute("currentUser", faculty);
        model.addAttribute("patients", faculty != null
                ? patientService.getUnassignedPatientsForFaculty(faculty)
                : List.of());
        model.addAttribute("clinicians", patientService.getAllClinicians());
        return "assign-patients-faculty";
    }

    @GetMapping("/chartsview-faculty/{id}")
    public String chartsView(@PathVariable Long id, Model model, Authentication authentication) {
        Patient patient = patientService.getPatientById(id);
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("patient", patient);
        model.addAttribute("osCharts", oralSurgeryChartService.findAllByPatient(patient));
        return "chartsview-faculty";
    }

    @GetMapping("/admitting-view-faculty/{id}")
    public String admittingView(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("patient", patientService.getPatientById(id));
        return "admitting-view-faculty";
    }

    @GetMapping("/endodontics2-faculty")
    public String endodontics2Faculty(Model model, Authentication authentication) {
        model.addAttribute("currentUser", getCurrentUser(authentication));
        return "endodontics2-faculty";
    }

    @PostMapping("/oralsurgery-faculty/{id}/approve")
    public String approveChart(@PathVariable Long id) {
        OralSurgeryChart chart = oralSurgeryChartService.approve(id);
        return "redirect:/chartsview-faculty/" + chart.getPatient().getId();
    }

    @PostMapping("/oralsurgery-faculty/{id}/revise")
    public String reviseChart(@PathVariable Long id) {
        OralSurgeryChart chart = oralSurgeryChartService.revise(id);
        return "redirect:/chartsview-faculty/" + chart.getPatient().getId();
    }

    @PostMapping("/faculty-dashboard/{id}/assign-clinician")
    public String assignClinician(@PathVariable Long id,
                                  @RequestParam Long clinicianId,
                                  @RequestParam(required = false, defaultValue = "faculty-assign") String returnTo,
                                  Authentication authentication) {
        User faculty = getCurrentUser(authentication);
        patientService.assignClinician(id, clinicianId, faculty);
        return "redirect:/" + returnTo;
    }
}
