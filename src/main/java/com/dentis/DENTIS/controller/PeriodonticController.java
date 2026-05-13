package com.dentis.DENTIS.controller;

import com.dentis.DENTIS.model.PeriodonticChart;
import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.repository.UserRepository;
import com.dentis.DENTIS.service.PeriodonticChartService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PeriodonticController {

    private final PeriodonticChartService periodonticChartService;
    private final UserRepository userRepository;

    public PeriodonticController(PeriodonticChartService periodonticChartService,
                                 UserRepository userRepository) {
        this.periodonticChartService = periodonticChartService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            String email = oAuth2User.getAttribute("email");
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }

    // ── Clinician: Form A ────────────────────────────────────────────────────

    @GetMapping("/periodontics-a/{id}")
    public String formA(@PathVariable Long id, Model model, Authentication authentication) {
        PeriodonticChart chart = periodonticChartService.getById(id);
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        model.addAttribute("viewOnly", false);
        return "periodonticsa-clinician";
    }

    @GetMapping("/periodontics-a/{id}/view")
    public String formAView(@PathVariable Long id, Model model, Authentication authentication) {
        PeriodonticChart chart = periodonticChartService.getById(id);
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        model.addAttribute("viewOnly", true);
        return "periodonticsa-clinician";
    }

    @PostMapping("/periodontics-a/{id}")
    public String saveFormA(@PathVariable Long id,
                            @ModelAttribute("chart") PeriodonticChart updated) {
        PeriodonticChart chart = periodonticChartService.saveFormA(id, updated);
        // "Next" navigates to Form B
        return "redirect:/periodontics-b/" + id;
    }

    // ── Clinician: Form B ────────────────────────────────────────────────────

    @GetMapping("/periodontics-b/{id}")
    public String formB(@PathVariable Long id, Model model, Authentication authentication) {
        PeriodonticChart chart = periodonticChartService.getById(id);
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        model.addAttribute("viewOnly", false);
        return "periodonticsb-clinician";
    }

    @GetMapping("/periodontics-b/{id}/view")
    public String formBView(@PathVariable Long id, Model model, Authentication authentication) {
        PeriodonticChart chart = periodonticChartService.getById(id);
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        model.addAttribute("viewOnly", true);
        return "periodonticsb-clinician";
    }

    @PostMapping("/periodontics-b/{id}")
    public String saveFormB(@PathVariable Long id,
                            @ModelAttribute("chart") PeriodonticChart updated) {
        PeriodonticChart chart = periodonticChartService.saveFormB(id, updated);
        return "redirect:/chartsview-clinician/" + chart.getPatient().getId();
    }

    // ── Clinician: Form C ────────────────────────────────────────────────────

    @GetMapping("/periodontics-c/{id}")
    public String formC(@PathVariable Long id, Model model, Authentication authentication) {
        PeriodonticChart chart = periodonticChartService.getById(id);
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        model.addAttribute("viewOnly", false);
        return "periodonticsc-clinician";
    }

    @PostMapping("/periodontics-c/{id}")
    public String saveFormC(@PathVariable Long id,
                            @ModelAttribute("chart") PeriodonticChart updated) {
        PeriodonticChart chart = periodonticChartService.saveFormC(id, updated);
        return "redirect:/chartsview-clinician/" + chart.getPatient().getId();
    }

    // ── Clinician: submit PF1 (Form A/B) ────────────────────────────────────

    @PostMapping("/periodontics-ab/{id}/submit")
    public String submitFormAB(@PathVariable Long id) {
        PeriodonticChart chart = periodonticChartService.submitFormAB(id);
        return "redirect:/chartsview-clinician/" + chart.getPatient().getId();
    }

    // ── Clinician: submit PF2 (Form C) ──────────────────────────────────────

    @PostMapping("/periodontics-c/{id}/submit")
    public String submitFormC(@PathVariable Long id) {
        PeriodonticChart chart = periodonticChartService.submitFormC(id);
        return "redirect:/chartsview-clinician/" + chart.getPatient().getId();
    }

    // ── Faculty: View-only (Form C / PF2) ───────────────────────────────────

    @GetMapping("/periodontics-view-faculty/{id}")
    public String viewFaculty(@PathVariable Long id, Model model, Authentication authentication) {
        PeriodonticChart chart = periodonticChartService.getById(id);
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        model.addAttribute("viewOnly", true);
        return "periodonticsc-clinician";
    }

    // ── Faculty: Approve / Revise PF1 (Form A/B) ────────────────────────────

    @PostMapping("/periodontics-faculty/{id}/approve")
    public String approveFormAB(@PathVariable Long id) {
        PeriodonticChart chart = periodonticChartService.approveFormAB(id);
        return "redirect:/chartsview-faculty/" + chart.getPatient().getId();
    }

    @PostMapping("/periodontics-faculty/{id}/revise")
    public String reviseFormAB(@PathVariable Long id) {
        PeriodonticChart chart = periodonticChartService.reviseFormAB(id);
        return "redirect:/chartsview-faculty/" + chart.getPatient().getId();
    }

    // ── Faculty: Approve / Revise PF2 (Form C) ──────────────────────────────

    @PostMapping("/periodontics2-faculty/{id}/approve")
    public String approveFormC(@PathVariable Long id) {
        PeriodonticChart chart = periodonticChartService.approveFormC(id);
        return "redirect:/chartsview-faculty/" + chart.getPatient().getId();
    }

    @PostMapping("/periodontics2-faculty/{id}/revise")
    public String reviseFormC(@PathVariable Long id) {
        PeriodonticChart chart = periodonticChartService.reviseFormC(id);
        return "redirect:/chartsview-faculty/" + chart.getPatient().getId();
    }
}
