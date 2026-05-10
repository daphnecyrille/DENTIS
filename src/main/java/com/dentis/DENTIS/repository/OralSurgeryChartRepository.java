package com.dentis.DENTIS.repository;

import com.dentis.DENTIS.model.OralSurgeryChart;
import com.dentis.DENTIS.model.OralSurgeryChartType;
import com.dentis.DENTIS.model.Patient;
import com.dentis.DENTIS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OralSurgeryChartRepository extends JpaRepository<OralSurgeryChart, Long> {
    List<OralSurgeryChart> findByPatientOrderByCreatedAtDesc(Patient patient);
    Optional<OralSurgeryChart> findByPatientAndChartType(Patient patient, OralSurgeryChartType chartType);
    List<OralSurgeryChart> findByClinicianOrderByCreatedAtDesc(User clinician);
    List<OralSurgeryChart> findByFacultyOrderByCreatedAtDesc(User faculty);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
