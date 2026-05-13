package com.dentis.DENTIS.controller;

import com.dentis.DENTIS.model.OperativeChart;
import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.repository.UserRepository;
import com.dentis.DENTIS.service.OperativeChartService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class OperativeController {

    private final OperativeChartService operativeChartService;
    private final UserRepository userRepository;

    public OperativeController(OperativeChartService operativeChartService,
                               UserRepository userRepository) {
        this.operativeChartService = operativeChartService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            String email = oAuth2User.getAttribute("email");
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }

    // ── Preview routes (no ID, empty chart for design checking) ─────────────

    @GetMapping("/operativea-clinician")
    public String previewFormA(Model model, Authentication authentication) {
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", new OperativeChart());
        model.addAttribute("viewOnly", false);
        return "operativea-clinician";
    }

    @GetMapping("/operativeb-clinician")
    public String previewFormB(Model model, Authentication authentication) {
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", new OperativeChart());
        model.addAttribute("viewOnly", false);
        return "operativeb-clinician";
    }

    @GetMapping("/operativec-clinician")
    public String previewFormC(Model model, Authentication authentication) {
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", new OperativeChart());
        model.addAttribute("viewOnly", false);
        return "operativec-clinician";
    }

    // ── Clinician: Form A ────────────────────────────────────────────────────

    @GetMapping("/operative-a/{id}")
    public String formA(@PathVariable Long id, Model model, Authentication authentication) {
        OperativeChart chart = operativeChartService.getById(id);
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        model.addAttribute("viewOnly", false);
        return "operativea-clinician";
    }

    @GetMapping("/operative-a/{id}/view")
    public String formAView(@PathVariable Long id, Model model, Authentication authentication) {
        OperativeChart chart = operativeChartService.getById(id);
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        model.addAttribute("viewOnly", true);
        return "operativea-clinician";
    }

    @PostMapping("/operative-a/{id}")
    public String saveFormA(@PathVariable Long id,
                            @ModelAttribute("chart") OperativeChart updated) {
        OperativeChart chart = operativeChartService.saveFormC(id, updated);
        return "redirect:/chartsview-clinician/" + chart.getPatient().getId();
    }

    @PostMapping("/operative-a/{id}/submit")
    public String submitFormA(@PathVariable Long id) {
        OperativeChart chart = operativeChartService.submitForm2(id);
        return "redirect:/chartsview-clinician/" + chart.getPatient().getId();
    }

    // ── Clinician: Form B ────────────────────────────────────────────────────

    @GetMapping("/operative-b/{id}")
    public String formB(@PathVariable Long id, Model model, Authentication authentication) {
        OperativeChart chart = operativeChartService.getById(id);
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        model.addAttribute("viewOnly", false);
        return "operativeb-clinician";
    }

    @GetMapping("/operative-b/{id}/view")
    public String formBView(@PathVariable Long id, Model model, Authentication authentication) {
        OperativeChart chart = operativeChartService.getById(id);
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        model.addAttribute("viewOnly", true);
        return "operativeb-clinician";
    }

    @PostMapping("/operative-b/{id}")
    public String saveFormB(@PathVariable Long id,
                            @ModelAttribute("chart") OperativeChart updated) {
        operativeChartService.saveFormA(id, updated);
        return "redirect:/operative-c/" + id;
    }

    // ── Clinician: Form C ────────────────────────────────────────────────────

    @GetMapping("/operative-c/{id}")
    public String formC(@PathVariable Long id, Model model, Authentication authentication) {
        OperativeChart chart = operativeChartService.getById(id);
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        model.addAttribute("viewOnly", false);
        return "operativec-clinician";
    }

    @GetMapping("/operative-c/{id}/view")
    public String formCView(@PathVariable Long id, Model model, Authentication authentication) {
        OperativeChart chart = operativeChartService.getById(id);
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        model.addAttribute("viewOnly", true);
        return "operativec-clinician";
    }

    @PostMapping("/operative-c/{id}")
    public String saveFormC(@PathVariable Long id,
                            @ModelAttribute("chart") OperativeChart updated) {
        OperativeChart chart = operativeChartService.saveFormB(id, updated);
        return "redirect:/chartsview-clinician/" + chart.getPatient().getId();
    }

    @PostMapping("/operative-c/{id}/submit")
    public String submitFormC(@PathVariable Long id) {
        OperativeChart chart = operativeChartService.submitForm1(id);
        return "redirect:/chartsview-clinician/" + chart.getPatient().getId();
    }

    // ── Faculty: View Form 10 ────────────────────────────────────────────────

    @GetMapping("/operative-view-faculty/{id}")
    public String viewFacultyForm10p1(@PathVariable Long id, Model model, Authentication authentication) {
        OperativeChart chart = operativeChartService.getById(id);
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        model.addAttribute("viewOnly", true);
        return "operativeb-clinician";
    }

    @GetMapping("/operative-view2-faculty/{id}")
    public String viewFacultyForm10p2(@PathVariable Long id, Model model, Authentication authentication) {
        OperativeChart chart = operativeChartService.getById(id);
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        model.addAttribute("viewOnly", true);
        return "operativec-clinician";
    }

    // ── Faculty: View Form 6 (RESTO6) ────────────────────────────────────────

    @GetMapping("/operative2-view-faculty/{id}")
    public String viewFacultyForm6(@PathVariable Long id, Model model, Authentication authentication) {
        OperativeChart chart = operativeChartService.getById(id);
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        model.addAttribute("viewOnly", true);
        return "operativea-clinician";
    }

    // ── Faculty: Approve / Revise Form 10 ───────────────────────────────────

    @PostMapping("/operative-faculty/{id}/approve")
    public String approveForm1(@PathVariable Long id) {
        OperativeChart chart = operativeChartService.approveForm1(id);
        return "redirect:/chartsview-faculty/" + chart.getPatient().getId();
    }

    @PostMapping("/operative-faculty/{id}/revise")
    public String reviseForm1(@PathVariable Long id) {
        OperativeChart chart = operativeChartService.reviseForm1(id);
        return "redirect:/chartsview-faculty/" + chart.getPatient().getId();
    }

    // ── Faculty: Approve / Revise Form 6 ────────────────────────────────────

    @PostMapping("/operative2-faculty/{id}/approve")
    public String approveForm2(@PathVariable Long id) {
        OperativeChart chart = operativeChartService.approveForm2(id);
        return "redirect:/chartsview-faculty/" + chart.getPatient().getId();
    }

    @PostMapping("/operative2-faculty/{id}/revise")
    public String reviseForm2(@PathVariable Long id) {
        OperativeChart chart = operativeChartService.reviseForm2(id);
        return "redirect:/chartsview-faculty/" + chart.getPatient().getId();
    }
}
