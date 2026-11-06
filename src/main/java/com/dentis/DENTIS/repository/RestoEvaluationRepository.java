package com.dentis.DENTIS.repository;

import com.dentis.DENTIS.model.OperativeChart;
import com.dentis.DENTIS.model.RestoEvaluation;
import com.dentis.DENTIS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestoEvaluationRepository extends JpaRepository<RestoEvaluation, Long> {
    List<RestoEvaluation> findByOperativeChartOrderByCreatedAtAsc(OperativeChart chart);
    List<RestoEvaluation> findByOperativeChart_ClinicianAndSentAtIsNotNullOrderBySentAtDesc(User clinician);
    long countByOperativeChart_ClinicianAndSentAtIsNotNullAndViewedAtIsNull(User clinician);
    boolean existsByOperativeChart(OperativeChart chart);
}
