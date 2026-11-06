package com.dentis.DENTIS.service;

import com.dentis.DENTIS.model.EndoEvaluation;
import com.dentis.DENTIS.model.EndodonticsChart;
import com.dentis.DENTIS.model.EndodonticsChartStatus;
import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.repository.EndoEvaluationRepository;
import com.dentis.DENTIS.repository.EndodonticsChartRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** ENDO2 evaluations: the faculty adds, grades and sends them; the clinician only sees sent ones. */
@Service
@Transactional
public class EndoEvaluationService {

    private final EndoEvaluationRepository repo;
    private final EndodonticsChartRepository chartRepo;

    public EndoEvaluationService(EndoEvaluationRepository repo, EndodonticsChartRepository chartRepo) {
        this.repo = repo;
        this.chartRepo = chartRepo;
    }

    public EndoEvaluation getById(Long id) { return repo.findById(id).orElseThrow(); }

    public List<EndoEvaluation> listForChart(EndodonticsChart chart) { return repo.findByEndoChartOrderByCreatedAtAsc(chart); }

    public List<EndoEvaluation> listSentForChart(EndodonticsChart chart) {
        return listForChart(chart).stream().filter(EndoEvaluation::isSent).toList();
    }

    public int countForCharts(List<EndodonticsChart> charts) {
        return charts == null ? 0 : charts.stream().mapToInt(c -> listForChart(c).size()).sum();
    }

    public int countSentForCharts(List<EndodonticsChart> charts) {
        return charts == null ? 0 : charts.stream().mapToInt(c -> listSentForChart(c).size()).sum();
    }

    /** "+ Add Evaluation": a new draft, with the tooth and diagnosis of the Patient Workup for the first one. */
    public EndoEvaluation create(EndodonticsChart chart) {
        List<EndoEvaluation> existing = listForChart(chart);
        EndoEvaluation e = new EndoEvaluation();
        e.setEndoChart(chart);
        e.setVisitNo(existing.stream().mapToInt(x -> x.getVisitNo() != null ? x.getVisitNo() : 1).max().orElse(0) + 1);
        if (existing.isEmpty()) prefillFromWorkup(e);
        e.setEvalDate(LocalDate.now());
        e.setEvalTime(LocalTime.now().withSecond(0).withNano(0));
        return repo.save(e);
    }

    /** First ENDO2 of a chart: an empty tooth # / diagnosis is taken from the Patient Workup (ENDO1).
     *  Also used when the form is opened, so a workup filled in later still shows up. */
    public void prefillFromWorkup(EndoEvaluation e) {
        if (e.isSent() || (e.getVisitNo() != null && e.getVisitNo() > 1)) return;
        EndodonticsChart c = e.getEndoChart();
        if ((e.getToothNumber() == null || e.getToothNumber().isBlank()) && c.getToothNumber() != null && !c.getToothNumber().isBlank())
            e.setToothNumber(c.getToothNumber());
        if (e.getDiagnosis() == null || e.getDiagnosis().isBlank()) {
            String d = java.util.stream.Stream.of(c.getPulpalDiagnosis(), c.getPeriapicalDiagnosis())
                    .filter(x -> x != null && !x.isBlank()).collect(java.util.stream.Collectors.joining("; "));
            if (!d.isEmpty()) e.setDiagnosis(d);
        }
    }

    public EndoEvaluation save(EndoEvaluation e, Map<String, String> p) {
        if (e.isSent()) return e;   // locked until recalled
        e.setToothNumber(blank(p.get("toothNumber")));
        e.setDiagnosis(blank(p.get("diagnosis")));
        String[] max = EndoEvaluation.MAX;
        e.setScore1(score(p.get("score1"), max[0]));
        e.setScore2(score(p.get("score2"), max[1]));
        e.setScore3(score(p.get("score3"), max[2]));
        e.setScore4(score(p.get("score4"), max[3]));
        e.setScore5(score(p.get("score5"), max[4]));
        try { e.setEvalDate(blank(p.get("evalDate")) != null ? LocalDate.parse(p.get("evalDate")) : null); } catch (Exception ignored) { }
        try { e.setEvalTime(blank(p.get("evalTime")) != null ? LocalTime.parse(p.get("evalTime")) : null); } catch (Exception ignored) { }
        if (e.getEvalDate() == null) e.setEvalDate(LocalDate.now());   // left empty: today / now
        if (e.getEvalTime() == null) e.setEvalTime(LocalTime.now().withSecond(0).withNano(0));
        e.setNotes(blank(p.get("notes")));
        e.setEpaLevel(blank(p.get("epaLevel")));
        return repo.save(e);
    }

