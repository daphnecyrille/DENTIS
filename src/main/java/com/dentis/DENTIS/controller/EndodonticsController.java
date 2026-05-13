package com.dentis.DENTIS.controller;

import com.dentis.DENTIS.model.EndodonticsChart;
import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.repository.UserRepository;
import com.dentis.DENTIS.service.EndodonticsChartService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class EndodonticsController {

    private final EndodonticsChartService endodonticsChartService;
    private final UserRepository userRepository;

    public EndodonticsController(EndodonticsChartService endodonticsChartService,
                                 UserRepository userRepository) {
        this.endodonticsChartService = endodonticsChartService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            String email = oAuth2User.getAttribute("email");
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }

    // ── Clinician: Form 1 (Patient Workup) ──────────────────────────────────

    @GetMapping("/endodontics-clinician/{id}")
    public String form1(@PathVariable Long id, Model model, Authentication authentication) {
        EndodonticsChart chart = endodonticsChartService.getById(id);
        if (chart.getChiefComplaint() == null || chart.getChiefComplaint().isBlank()) {
            chart.setChiefComplaint(chart.getPatient().getChiefComplaint());
        }
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        model.addAttribute("viewOnly", false);
        return "endodontics-clinician";
    }

    @GetMapping("/endodontics-clinician/{id}/view")
    public String viewForm1(@PathVariable Long id, Model model, Authentication authentication) {
        EndodonticsChart chart = endodonticsChartService.getById(id);
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        model.addAttribute("viewOnly", true);
        return "endodontics-clinician";
    }

    @PostMapping("/endodontics-clinician/{id}")
    public String saveForm1(@PathVariable Long id,
                            @ModelAttribute("chart") EndodonticsChart updated) {
        EndodonticsChart chart = endodonticsChartService.saveForm1(id, updated);
        return "redirect:/chartsview-clinician/" + chart.getPatient().getId();
    }

    @PostMapping("/endodontics-clinician/{id}/submit")
    public String submitChart(@PathVariable Long id) {
        EndodonticsChart chart = endodonticsChartService.submit(id);
        return "redirect:/chartsview-clinician/" + chart.getPatient().getId();
    }

    // ── Faculty: View-only pages ──────────────────────────────────────────────

    @GetMapping("/endodontics-view-faculty/{id}")
    public String viewForm1Faculty(@PathVariable Long id, Model model, Authentication authentication) {
        EndodonticsChart chart = endodonticsChartService.getById(id);
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        return "endodontics-view-faculty";
    }

    @GetMapping("/endodontics2-view-faculty/{id}")
    public String viewForm2Faculty(@PathVariable Long id, Model model, Authentication authentication) {
        EndodonticsChart chart = endodonticsChartService.getById(id);
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        return "endodontics2-view-faculty";
    }

    // ── Faculty: Form 2 (Endodontic Evaluation) ─────────────────────────────

    @GetMapping("/endodontics2-clinician/{id}")
    public String form2(@PathVariable Long id, Model model, Authentication authentication) {
        EndodonticsChart chart = endodonticsChartService.getById(id);
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        model.addAttribute("viewOnly", false);
        return "endodontics2-clinician";
    }

    @GetMapping("/endodontics2-clinician/{id}/view")
    public String viewForm2(@PathVariable Long id, Model model, Authentication authentication) {
        EndodonticsChart chart = endodonticsChartService.getById(id);
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        model.addAttribute("viewOnly", true);
        return "endodontics2-clinician";
    }

    @PostMapping("/endodontics2-clinician/{id}")
    public String saveForm2(@PathVariable Long id,
                            @ModelAttribute("chart") EndodonticsChart updated) {
        EndodonticsChart chart = endodonticsChartService.saveForm2(id, updated);
        return "redirect:/chartsview-clinician/" + chart.getPatient().getId();
    }

    @PostMapping("/endodontics2-clinician/{id}/submit")
    public String submitForm2(@PathVariable Long id) {
        EndodonticsChart chart = endodonticsChartService.submitForm2(id);
        return "redirect:/chartsview-clinician/" + chart.getPatient().getId();
    }

    @PostMapping("/endodontics-faculty/{id}/approve")
    public String approveChart(@PathVariable Long id) {
        EndodonticsChart chart = endodonticsChartService.approve(id);
        return "redirect:/chartsview-faculty/" + chart.getPatient().getId();
    }

    @PostMapping("/endodontics-faculty/{id}/revise")
    public String reviseChart(@PathVariable Long id) {
        EndodonticsChart chart = endodonticsChartService.revise(id);
        return "redirect:/chartsview-faculty/" + chart.getPatient().getId();
    }

    @PostMapping("/endodontics2-faculty/{id}/approve")
    public String approveForm2(@PathVariable Long id) {
        EndodonticsChart chart = endodonticsChartService.approveForm2(id);
        return "redirect:/chartsview-faculty/" + chart.getPatient().getId();
    }

    @PostMapping("/endodontics2-faculty/{id}/revise")
    public String reviseForm2(@PathVariable Long id) {
        EndodonticsChart chart = endodonticsChartService.reviseForm2(id);
        return "redirect:/chartsview-faculty/" + chart.getPatient().getId();
    }
}
