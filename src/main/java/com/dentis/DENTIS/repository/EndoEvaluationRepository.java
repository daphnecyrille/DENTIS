package com.dentis.DENTIS.repository;

import com.dentis.DENTIS.model.EndoEvaluation;
import com.dentis.DENTIS.model.EndodonticsChart;
import com.dentis.DENTIS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EndoEvaluationRepository extends JpaRepository<EndoEvaluation, Long> {
    List<EndoEvaluation> findByEndoChartOrderByCreatedAtAsc(EndodonticsChart chart);
    List<EndoEvaluation> findByEndoChart_ClinicianAndSentAtIsNotNullOrderBySentAtDesc(User clinician);
    long countByEndoChart_ClinicianAndSentAtIsNotNullAndViewedAtIsNull(User clinician);
    boolean existsByEndoChart(EndodonticsChart chart);
}
