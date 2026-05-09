package com.dentis.DENTIS.repository;

import com.dentis.DENTIS.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    long countByRegistrationDate(LocalDate date);
    List<Patient> findAllByOrderByCreatedAtDesc();
}
