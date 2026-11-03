package com.dentis.DENTIS.controller;

import com.dentis.DENTIS.model.*;
import com.dentis.DENTIS.repository.UserRepository;
import com.dentis.DENTIS.service.PerioEvalForm;
import com.dentis.DENTIS.service.PerioEvaluationService;
import com.dentis.DENTIS.service.PeriodonticChartService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Objects;

/**
 * Periodontics evaluation forms (PF4, PF5, PF6, PF7, PF17).
 * Faculty fill them in and send them; the clinician only sees a form after it is sent, read-only.
 */
@Controller
public class PerioEvaluationController {

    private final PerioEvaluationService evalService;
    private final PeriodonticChartService chartService;
    private final UserRepository userRepository;

    public PerioEvaluationController(PerioEvaluationService evalService,
                                     PeriodonticChartService chartService,
                                     UserRepository userRepository) {
        this.evalService = evalService;
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

    /** Puts the form layout (criteria, multipliers) and the saved grades on the page. */
    private void addForm(Model model, PerioEvaluation eval) {
        PerioEvalForm form = PerioEvalForm.of(eval.getFormType());
        if (form == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);   // old forms no longer offered
        model.addAttribute("form", form);
        model.addAttribute("grades", evalService.gradeStrings(eval));
    }

    private static final String TEMPLATE = "perio-eval";

    private static void requireSent(PerioEvaluation eval, boolean faculty) {
        if (!faculty && !eval.getStatus().isSent()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);   // drafts stay hidden from clinicians
    }

    private static String toChartsView(PerioEvaluation eval) {
        return "redirect:/chartsview-faculty/" + eval.getPerioChart().getPatient().getId();
    }

    // ── Faculty: add / open / save / send / recall / delete ─────────────────

    /** "+ Add Evaluation": creates the chosen form for the chart and opens it. */
    @PostMapping("/perio-eval-faculty/chart/{chartId}/add")
    public String facultyAdd(@PathVariable Long chartId, @RequestParam("form") String form) {
        PerioEvalFormType type;
        try { type = PerioEvalFormType.valueOf(form); }
        catch (IllegalArgumentException ex) { throw new ResponseStatusException(HttpStatus.NOT_FOUND); }
        if (!type.isOffered() || !type.hasPage()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        PerioEvaluation eval = evalService.create(chartService.getById(chartId), type);
        return "redirect:/perio-eval-faculty/" + eval.getId();
    }

    @GetMapping("/perio-eval-faculty/{evalId}")
    public String facultyForm(@PathVariable Long evalId, Model model, Authentication authentication) {
        PerioEvaluation eval = evalService.getById(evalId);
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", eval.getPerioChart());
        model.addAttribute("eval", eval);
        model.addAttribute("viewOnly", eval.getStatus().isSent());   // sent forms are locked until recalled
        model.addAttribute("facultyMode", true);
        addForm(model, eval);
        return TEMPLATE;
    }

    @PostMapping("/perio-eval-faculty/{evalId}")
    public String facultySave(@PathVariable Long evalId,
                              @RequestParam Map<String, String> params,
                              @RequestParam(value = "action", defaultValue = "save") String action) {
        PerioEvaluation eval = evalService.getById(evalId);
        eval = evalService.saveGrades(eval, params);
        if ("send".equals(action) && !eval.getStatus().isSent()) {
            evalService.send(eval);
            return "redirect:/perio-eval-faculty/" + evalId + "?sent";
        }
        return "redirect:/perio-eval-faculty/" + evalId + "?saved";
    }

    @PostMapping("/perio-eval-faculty/{evalId}/recall")
    public String facultyRecall(@PathVariable Long evalId) {
        PerioEvaluation eval = evalService.getById(evalId);
        if (eval.getStatus().isSent()) evalService.recall(eval);
        return "redirect:/perio-eval-faculty/" + evalId + "?recalled";
    }

    /** Deletes a draft or a sent evaluation (the charts view asks for confirmation first). */
    @PostMapping("/perio-eval-faculty/{evalId}/delete")
    public String facultyDelete(@PathVariable Long evalId) {
        PerioEvaluation eval = evalService.getById(evalId);
        String back = toChartsView(eval);
        evalService.delete(eval);
        return back;
    }

    // ── Clinician: Evaluations page (every evaluation sent to them) ─────────

    @GetMapping("/evaluations-clinician")
    public String clinicianEvaluations(Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        model.addAttribute("currentUser", user);
        model.addAttribute("evals", evalService.sentForClinician(user));
        model.addAttribute("unreadCount", evalService.unreadCount(user));
        return "evaluations-clinician";
    }

    // ── Clinician: read-only, sent forms only (faculty can also open this view) ──

    @GetMapping("/perio-eval-view/{evalId}")
    public String view(@PathVariable Long evalId, Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        PerioEvaluation eval = evalService.getById(evalId);
        PeriodonticChart chart = eval.getPerioChart();
        boolean faculty = user != null && user.getRole() == Role.FACULTY;
        boolean clinician = user != null && user.getRole() == Role.CLINICIAN;
        if (!faculty && !clinician) throw new ResponseStatusException(HttpStatus.FORBIDDEN);   // evaluations are only between faculty and clinician
        requireSent(eval, faculty);
        if (user != null && user.getRole() == Role.CLINICIAN
                && (chart.getClinician() == null || !Objects.equals(chart.getClinician().getId(), user.getId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);   // only the patient's own clinician
        }
        if (user != null && user.getRole() == Role.CLINICIAN) evalService.markViewed(eval);   // no longer "New"
        model.addAttribute("currentUser", user);
        model.addAttribute("chart", chart);
        model.addAttribute("eval", eval);
        model.addAttribute("viewOnly", true);
        model.addAttribute("facultyMode", faculty);
        addForm(model, eval);
        return TEMPLATE;
    }
}
