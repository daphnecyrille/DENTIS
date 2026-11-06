package com.dentis.DENTIS.controller;

import com.dentis.DENTIS.model.EndoEvaluation;
import com.dentis.DENTIS.model.EndodonticsChart;
import com.dentis.DENTIS.model.Role;
import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.repository.UserRepository;
import com.dentis.DENTIS.service.EndoEvaluationService;
import com.dentis.DENTIS.service.EndodonticsChartService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * ENDO2 – Endodontic Evaluation & Treatment Planning, handled like the Perio evaluation forms:
 * the assigned faculty adds it (one per tooth checked), saves drafts and sends it; the clinician sees it once sent, read-only.
 */
@Controller
public class EndoEvaluationController {

    private static final String TEMPLATE = "endo-eval";

    private final EndoEvaluationService evalService;
    private final EndodonticsChartService chartService;
    private final UserRepository userRepository;

    public EndoEvaluationController(EndoEvaluationService evalService, EndodonticsChartService chartService,
                                    UserRepository userRepository) {
        this.evalService = evalService;
        this.chartService = chartService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User u)
            return userRepository.findByEmail(u.getAttribute("email")).orElse(null);
        return null;
    }

    private boolean isAssigned(EndodonticsChart chart, Authentication authentication) {
        User me = getCurrentUser(authentication);
        return me != null && chart.getFaculty() != null && chart.getFaculty().getId().equals(me.getId());
    }

    private static String toChartsView(EndodonticsChart chart, String q) {
        return "redirect:/chartsview-faculty/" + chart.getPatient().getId() + (q != null ? "?" + q : "");
    }

    // ── Faculty ─────────────────────────────────────────────────────────────

    @PostMapping("/endo-eval-faculty/chart/{chartId}/add")
    public String add(@PathVariable Long chartId, Authentication authentication) {
        EndodonticsChart chart = chartService.getById(chartId);
        if (!isAssigned(chart, authentication)) return toChartsView(chart, "notAssigned");
        return "redirect:/endo-eval-faculty/" + evalService.create(chart).getId();
    }

    @GetMapping("/endo-eval-faculty/{id}")
    public String facultyForm(@PathVariable Long id, Model model, Authentication authentication) {
        EndoEvaluation eval = evalService.getById(id);
        boolean assigned = isAssigned(eval.getEndoChart(), authentication);
        if (assigned) evalService.prefillFromWorkup(eval);   // shown on the page; saved with Save draft / Send
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("chart", eval.getEndoChart());
        model.addAttribute("eval", eval);
        model.addAttribute("facultyMode", true);
        model.addAttribute("canGrade", assigned);
        model.addAttribute("viewOnly", !assigned || eval.isSent());
        return TEMPLATE;
    }

    /** Save draft (action=save) or send to the clinician (action=send). */
    @PostMapping("/endo-eval-faculty/{id}")
    public String facultySave(@PathVariable Long id, @RequestParam Map<String, String> params,
                              @RequestParam(value = "action", defaultValue = "save") String action,
                              Authentication authentication) {
        EndoEvaluation eval = evalService.getById(id);
        if (!isAssigned(eval.getEndoChart(), authentication)) return toChartsView(eval.getEndoChart(), "notAssigned");
        eval = evalService.save(eval, params);
        if ("send".equals(action) && !eval.isSent()) {
            List<String> missing = evalService.missingForSend(eval);
            if (!missing.isEmpty())
                return "redirect:/endo-eval-faculty/" + id + "?missing="
                        + java.net.URLEncoder.encode(String.join(", ", missing), java.nio.charset.StandardCharsets.UTF_8);
            evalService.send(eval);
            return "redirect:/endo-eval-faculty/" + id + "?sent";
        }
        return "redirect:/endo-eval-faculty/" + id + "?saved";
    }

    @PostMapping("/endo-eval-faculty/{id}/recall")
    public String recall(@PathVariable Long id, Authentication authentication) {
        EndoEvaluation eval = evalService.getById(id);
        if (!isAssigned(eval.getEndoChart(), authentication)) return toChartsView(eval.getEndoChart(), "notAssigned");
        if (eval.isSent()) evalService.recall(eval);
        return "redirect:/endo-eval-faculty/" + id + "?recalled";
    }

    @PostMapping("/endo-eval-faculty/{id}/delete")
    public String delete(@PathVariable Long id, Authentication authentication) {
        EndoEvaluation eval = evalService.getById(id);
        EndodonticsChart chart = eval.getEndoChart();
        if (!isAssigned(chart, authentication)) return toChartsView(chart, "notAssigned");
        evalService.delete(eval);
        return toChartsView(chart, null);
    }

    // ── Clinician: read-only, only once sent ────────────────────────────────

    @GetMapping("/endo-eval-view/{id}")
    public String view(@PathVariable Long id, Model model, Authentication authentication) {
        EndoEvaluation eval = evalService.getById(id);
        User me = getCurrentUser(authentication);
        if (me != null && me.getRole() == Role.FACULTY) return "redirect:/endo-eval-faculty/" + id;
        if (!eval.isSent()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);   // drafts stay hidden from clinicians
        if (me != null && me.getRole() == Role.CLINICIAN) evalService.markViewed(eval);
        model.addAttribute("currentUser", me);
        model.addAttribute("chart", eval.getEndoChart());
        model.addAttribute("eval", eval);
        model.addAttribute("facultyMode", false);
        model.addAttribute("canGrade", false);
        model.addAttribute("viewOnly", true);
        return TEMPLATE;
    }
}
