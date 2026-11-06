package com.dentis.DENTIS.controller;

import com.dentis.DENTIS.model.*;
import com.dentis.DENTIS.repository.UserRepository;
import com.dentis.DENTIS.service.OperativeChartService;
import com.dentis.DENTIS.service.RestoEvaluationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/** Operative evaluation forms (RESTO FORM 1, RESTO FORM 2, NOC sealant form), handled like the Perio and Endo evaluations. */
@Controller
public class RestoEvaluationController {

    private static final String TEMPLATE = "resto-eval";

    private final RestoEvaluationService evalService;
    private final OperativeChartService chartService;
    private final UserRepository userRepository;

    public RestoEvaluationController(RestoEvaluationService evalService, OperativeChartService chartService, UserRepository userRepository) {
        this.evalService = evalService; this.chartService = chartService; this.userRepository = userRepository;
    }

    private User me(Authentication a) {
        if (a != null && a.getPrincipal() instanceof OAuth2User u) return userRepository.findByEmail(u.getAttribute("email")).orElse(null);
        return null;
    }

    private boolean isAssigned(OperativeChart c, Authentication a) {
        User u = me(a);
        return u != null && c.getFaculty() != null && c.getFaculty().getId().equals(u.getId());
    }

    private static String toCharts(OperativeChart c, String q) {
        return "redirect:/chartsview-faculty/" + c.getPatient().getId() + (q != null ? "?" + q : "");
    }

    @PostMapping("/resto-eval-faculty/chart/{chartId}/add")
    public String add(@PathVariable Long chartId, @RequestParam("form") String form, Authentication a) {
        OperativeChart chart = chartService.getById(chartId);
        if (!isAssigned(chart, a)) return toCharts(chart, "notAssigned");
        RestoEvalFormType type;
        try { type = RestoEvalFormType.valueOf(form); } catch (IllegalArgumentException e) { throw new ResponseStatusException(HttpStatus.NOT_FOUND); }
        return "redirect:/resto-eval-faculty/" + evalService.create(chart, type).getId();
    }

    @GetMapping("/resto-eval-faculty/{id}")
    public String facultyForm(@PathVariable Long id, Model model, Authentication a) {
        RestoEvaluation e = evalService.getById(id);
        boolean assigned = isAssigned(e.getOperativeChart(), a);
        model.addAttribute("currentUser", me(a));
        model.addAttribute("chart", e.getOperativeChart());
        model.addAttribute("eval", e);
        model.addAttribute("facultyMode", true);
        model.addAttribute("canGrade", assigned);
        model.addAttribute("viewOnly", !assigned || e.isSent());
        return TEMPLATE;
    }

    @PostMapping("/resto-eval-faculty/{id}")
    public String facultySave(@PathVariable Long id, @RequestParam Map<String, String> params,
                              @RequestParam(value = "action", defaultValue = "save") String action, Authentication a) {
        RestoEvaluation e = evalService.getById(id);
        if (!isAssigned(e.getOperativeChart(), a)) return toCharts(e.getOperativeChart(), "notAssigned");
        e = evalService.save(e, params, me(a));
        if ("send".equals(action) && !e.isSent()) {
            List<String> missing = evalService.missingForSend(e);
            if (!missing.isEmpty())
                return "redirect:/resto-eval-faculty/" + id + "?missing=" + java.net.URLEncoder.encode(String.join(", ", missing), java.nio.charset.StandardCharsets.UTF_8);
            evalService.send(e, me(a));
            return "redirect:/resto-eval-faculty/" + id + "?sent";
        }
        return "redirect:/resto-eval-faculty/" + id + "?saved";
    }

    @PostMapping("/resto-eval-faculty/{id}/recall")
    public String recall(@PathVariable Long id, Authentication a) {
        RestoEvaluation e = evalService.getById(id);
        if (!isAssigned(e.getOperativeChart(), a)) return toCharts(e.getOperativeChart(), "notAssigned");
        if (e.isSent()) evalService.recall(e);
        return "redirect:/resto-eval-faculty/" + id + "?recalled";
    }

    @PostMapping("/resto-eval-faculty/{id}/delete")
    public String delete(@PathVariable Long id, Authentication a) {
        RestoEvaluation e = evalService.getById(id);
        OperativeChart c = e.getOperativeChart();
        if (!isAssigned(c, a)) return toCharts(c, "notAssigned");
        evalService.delete(e);
        return toCharts(c, null);
    }

    /** Clinician: read-only, only once sent. */
    @GetMapping("/resto-eval-view/{id}")
    public String view(@PathVariable Long id, Model model, Authentication a) {
        RestoEvaluation e = evalService.getById(id);
        User u = me(a);
        if (u != null && u.getRole() == Role.FACULTY) return "redirect:/resto-eval-faculty/" + id;
        if (!e.isSent()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (u != null && u.getRole() == Role.CLINICIAN) evalService.markViewed(e);
        model.addAttribute("currentUser", u);
        model.addAttribute("chart", e.getOperativeChart());
        model.addAttribute("eval", e);
        model.addAttribute("facultyMode", false);
        model.addAttribute("canGrade", false);
        model.addAttribute("viewOnly", true);
        return TEMPLATE;
    }
}
