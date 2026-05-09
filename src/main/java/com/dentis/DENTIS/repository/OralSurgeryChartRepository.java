package com.dentis.DENTIS.repository;

import com.dentis.DENTIS.model.OralSurgeryChart;
import com.dentis.DENTIS.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OralSurgeryChartRepository extends JpaRepository<OralSurgeryChart, Long> {
    Optional<OralSurgeryChart> findByPatient(Patient patient);
    List<OralSurgeryChart> findByPatientOrderByCreatedAtDesc(Patient patient);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
