package com.dentis.DENTIS.repository;

import com.dentis.DENTIS.model.Patient;
import com.dentis.DENTIS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    long countByRegistrationDate(LocalDate date);
    long countByRegistrationDateBetween(LocalDate start, LocalDate end);
    long countByServiceCodeIgnoreCase(String serviceCode);
    // Count only root (non-recall) patients
    long countByOriginalPatientIsNull();
    long countByServiceCodeIgnoreCaseAndOriginalPatientIsNull(String serviceCode);
    // Unique patient count per clinician (root patients only)
    @Query("SELECT p.assignedClinician.id, COUNT(p) FROM Patient p WHERE p.originalPatient IS NULL AND p.assignedClinician IS NOT NULL GROUP BY p.assignedClinician.id")
    List<Object[]> countUniqueRootPatientsByClinician();
    List<Patient> findAllByOrderByCreatedAtDesc();
    Optional<Patient> findByChartNo(String chartNo);
    List<Patient> findByAssignedFacultyOrderByCreatedAtDesc(User faculty);
    List<Patient> findByAssignedFacultyAndAssignedClinicianIsNullOrderByCreatedAtDesc(User faculty);
    List<Patient> findByAssignedFacultyAndAssignedClinicianIsNotNullOrderByCreatedAtDesc(User faculty);
    List<Patient> findByAssignedClinicianOrderByCreatedAtDesc(User clinician);
    long countByOriginalPatient(Patient originalPatient);
    List<Patient> findByOriginalPatient(Patient originalPatient);
}
