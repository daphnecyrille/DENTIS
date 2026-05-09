package com.dentis.DENTIS.controller;

import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.repository.UserRepository;
import com.dentis.DENTIS.service.PatientService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class FacultyController {

    private final PatientService patientService;
    private final UserRepository userRepository;

    public FacultyController(PatientService patientService, UserRepository userRepository) {
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

    @GetMapping("/faculty-dashboard")
    public String dashboard(Model model, Authentication authentication) {
        User faculty = getCurrentUser(authentication);
        model.addAttribute("currentUser", faculty);
        model.addAttribute("patients", faculty != null
                ? patientService.getPatientsForFaculty(faculty)
                : java.util.List.of());
        return "dashboard-faculty";
    }

    @GetMapping("/faculty-patientlist")
    public String patientList(Model model, Authentication authentication) {
        User faculty = getCurrentUser(authentication);
        model.addAttribute("currentUser", faculty);
        model.addAttribute("patients", faculty != null
                ? patientService.getPatientsForFaculty(faculty)
                : java.util.List.of());
        return "patientlist-faculty";
    }

    @GetMapping("/faculty-assign")
    public String assignPatients(Model model, Authentication authentication) {
        User faculty = getCurrentUser(authentication);
        model.addAttribute("currentUser", faculty);
        model.addAttribute("patients", faculty != null
                ? patientService.getPatientsForFaculty(faculty)
                : java.util.List.of());
        model.addAttribute("clinicians", patientService.getAllClinicians());
        return "assign-patients-faculty";
    }

    @GetMapping("/chartsview-faculty/{id}")
    public String chartsView(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("patient", patientService.getPatientById(id));
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

    @PostMapping("/faculty-dashboard/{id}/assign-clinician")
    public String assignClinician(@PathVariable Long id,
                                  @RequestParam Long clinicianId,
                                  @RequestParam(required = false, defaultValue = "assign") String returnTo) {
        patientService.assignClinician(id, clinicianId);
        return "redirect:/" + returnTo;
    }
}
