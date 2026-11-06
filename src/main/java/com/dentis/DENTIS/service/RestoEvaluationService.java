package com.dentis.DENTIS.service;

import com.dentis.DENTIS.model.*;
import com.dentis.DENTIS.repository.RestoEvaluationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/** Operative evaluation forms (RESTO1, RESTO2, NOC): the faculty adds, grades and sends them; clinicians see sent ones. */
@Service
@Transactional
public class RestoEvaluationService {

    private final RestoEvaluationRepository repo;

    public RestoEvaluationService(RestoEvaluationRepository repo) { this.repo = repo; }

    public RestoEvaluation getById(Long id) { return repo.findById(id).orElseThrow(); }

    public List<RestoEvaluation> listForChart(OperativeChart c) { return repo.findByOperativeChartOrderByCreatedAtAsc(c); }

    public List<RestoEvaluation> listSentForChart(OperativeChart c) {
        return listForChart(c).stream().filter(RestoEvaluation::isSent).toList();
    }

    public int countForCharts(List<OperativeChart> charts) {
        return charts == null ? 0 : charts.stream().mapToInt(c -> listForChart(c).size()).sum();
    }

    public int countSentForCharts(List<OperativeChart> charts) {
        return charts == null ? 0 : charts.stream().mapToInt(c -> listSentForChart(c).size()).sum();
    }

    public List<RestoEvalFormType> offeredForms() { return List.of(RestoEvalFormType.values()); }

    public RestoEvaluation create(OperativeChart chart, RestoEvalFormType type) {
        RestoEvaluation e = new RestoEvaluation();
        e.setOperativeChart(chart);
        e.setFormType(type);
        e.setVisitNo((int) listForChart(chart).stream().filter(x -> x.getFormType() == type).count() + 1);
        e.setEvalDate(LocalDate.now());
        return repo.save(e);
    }

    /** Saves the draft. A part whose rating changes gets the faculty's name and today's date. */
    public RestoEvaluation save(RestoEvaluation e, Map<String, String> p, User faculty) {
        if (e.isSent()) return e;   // locked until recalled
        e.setToothSurface(blank(p.get("toothSurface")));
        if (e.getFormType() == RestoEvalFormType.RESTO1) {
            e.setRestoMaterial(blank(p.get("restoMaterial")));
            e.setCavityClass(oneOf(p.get("cavityClass"), "I/III", "II/IV", "V", "ONLAY"));
            e.setCavitySize(oneOf(p.get("cavitySize"), "SHALLOW_050", "SHALLOW_075", "MODERATE", "EXTENSIVE"));
        }
        if (e.getFormType() == RestoEvalFormType.RESTO2) e.setCaseType(oneOf(p.get("caseType"), "PATIENT", "SIMULATED"));
        try { e.setEvalDate(blank(p.get("evalDate")) != null ? LocalDate.parse(p.get("evalDate")) : LocalDate.now()); } catch (Exception ignored) { }

        List<String> allowed = e.getFormType().getRatings();
        Map<String, Map<String, String>> stamps = e.getStamps();
        for (int i = 0; i < e.getFormType().getParts().size(); i++) {
            String v = p.get("r" + (i + 1));
            String nv = v != null && allowed.contains(v) ? v : null;
            if (!Objects.equals(nv, e.getRating(i))) {
                e.setRating(i, nv);
                if (nv == null) stamps.remove(String.valueOf(i + 1));
                else stamps.put(String.valueOf(i + 1), Map.of("by", faculty != null ? faculty.getFullName() : "Faculty", "at", LocalDate.now().toString()));
            }
        }
        e.setStamps(stamps);
        e.setEpaLevel(oneOf(p.get("epaLevel"), "EPA Level 1", "EPA Level 2", "EPA Level 3", "EPA Level 4", "EPA Level 5"));
        e.setRemarks(blank(p.get("remarks")));
        return repo.save(e);
    }

    public List<String> missingForSend(RestoEvaluation e) {
        List<String> out = new ArrayList<>();
        List<RestoEvalFormType.Part> parts = e.getFormType().getParts();
        for (int i = 0; i < parts.size(); i++) if (e.getRating(i) == null) out.add(parts.get(i).no() + " " + parts.get(i).name());
        if (e.getFormType() == RestoEvalFormType.RESTO1) {
            if (e.getCavityClass() == null) out.add("cavity classification");
            if (e.getCavitySize() == null) out.add("cavity size");
        }
        if (e.getFormType() == RestoEvalFormType.RESTO2 && e.getCaseType() == null) out.add("patient or simulation");
        if (e.getEpaLevel() == null) out.add("Over-all EPA");
        return out;
    }

    public RestoEvaluation send(RestoEvaluation e, User faculty) {
        e.setSentAt(LocalDateTime.now());
        e.setSentBy(faculty != null ? faculty.getFullName() : null);
        e.setViewedAt(null);
        return repo.save(e);
    }

    public RestoEvaluation recall(RestoEvaluation e) { e.setSentAt(null); return repo.save(e); }

    public void delete(RestoEvaluation e) { repo.delete(e); }

    public void markViewed(RestoEvaluation e) {
        if (e.isSent() && e.getViewedAt() == null) { e.setViewedAt(LocalDateTime.now()); repo.save(e); }
    }

    public List<RestoEvaluation> sentForClinician(User c) {
        return c == null ? List.of() : repo.findByOperativeChart_ClinicianAndSentAtIsNotNullOrderBySentAtDesc(c);
    }

    public int unreadCount(User c) {
        return c == null ? 0 : (int) repo.countByOperativeChart_ClinicianAndSentAtIsNotNullAndViewedAtIsNull(c);
    }

    private static String blank(String s) { return s == null || s.isBlank() ? null : s.trim(); }

    private static String oneOf(String v, String... options) {
        if (v == null) return null;
        for (String o : options) if (o.equals(v)) return o;
        return null;
    }
}