    /** What is still missing before it can be sent (empty = ready). */
    public List<String> missingForSend(EndoEvaluation e) {
        List<String> out = new ArrayList<>();
        String[] s = e.getScores();
        for (int i = 0; i < s.length; i++) if (s[i] == null || s[i].isBlank()) out.add("score " + (i + 1));
        if (e.getEpaLevel() == null) out.add("EPA level");
        return out;
    }

    public EndoEvaluation send(EndoEvaluation e) {
        e.setSentAt(LocalDateTime.now());
        e.setViewedAt(null);
        return repo.save(e);
    }

    public EndoEvaluation recall(EndoEvaluation e) {
        e.setSentAt(null);
        return repo.save(e);
    }

    public void delete(EndoEvaluation e) { repo.delete(e); }

    public void markViewed(EndoEvaluation e) {
        if (e.isSent() && e.getViewedAt() == null) { e.setViewedAt(LocalDateTime.now()); repo.save(e); }
    }

    public List<EndoEvaluation> sentForClinician(User clinician) {
        return clinician == null ? List.of() : repo.findByEndoChart_ClinicianAndSentAtIsNotNullOrderBySentAtDesc(clinician);
    }

    public int unreadCount(User clinician) {
        return clinician == null ? 0 : (int) repo.countByEndoChart_ClinicianAndSentAtIsNotNullAndViewedAtIsNull(clinician);
    }

    /** One time: ENDO2 used to be stored on the Endo chart itself. Moves any filled-in one to its own record. */
    @EventListener(ApplicationReadyEvent.class)
    public void moveOldEvaluations() {
        for (EndodonticsChart c : chartRepo.findAll()) {
            if (c.getForm2Status() == null || c.getForm2Status() == EndodonticsChartStatus.CREATED) continue;
            boolean filled = java.util.stream.Stream.of(c.getEvalScore1(), c.getEvalScore2(), c.getEvalScore3(), c.getEvalScore4(),
                    c.getEvalScore5(), c.getEvalNotes(), c.getEpaLevel()).anyMatch(x -> x != null && !x.isBlank());
            if (filled && !repo.existsByEndoChart(c)) {
                EndoEvaluation e = new EndoEvaluation();
                e.setEndoChart(c);
                e.setVisitNo(1);
                e.setToothNumber(c.getEvalToothNumber());
                e.setDiagnosis(c.getEvalDiagnosis());
                e.setScore1(c.getEvalScore1()); e.setScore2(c.getEvalScore2()); e.setScore3(c.getEvalScore3());
                e.setScore4(c.getEvalScore4()); e.setScore5(c.getEvalScore5());
                e.setEvalDate(c.getEvalDate()); e.setEvalTime(c.getEvalTime());
                e.setNotes(c.getEvalNotes());
                e.setEpaLevel(c.getEpaLevel());
                if (c.getForm2Status() == EndodonticsChartStatus.APPROVED) { e.setSentAt(LocalDateTime.now()); e.setViewedAt(LocalDateTime.now()); }
                repo.save(e);
            }
            c.setForm2Status(EndodonticsChartStatus.CREATED);   // marks it as moved
            chartRepo.save(c);
        }
    }

    private static String blank(String s) { return s == null || s.isBlank() ? null : s.trim(); }

    /** Keeps a score between 0 and the item's points; anything else is dropped. */
    private static String score(String v, String max) {
        if (v == null || v.isBlank()) return null;
        try {
            double d = Math.max(0, Math.min(Double.parseDouble(v.trim()), Double.parseDouble(max)));
            return d == Math.floor(d) ? String.valueOf((int) d) : String.valueOf(d);
        } catch (NumberFormatException e) { return null; }
    }
}
