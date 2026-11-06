package com.dentis.DENTIS.repository;

import com.dentis.DENTIS.model.EndodonticsChart;
import com.dentis.DENTIS.model.EndodonticsChartStatus;
import com.dentis.DENTIS.model.Patient;
import com.dentis.DENTIS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EndodonticsChartRepository extends JpaRepository<EndodonticsChart, Long> {
    List<EndodonticsChart> findByPatientOrderByCreatedAtDesc(Patient patient);
    Optional<EndodonticsChart> findByPatient(Patient patient);
    List<EndodonticsChart> findByClinicianOrderByCreatedAtDesc(User clinician);
    List<EndodonticsChart> findByFacultyOrderByCreatedAtDesc(User faculty);
    List<EndodonticsChart> findByClinicianAndStatusInOrderByCreatedAtDesc(User clinician, List<EndodonticsChartStatus> statuses);
    List<EndodonticsChart> findByFacultyAndStatusOrderByCreatedAtDesc(User faculty, EndodonticsChartStatus status);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<EndodonticsChart> findAllByOrderByCreatedAtDesc();
}
