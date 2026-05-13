package com.dentis.DENTIS.repository;

import com.dentis.DENTIS.model.Patient;
import com.dentis.DENTIS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    long countByRegistrationDate(LocalDate date);
    long countByServiceCodeIgnoreCase(String serviceCode);
    List<Patient> findAllByOrderByCreatedAtDesc();
    Optional<Patient> findByChartNo(String chartNo);
    List<Patient> findByAssignedFacultyOrderByCreatedAtDesc(User faculty);
    List<Patient> findByAssignedFacultyAndAssignedClinicianIsNullOrderByCreatedAtDesc(User faculty);
    List<Patient> findByAssignedFacultyAndAssignedClinicianIsNotNullOrderByCreatedAtDesc(User faculty);
    List<Patient> findByAssignedClinicianOrderByCreatedAtDesc(User clinician);
}
