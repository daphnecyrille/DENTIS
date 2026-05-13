package com.dentis.DENTIS.repository;

import com.dentis.DENTIS.model.ClinicianFacultyAssignment;
import com.dentis.DENTIS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClinicianFacultyAssignmentRepository extends JpaRepository<ClinicianFacultyAssignment, Long> {
    Optional<ClinicianFacultyAssignment> findByClinicianAndSection(User clinician, String section);
    List<ClinicianFacultyAssignment> findByClinician(User clinician);
    List<ClinicianFacultyAssignment> findByFacultyAndSectionIgnoreCase(User faculty, String section);
}
