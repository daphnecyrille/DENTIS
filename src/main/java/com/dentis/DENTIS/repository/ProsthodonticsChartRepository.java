package com.dentis.DENTIS.repository;

import com.dentis.DENTIS.model.Patient;
import com.dentis.DENTIS.model.ProsthodonticsChart;
import com.dentis.DENTIS.model.ProsthodonticsChartStatus;
import com.dentis.DENTIS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProsthodonticsChartRepository extends JpaRepository<ProsthodonticsChart, Long> {
    List<ProsthodonticsChart> findByPatientOrderByCreatedAtDesc(Patient patient);
    Optional<ProsthodonticsChart> findTopByPatientOrderByCreatedAtDesc(Patient patient);
    List<ProsthodonticsChart> findByClinicianOrderByCreatedAtDesc(User clinician);
    List<ProsthodonticsChart> findByFacultyOrderByCreatedAtDesc(User faculty);
    List<ProsthodonticsChart> findByClinicianAndStatusInOrderByCreatedAtDesc(User clinician, List<ProsthodonticsChartStatus> statuses);
    List<ProsthodonticsChart> findByFacultyAndStatusOrderByCreatedAtDesc(User faculty, ProsthodonticsChartStatus status);
}
