package com.dentis.DENTIS.repository;

import com.dentis.DENTIS.model.OperativeChart;
import com.dentis.DENTIS.model.OperativeChartStatus;
import com.dentis.DENTIS.model.Patient;
import com.dentis.DENTIS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OperativeChartRepository extends JpaRepository<OperativeChart, Long> {

    List<OperativeChart> findByPatientOrderByCreatedAtDesc(Patient patient);

    List<OperativeChart> findByClinicianOrderByCreatedAtDesc(User clinician);

    List<OperativeChart> findByFacultyOrderByCreatedAtDesc(User faculty);

    // Form 10 (status) queries
    List<OperativeChart> findByClinicianAndStatusInOrderByCreatedAtDesc(
            User clinician, List<OperativeChartStatus> statuses);

    List<OperativeChart> findByFacultyAndStatusOrderByCreatedAtDesc(
            User faculty, OperativeChartStatus status);

    // Form 6 (form2Status) queries
    List<OperativeChart> findByClinicianAndForm2StatusInOrderByCreatedAtDesc(
            User clinician, List<OperativeChartStatus> statuses);

    List<OperativeChart> findByFacultyAndForm2StatusOrderByCreatedAtDesc(
            User faculty, OperativeChartStatus form2Status);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
