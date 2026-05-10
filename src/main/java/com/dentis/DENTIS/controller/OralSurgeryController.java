package com.dentis.DENTIS.controller;

import com.dentis.DENTIS.model.OralSurgeryChart;
import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.repository.UserRepository;
import com.dentis.DENTIS.service.OralSurgeryChartService;
import com.dentis.DENTIS.service.PatientService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class OralSurgeryController {

    private final OralSurgeryChartService oralSurgeryChartService;
    private final PatientService patientService;
    private final UserRepository userRepository;

    public OralSurgeryController(OralSurgeryChartService oralSurgeryChartService,
                                 PatientService patientService,
                                 UserRepository userRepository) {
        this.oralSurgeryChartService = oralSurgeryChartService;
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

    // ── Clinician: Form 1 (Patient Workup) ───────────────────────────────────

    @GetMapping("/oralsurgery-clinician/{id}")
    public String form1(@PathVariable Long id, Model model, Authentication authentication) {
        OralSurgeryChart chart = oralSurgeryChartService.getById(id);
        if (chart.getChiefComplaint() == null || chart.getChiefComplaint().isBlank()) {
            chart.setChiefComplaint(chart.getPatient().getChiefComplaint());
        }
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        model.addAttribute("viewOnly", false);
        return "oralsurgery-clinician";
    }

    @GetMapping("/oralsurgery-clinician/{id}/view")
    public String viewForm1(@PathVariable Long id, Model model, Authentication authentication) {
        OralSurgeryChart chart = oralSurgeryChartService.getById(id);
        if (chart.getChiefComplaint() == null || chart.getChiefComplaint().isBlank()) {
            chart.setChiefComplaint(chart.getPatient().getChiefComplaint());
        }
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        model.addAttribute("viewOnly", true);
        return "oralsurgery-clinician";
    }

    @PostMapping("/oralsurgery-clinician/{id}")
    public String saveForm1(@PathVariable Long id,
                            @ModelAttribute("chart") OralSurgeryChart updated) {
        OralSurgeryChart chart = oralSurgeryChartService.saveForm1(id, updated);
        return "redirect:/chartsview-clinician/" + chart.getPatient().getId();
    }

    // ── Clinician: Form 2 (Odontectomy Workup) ───────────────────────────────

    @GetMapping("/oralsurgery2-clinician/{id}")
    public String form2(@PathVariable Long id, Model model, Authentication authentication) {
        OralSurgeryChart chart = oralSurgeryChartService.getById(id);
        if (chart.getChiefComplaint() == null || chart.getChiefComplaint().isBlank()) {
            chart.setChiefComplaint(chart.getPatient().getChiefComplaint());
        }
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        model.addAttribute("viewOnly", false);
        return "oralsurgery2-clinician";
    }

    @GetMapping("/oralsurgery2-clinician/{id}/view")
    public String viewForm2(@PathVariable Long id, Model model, Authentication authentication) {
        OralSurgeryChart chart = oralSurgeryChartService.getById(id);
        if (chart.getChiefComplaint() == null || chart.getChiefComplaint().isBlank()) {
            chart.setChiefComplaint(chart.getPatient().getChiefComplaint());
        }
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        model.addAttribute("viewOnly", true);
        return "oralsurgery2-clinician";
    }

    @PostMapping("/oralsurgery2-clinician/{id}")
    public String saveForm2(@PathVariable Long id,
                            @ModelAttribute("chart") OralSurgeryChart updated) {
        OralSurgeryChart chart = oralSurgeryChartService.saveForm2(id, updated);
        return "redirect:/chartsview-clinician/" + chart.getPatient().getId();
    }

    @PostMapping("/oralsurgery-clinician/{id}/submit")
    public String submitChart(@PathVariable Long id) {
        OralSurgeryChart chart = oralSurgeryChartService.submit(id);
        return "redirect:/chartsview-clinician/" + chart.getPatient().getId();
    }

    // ── Faculty: Read-only view ───────────────────────────────────────────────

    @GetMapping("/oralsurgery-view-faculty/{id}")
    public String facultyView(@PathVariable Long id, Model model, Authentication authentication) {
        OralSurgeryChart chart = oralSurgeryChartService.getById(id);
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        return "oralsurgery-view-faculty";
    }

    @GetMapping("/oralsurgery2-view-faculty/{id}")
    public String facultyView2(@PathVariable Long id, Model model, Authentication authentication) {
        OralSurgeryChart chart = oralSurgeryChartService.getById(id);
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        return "oralsurgery2-view-faculty";
    }
}
