package com.dentis.DENTIS.service;

import com.dentis.DENTIS.model.*;
import com.dentis.DENTIS.repository.PerioEvaluationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PerioEvaluationService {

    private final PerioEvaluationRepository repo;

    public PerioEvaluationService(PerioEvaluationRepository repo) {
        this.repo = repo;
    }

    /** Auto-generates all evaluation form records (DRAFT) for a chart if not already present. No longer called on chart creation. */
    public void initForChart(PeriodonticChart chart) {
        if (repo.existsByPerioChart(chart)) return;
        for (PerioEvalFormType type : PerioEvalFormType.values()) {
            PerioEvaluation eval = new PerioEvaluation();
            eval.setPerioChart(chart);
            eval.setFormType(type);
            eval.setStatus(PerioEvalStatus.DRAFT);
            repo.save(eval);
        }
    }

    public List<PerioEvaluation> getByChart(PeriodonticChart chart) {
        return repo.findByPerioChartOrderByFormTypeAsc(chart);
    }

    /** Returns only sent evaluations (for clinician read-only view). */
    public List<PerioEvaluation> getGradedByChart(PeriodonticChart chart) {
        return repo.findByPerioChartAndStatus(chart, PerioEvalStatus.GRADED);
    }

    // ── Evaluation Forms table (charts views) ───────────────────────────────

    /** Evaluations the faculty added for this chart, oldest first. Blank records made by the old auto-create (no date) are skipped. */
    public List<PerioEvaluation> listForChart(PeriodonticChart chart) {
        return repo.findByPerioChartOrderByCreatedAtAsc(chart).stream()
                .filter(e -> e.getDate() != null && e.getFormType().isOffered())
                .toList();
    }

    /** Number of evaluations added across the given charts (for the empty-table message). */
    public int countForCharts(List<PeriodonticChart> charts) {
        return charts == null ? 0 : charts.stream().mapToInt(c -> listForChart(c).size()).sum();
    }

    /** Number of sent evaluations across the given charts (clinician view). */
    public int countSentForCharts(List<PeriodonticChart> charts) {
        return charts == null ? 0 : charts.stream().mapToInt(c -> listSentForChart(c).size()).sum();
    }

    /** Only the evaluations already sent to the clinician. */
    public List<PerioEvaluation> listSentForChart(PeriodonticChart chart) {
        return listForChart(chart).stream().filter(e -> e.getStatus().isSent()).toList();
    }

    /** Forms offered in the "+ Add Evaluation" menu, in order. */
    public List<PerioEvalFormType> offeredForms() {
        return java.util.Arrays.stream(PerioEvalFormType.values()).filter(PerioEvalFormType::isOffered).toList();
    }

    /** Creates a new evaluation (draft, dated today, PF1 diagnosis filled in). Numbered when the same form is added again. */
    public PerioEvaluation create(PeriodonticChart chart, PerioEvalFormType type) {
        List<PerioEvaluation> existing = listForChart(chart).stream().filter(e -> e.getFormType() == type).toList();
        PerioEvaluation e = new PerioEvaluation();
        e.setPerioChart(chart);
        e.setFormType(type);
        e.setStatus(PerioEvalStatus.DRAFT);
        e.setVisitNo(existing.stream().mapToInt(x -> x.getVisitNo() == null ? 1 : x.getVisitNo()).max().orElse(0) + 1);
        e.setDate(LocalDate.now());
        if (chart.getDiagnosis() != null) e.setDiagnosis(chart.getDiagnosis());
        return repo.save(e);
    }

    public void delete(PerioEvaluation e) {
        repo.delete(e);
    }

    public PerioEvaluation getById(Long id) {
        return repo.findById(id).orElseThrow();
    }

    public PerioEvaluation save(PerioEvaluation eval) {
        return repo.save(eval);
    }

    // ── Saving grades (all forms) ──────────────────────────────────────────

    /** Copies the submitted fields, recomputes the total and saves. Ignored once sent. */
    public PerioEvaluation saveGrades(PerioEvaluation e, Map<String, String> p) {
        if (e.getStatus().isSent()) return e;
        PerioEvalForm form = PerioEvalForm.of(e.getFormType());
        e.setDate(date(p.get("date")));
        e.setDiagnosis(p.get("diagnosis"));
        e.setRemarks(p.get("remarks"));
        Map<String, Double> g = new LinkedHashMap<>();
        form.getLeft().forEach(c -> put(g, (String) c.get("field"), p.get((String) c.get("field"))));
        form.getRight().forEach(c -> PerioEvalForm.ATTEMPTS.forEach(a ->
            put(g, c.get("field") + a, p.get(c.get("field") + a))));
        if (form.isHasRootPlaning() && p.get(PerioEvalForm.RP_NA) != null) {
            g.put(PerioEvalForm.RP_NA, 1.0);
            PerioEvalForm.ATTEMPTS.forEach(a -> g.remove(form.getRootPlaningField() + a));
        }
        e.setGradesJson(toJson(g));
        e.setTotalScore(form.total(g));
        return repo.save(e);
    }

    /** Grades for display: field -> "4" / "4.5" (blank fields absent). Falls back to the older Integer columns. */
    public Map<String, String> gradeStrings(PerioEvaluation e) {
        Map<String, Double> g = fromJson(e.getGradesJson());
        if (g.isEmpty() && e.getFormType() == PerioEvalFormType.PF4) g = legacyPf4(e);
        Map<String, String> out = new LinkedHashMap<>();
        g.forEach((k, v) -> out.put(k, v == Math.floor(v) ? String.valueOf(v.intValue()) : String.valueOf(v)));
        return out;
    }

    private static Map<String, Double> legacyPf4(PerioEvaluation e) {
        Map<String, Double> g = new LinkedHashMap<>();
        Object[][] f = {
            {"determinedHistory", e.getDeterminedHistory()}, {"assessedPerioVisually", e.getAssessedPerioVisually()},
            {"assessedOralHygiene", e.getAssessedOralHygiene()}, {"appliedClinicalParameters", e.getAppliedClinicalParameters()},
            {"diagnosedClassified", e.getDiagnosedClassified()}, {"formulatedTreatmentPlan", e.getFormulatedTreatmentPlan()},
            {"healthEd1st", e.getHealthEd1st()}, {"healthEd2nd", e.getHealthEd2nd()}, {"healthEd3rd", e.getHealthEd3rd()},
            {"instrumentation1st", e.getInstrumentation1st()}, {"instrumentation2nd", e.getInstrumentation2nd()}, {"instrumentation3rd", e.getInstrumentation3rd()},
            {"debridementTissues1st", e.getDebridementTissues1st()}, {"debridementTissues2nd", e.getDebridementTissues2nd()}, {"debridementTissues3rd", e.getDebridementTissues3rd()},
            {"infectionControlRight1st", e.getInfectionControlRight1st()}, {"infectionControlRight2nd", e.getInfectionControlRight2nd()}, {"infectionControlRight3rd", e.getInfectionControlRight3rd()},
            {"awareness1st", e.getAwareness1st()}, {"awareness2nd", e.getAwareness2nd()}, {"awareness3rd", e.getAwareness3rd()}};
        for (Object[] x : f) if (x[1] != null) g.put((String) x[0], ((Integer) x[1]).doubleValue());
        return g;
    }

    // ── New (unread) evaluations for the clinician ──────────────────────────

    /** Sent evaluations this clinician hasn't opened yet, newest first (clinician dashboard). */
    public List<PerioEvaluation> unreadForClinician(User clinician) {
        if (clinician == null) return List.of();
        return repo.findByPerioChart_ClinicianAndStatusAndViewedAtIsNullOrderBySentAtDesc(clinician, PerioEvalStatus.GRADED)
                .stream().filter(e -> e.getDate() != null && e.getFormType().isOffered()).toList();
    }

    /** Every evaluation sent to this clinician (Evaluations page): unread first, then newest first. */
    public List<PerioEvaluation> sentForClinician(User clinician) {
        if (clinician == null) return List.of();
        List<PerioEvaluation> list = new java.util.ArrayList<>(
            repo.findByPerioChart_ClinicianAndStatusOrderBySentAtDesc(clinician, PerioEvalStatus.GRADED).stream()
                .filter(e -> e.getDate() != null && e.getFormType().isOffered()).toList());
        list.sort(java.util.Comparator.comparing((PerioEvaluation e) -> e.getViewedAt() != null));   // stable: keeps newest-first within each group
        return list;
    }

    /** Number of unread evaluations (menu badge). */
    public int unreadCount(User clinician) {
        return unreadForClinician(clinician).size();
    }

    /** Called when the clinician opens a sent evaluation: it stops showing as new. */
    public void markViewed(PerioEvaluation e) {
        if (e.getViewedAt() == null && e.getStatus().isSent()) {
            e.setViewedAt(LocalDateTime.now());
            repo.save(e);
        }
    }

    // ── Send / recall ───────────────────────────────────────────────────────

    /** Makes the evaluation visible (read-only) to the clinician; it shows as new until they open it. */
    public PerioEvaluation send(PerioEvaluation e) {
        e.setStatus(PerioEvalStatus.GRADED);
        e.setSentAt(LocalDateTime.now());
        e.setViewedAt(null);
        return repo.save(e);
    }

    /** Takes a sent evaluation back so the faculty can edit it; the clinician no longer sees it until it is sent again. */
    public PerioEvaluation recall(PerioEvaluation e) {
        e.setStatus(PerioEvalStatus.DRAFT);
        e.setSentAt(null);
        e.setViewedAt(null);
        return repo.save(e);
    }

    // kept for older callers
    public PerioEvaluation grade(PerioEvaluation eval) { return send(eval); }
    public PerioEvaluation resetToDraft(PerioEvaluation eval) { return recall(eval); }

    // ── helpers ─────────────────────────────────────────────────────────────

    private static final com.fasterxml.jackson.databind.ObjectMapper JSON = new com.fasterxml.jackson.databind.ObjectMapper();

    /** Accepts a grade from 1 to 5, decimals allowed (rounded to 2 places); anything else is left blank. */
    private static void put(Map<String, Double> g, String key, String v) {
        if (v == null || v.isBlank()) return;
        try {
            double d = Double.parseDouble(v.trim());
            if (d >= 1 && d <= 5) g.put(key, round2(d));
        } catch (NumberFormatException ignored) { }
    }

    private static String toJson(Map<String, Double> g) {
        try { return JSON.writeValueAsString(g); } catch (Exception ex) { return "{}"; }
    }

    private static Map<String, Double> fromJson(String s) {
        if (s == null || s.isBlank()) return new LinkedHashMap<>();
        try {
            return JSON.readValue(s, new com.fasterxml.jackson.core.type.TypeReference<LinkedHashMap<String, Double>>() {});
        } catch (Exception ex) { return new LinkedHashMap<>(); }
    }

    private static double round2(double d) { return Math.round(d * 100.0) / 100.0; }

    private static Map<String, Integer> linked(Object... kv) {
        Map<String, Integer> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put((String) kv[i], (Integer) kv[i + 1]);
        return java.util.Collections.unmodifiableMap(m);
    }

    private static LocalDate date(String v) {
        try { return (v == null || v.isBlank()) ? null : LocalDate.parse(v); }
        catch (Exception ex) { return null; }
    }

}
