package com.dentis.DENTIS.repository;

import com.dentis.DENTIS.model.PerioEvaluation;
import com.dentis.DENTIS.model.PerioEvalFormType;
import com.dentis.DENTIS.model.PerioEvalStatus;
import com.dentis.DENTIS.model.PeriodonticChart;
import com.dentis.DENTIS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PerioEvaluationRepository extends JpaRepository<PerioEvaluation, Long> {
    List<PerioEvaluation> findByPerioChartOrderByFormTypeAsc(PeriodonticChart chart);
    Optional<PerioEvaluation> findByPerioChartAndFormType(PeriodonticChart chart, PerioEvalFormType formType);
    List<PerioEvaluation> findByPerioChartAndStatus(PeriodonticChart chart, PerioEvalStatus status);
    boolean existsByPerioChart(PeriodonticChart chart);
    List<PerioEvaluation> findByPerioChartOrderByCreatedAtAsc(PeriodonticChart chart);
    /** All evaluations of a clinician with the given status, newest sent first. */
    List<PerioEvaluation> findByPerioChart_ClinicianAndStatusOrderBySentAtDesc(User clinician, PerioEvalStatus status);
    /** Sent evaluations the clinician hasn't opened yet. */
    List<PerioEvaluation> findByPerioChart_ClinicianAndStatusAndViewedAtIsNullOrderBySentAtDesc(User clinician, PerioEvalStatus status);
}
