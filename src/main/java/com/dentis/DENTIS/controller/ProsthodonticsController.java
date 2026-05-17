package com.dentis.DENTIS.controller;

import com.dentis.DENTIS.model.ProsthodonticsChart;
import com.dentis.DENTIS.model.ProsthodonticsChartStatus;
import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.repository.UserRepository;
import com.dentis.DENTIS.service.ProsthodonticsChartService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class ProsthodonticsController {

    private final ProsthodonticsChartService chartService;
    private final UserRepository userRepository;

    public ProsthodonticsController(ProsthodonticsChartService chartService,
                                    UserRepository userRepository) {
        this.chartService = chartService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            String email = oAuth2User.getAttribute("email");
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }

    // ── Clinician: view/edit form ─────────────────────────────────────────────

    @GetMapping("/prosthodontics-clinician/{id}")
    public String form(@PathVariable Long id, Model model, Authentication authentication) {
        ProsthodonticsChart chart = chartService.getById(id);
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chart);
        model.addAttribute("viewOnly", chart.getStatus() == ProsthodonticsChartStatus.APPROVED);
        return "prosthodontics-clinician";
    }

    @GetMapping("/prosthodontics-clinician/{id}/view")
    public String viewForm(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chartService.getById(id));
        model.addAttribute("viewOnly", true);
        return "prosthodontics-clinician";
    }

    @PostMapping("/prosthodontics-clinician/{id}")
    public String save(@PathVariable Long id,
                       @ModelAttribute("chart") ProsthodonticsChart updated,
                       @RequestParam(required = false) MultipartFile patientConsentFile,
                       @RequestParam(required = false) MultipartFile patientWorkupFile,
                       @RequestParam(required = false) MultipartFile primaryImpressionFile,
                       @RequestParam(required = false) MultipartFile individualTrayFile,
                       @RequestParam(required = false) MultipartFile finalImpressionFile,
                       @RequestParam(required = false) MultipartFile waxPatternLabFile,
                       @RequestParam(required = false) MultipartFile waxPatternDiagFile,
                       @RequestParam(required = false) MultipartFile metalFrameworkFile,
                       @RequestParam(required = false) MultipartFile trialDentureBaseFile,
                       @RequestParam(required = false) MultipartFile ocrFile,
                       @RequestParam(required = false) MultipartFile positionalRelationsFile,
                       @RequestParam(required = false) MultipartFile mountingFile,
                       @RequestParam(required = false) MultipartFile anteriorSetupFile,
                       @RequestParam(required = false) MultipartFile anteriorTryInFile,
                       @RequestParam(required = false) MultipartFile anteriorTryInSignatureFile,
                       @RequestParam(required = false) MultipartFile posteriorSetupFile,
                       @RequestParam(required = false) MultipartFile posteriorTryInFile,
                       @RequestParam(required = false) MultipartFile posteriorTryInSignatureFile,
                       @RequestParam(required = false) MultipartFile waxUpFile,
                       @RequestParam(required = false) MultipartFile processingFile,
                       @RequestParam(required = false) MultipartFile cefFile,
                       @RequestParam(required = false) MultipartFile installationFile,
                       @RequestParam(required = false) MultipartFile installationSignatureFile,
                       @RequestParam(required = false) MultipartFile postInstallationFile,
                       @RequestParam(required = false) MultipartFile postInstallationSignatureFile,
                       @RequestParam(required = false, defaultValue = "save") String action) throws IOException {

        ProsthodonticsChart chart = chartService.save(id, updated,
                patientConsentFile, patientWorkupFile, primaryImpressionFile,
                individualTrayFile, finalImpressionFile, waxPatternLabFile,
                waxPatternDiagFile, metalFrameworkFile, trialDentureBaseFile,
                ocrFile, positionalRelationsFile, mountingFile,
                anteriorSetupFile, anteriorTryInFile, anteriorTryInSignatureFile,
                posteriorSetupFile, posteriorTryInFile, posteriorTryInSignatureFile,
                waxUpFile, processingFile, cefFile,
                installationFile, installationSignatureFile,
                postInstallationFile, postInstallationSignatureFile);

        if ("submit".equals(action)) {
            chartService.submit(id);
        }
        return "redirect:/chartsview-clinician/" + chart.getPatient().getId();
    }

    /** Submit-only endpoint used from chartsview — does NOT touch form data. */
    @PostMapping("/prosthodontics-clinician/{id}/submit")
    public String submitOnly(@PathVariable Long id) {
        ProsthodonticsChart chart = chartService.submit(id);
        return "redirect:/chartsview-clinician/" + chart.getPatient().getId();
    }

    // ── Faculty: view form ────────────────────────────────────────────────────

    @GetMapping("/prosthodontics-faculty/{id}")
    public String facultyView(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chartService.getById(id));
        return "prosthodontics-view-faculty";
    }

    // ── Clinic Manager: view form ─────────────────────────────────────────────

    @GetMapping("/prosthodontics-view/{id}")
    public String clinicManagerView(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", chartService.getById(id));
        return "prosthodontics-view-faculty";
    }

    @PostMapping("/prosthodontics-faculty/{id}/approve")
    public String approve(@PathVariable Long id) {
        ProsthodonticsChart chart = chartService.approve(id);
        return "redirect:/chartsview-faculty/" + chart.getPatient().getId();
    }

    @PostMapping("/prosthodontics-faculty/{id}/revise")
    public String revise(@PathVariable Long id, @RequestParam String revisionReason) {
        ProsthodonticsChart chart = chartService.revise(id, revisionReason);
        return "redirect:/chartsview-faculty/" + chart.getPatient().getId();
    }

    @PostMapping("/prosthodontics-faculty/{id}/grade")
    public String saveGrade(@PathVariable Long id, @RequestParam String grade) {
        ProsthodonticsChart chart = chartService.saveGrade(id, grade);
        return "redirect:/prosthodontics-faculty/" + id + "?graded=true";
    }
}
