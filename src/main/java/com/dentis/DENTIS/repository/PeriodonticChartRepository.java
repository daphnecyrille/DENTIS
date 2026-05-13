package com.dentis.DENTIS.repository;

import com.dentis.DENTIS.model.Patient;
import com.dentis.DENTIS.model.PeriodonticChart;
import com.dentis.DENTIS.model.PeriodonticChartStatus;
import com.dentis.DENTIS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PeriodonticChartRepository extends JpaRepository<PeriodonticChart, Long> {

    List<PeriodonticChart> findByPatientOrderByCreatedAtDesc(Patient patient);

    List<PeriodonticChart> findByClinicianOrderByCreatedAtDesc(User clinician);

    List<PeriodonticChart> findByFacultyOrderByCreatedAtDesc(User faculty);

    // ── Form A/B (PF1) status queries ────────────────────────────────────────
    List<PeriodonticChart> findByClinicianAndStatusInOrderByCreatedAtDesc(
            User clinician, List<PeriodonticChartStatus> statuses);

    List<PeriodonticChart> findByFacultyAndStatusOrderByCreatedAtDesc(
            User faculty, PeriodonticChartStatus status);

    // ── Form C (PF2) status queries ───────────────────────────────────────────
    List<PeriodonticChart> findByClinicianAndFormCStatusInOrderByCreatedAtDesc(
            User clinician, List<PeriodonticChartStatus> statuses);

    List<PeriodonticChart> findByFacultyAndFormCStatusOrderByCreatedAtDesc(
            User faculty, PeriodonticChartStatus formCStatus);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
