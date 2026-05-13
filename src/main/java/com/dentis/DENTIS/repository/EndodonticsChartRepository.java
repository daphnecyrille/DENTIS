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

    @Query("SELECT e FROM EndodonticsChart e WHERE e.clinician = :clinician AND e.form2Status IN :statuses ORDER BY e.createdAt DESC")
    List<EndodonticsChart> findByClinicianAndForm2StatusIn(@Param("clinician") User clinician, @Param("statuses") List<EndodonticsChartStatus> statuses);

    @Query("SELECT e FROM EndodonticsChart e WHERE e.faculty = :faculty AND e.form2Status = :status ORDER BY e.createdAt DESC")
    List<EndodonticsChart> findByFacultyAndForm2Status(@Param("faculty") User faculty, @Param("status") EndodonticsChartStatus status);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
