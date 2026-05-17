package com.dentis.DENTIS.service;

import com.dentis.DENTIS.model.*;
import com.dentis.DENTIS.repository.PerioEvaluationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PerioEvaluationService {

    private final PerioEvaluationRepository repo;

    public PerioEvaluationService(PerioEvaluationRepository repo) {
        this.repo = repo;
    }

    /** Auto-generates all 8 evaluation form records (DRAFT) for a chart if not already present. */
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

    /** Returns only GRADED evaluations (for clinician read-only view). */
    public List<PerioEvaluation> getGradedByChart(PeriodonticChart chart) {
        return repo.findByPerioChartAndStatus(chart, PerioEvalStatus.GRADED);
    }

    public PerioEvaluation getById(Long id) {
        return repo.findById(id).orElseThrow();
    }

    public PerioEvaluation save(PerioEvaluation eval) {
        return repo.save(eval);
    }

    public PerioEvaluation grade(PerioEvaluation eval) {
        eval.setStatus(PerioEvalStatus.GRADED);
        return repo.save(eval);
    }

    public PerioEvaluation resetToDraft(PerioEvaluation eval) {
        eval.setStatus(PerioEvalStatus.DRAFT);
        return repo.save(eval);
    }
}
