package com.dentis.DENTIS.controller;

import com.dentis.DENTIS.model.EndodonticsChart;
import com.dentis.DENTIS.model.EndodonticsChartStatus;
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
        model.addAttribute("viewOnly", chart.getStatus() == EndodonticsChartStatus.APPROVED);
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
        endodonticsChartService.saveForm1(id, updated);
        return "redirect:/endodontics-clinician/" + id + "?saved";   // Save keeps the user on the form
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

    @PostMapping("/endodontics-faculty/{id}/approve")
    public String approveChart(@PathVariable Long id, Authentication authentication) {
        EndodonticsChart owned = endodonticsChartService.getById(id);   // only the faculty assigned to this chart can approve / revise it
        if (!isAssignedFaculty(owned.getFaculty(), authentication))
            return "redirect:/chartsview-faculty/" + owned.getPatient().getId() + "?notAssigned";
        EndodonticsChart chart = endodonticsChartService.approve(id);
        return "redirect:/chartsview-faculty/" + chart.getPatient().getId();
    }

    @PostMapping("/endodontics-faculty/{id}/revise")
    public String reviseChart(@PathVariable Long id, @RequestParam String revisionReason, Authentication authentication) {
        EndodonticsChart owned = endodonticsChartService.getById(id);   // only the faculty assigned to this chart can approve / revise it
        if (!isAssignedFaculty(owned.getFaculty(), authentication))
            return "redirect:/chartsview-faculty/" + owned.getPatient().getId() + "?notAssigned";
        EndodonticsChart chart = endodonticsChartService.revise(id, revisionReason);
        return "redirect:/chartsview-faculty/" + chart.getPatient().getId();
    }

    // ── Form 4 (ENDO4): Patient Information and Informed Consent ────────────

    @GetMapping({"/endodontics4-clinician/{id}", "/endodontics4-clinician/{id}/view"})
    public String consent(@PathVariable Long id, Model model, Authentication authentication,
                          jakarta.servlet.http.HttpServletRequest request) {
        EndodonticsChart chart = endodonticsChartService.getById(id);
        boolean view = request.getRequestURI().endsWith("/view") || !chart.isConsentEditable()
                || !isAssignedClinician(chart, authentication);   // only the chart's clinician fills in and signs it
        if (!view) endodonticsChartService.prefillConsent(chart);
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        model.addAttribute("viewOnly", view);
        return "endodontics4-clinician";
    }

    /** Save the plan and cost. action=patient-sign (from the consent popup) also saves the patient's signature. */
    @PostMapping("/endodontics4-clinician/{id}")
    public String saveConsent(@PathVariable Long id, @RequestParam java.util.Map<String, String> params,
                              @RequestParam(value = "action", defaultValue = "save") String action,
                              Authentication authentication) {
        if (!isAssignedClinician(endodonticsChartService.getById(id), authentication))
            return "redirect:/endodontics4-clinician/" + id + "/view";
        endodonticsChartService.saveConsentPlan(id, params);
        if ("patient-sign".equals(action)) {
            EndodonticsChart chart = endodonticsChartService.patientSignConsent(id, params.get("signerType"),
                    params.get("signerName"), params.get("signature"));
            return "redirect:/endodontics4-clinician/" + id + (chart.isConsentPatientSigned() ? "?patientSigned" : "?incomplete");
        }
        return "redirect:/endodontics4-clinician/" + id + "?saved";
    }

    @PostMapping("/endodontics4-clinician/{id}/clinician-sign")
    public String consentClinicianSign(@PathVariable Long id, @RequestParam(value = "signature", required = false) String signature,
                                       Authentication authentication) {
        if (!isAssignedClinician(endodonticsChartService.getById(id), authentication))
            return "redirect:/endodontics4-clinician/" + id + "/view";
        endodonticsChartService.clinicianSignConsent(id, signature, getCurrentUser(authentication));
        return "redirect:/endodontics4-clinician/" + id + "?signed";
    }

    @PostMapping("/endodontics4-clinician/{id}/restart")
    public String consentRestart(@PathVariable Long id, Authentication authentication) {
        if (!isAssignedClinician(endodonticsChartService.getById(id), authentication))
            return "redirect:/endodontics4-clinician/" + id + "/view";
        endodonticsChartService.restartConsent(id);
        return "redirect:/endodontics4-clinician/" + id + "?restarted";
    }

    @PostMapping("/endodontics4-clinician/{id}/submit")
    public String consentSubmit(@PathVariable Long id, Authentication authentication) {
        if (!isAssignedClinician(endodonticsChartService.getById(id), authentication))
            return "redirect:/endodontics4-clinician/" + id + "/view";
        EndodonticsChart chart = endodonticsChartService.submitConsent(id);
        return "redirect:/chartsview-clinician/" + chart.getPatient().getId();
    }

    @GetMapping("/endodontics4-view-faculty/{id}")
    public String consentFaculty(@PathVariable Long id, Model model, Authentication authentication) {
        EndodonticsChart chart = endodonticsChartService.getById(id);
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        model.addAttribute("viewOnly", true);
        return "endodontics4-clinician";
    }

    @PostMapping("/endodontics4-faculty/{id}/approve")
    public String consentApprove(@PathVariable Long id, @RequestParam(value = "signature", required = false) String signature,
                                 Authentication authentication) {
        EndodonticsChart owned = endodonticsChartService.getById(id);
        if (!isAssignedFaculty(owned.getFaculty(), authentication))
            return "redirect:/chartsview-faculty/" + owned.getPatient().getId() + "?notAssigned";
        if (!com.dentis.DENTIS.service.PeriodonticChartService.isSignature(signature))
            return "redirect:/endodontics4-view-faculty/" + id + "?signRequired";
        endodonticsChartService.approveConsent(id, signature, getCurrentUser(authentication));
        return "redirect:/chartsview-faculty/" + owned.getPatient().getId();
    }

    @PostMapping("/endodontics4-faculty/{id}/revise")
    public String consentRevise(@PathVariable Long id, @RequestParam String revisionReason, Authentication authentication) {
        EndodonticsChart owned = endodonticsChartService.getById(id);
        if (!isAssignedFaculty(owned.getFaculty(), authentication))
            return "redirect:/chartsview-faculty/" + owned.getPatient().getId() + "?notAssigned";
        endodonticsChartService.reviseConsent(id, revisionReason);
        return "redirect:/chartsview-faculty/" + owned.getPatient().getId();
    }

    /** True when the logged-in user is the clinician of this chart. */
    private boolean isAssignedClinician(EndodonticsChart chart, Authentication authentication) {
        User me = getCurrentUser(authentication);
        return me != null && chart.getClinician() != null && chart.getClinician().getId().equals(me.getId());
    }

    /** True when the logged-in user is the faculty assigned to the chart. */
    private boolean isAssignedFaculty(User chartFaculty, Authentication authentication) {
        User me = getCurrentUser(authentication);
        return me != null && chartFaculty != null && chartFaculty.getId().equals(me.getId());
    }
}
