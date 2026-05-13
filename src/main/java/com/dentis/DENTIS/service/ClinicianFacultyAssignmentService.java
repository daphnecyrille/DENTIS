package com.dentis.DENTIS.service;

import com.dentis.DENTIS.model.AccountStatus;
import com.dentis.DENTIS.model.ClinicianAssignmentRow;
import com.dentis.DENTIS.model.ClinicianFacultyAssignment;
import com.dentis.DENTIS.model.Role;
import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.repository.ClinicianFacultyAssignmentRepository;
import com.dentis.DENTIS.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClinicianFacultyAssignmentService {

    private final ClinicianFacultyAssignmentRepository assignmentRepo;
    private final UserRepository userRepository;

    public ClinicianFacultyAssignmentService(ClinicianFacultyAssignmentRepository assignmentRepo,
                                              UserRepository userRepository) {
        this.assignmentRepo = assignmentRepo;
        this.userRepository = userRepository;
    }

    public List<User> getAdviseesForFaculty(User faculty, String section) {
        return assignmentRepo.findByFacultyAndSectionIgnoreCase(faculty, section)
                .stream()
                .map(ClinicianFacultyAssignment::getClinician)
                .toList();
    }

    public ClinicianAssignmentRow getAssignmentRowForClinician(User clinician) {
        return new ClinicianAssignmentRow(
                clinician,
                getFacultyForSection(clinician, "OS"),
                getFacultyForSection(clinician, "ENDO"),
                getFacultyForSection(clinician, "PERIO"),
                getFacultyForSection(clinician, "RESTO")
        );
    }

    public List<ClinicianAssignmentRow> getAssignmentRows() {
        List<User> clinicians = userRepository.findByRoleAndStatusOrderByLastNameAsc(Role.CLINICIAN, AccountStatus.ACTIVE);
        return clinicians.stream().map(clinician -> new ClinicianAssignmentRow(
                clinician,
                getFacultyForSection(clinician, "OS"),
                getFacultyForSection(clinician, "ENDO"),
                getFacultyForSection(clinician, "PERIO"),
                getFacultyForSection(clinician, "RESTO")
        )).toList();
    }

    private User getFacultyForSection(User clinician, String section) {
        return assignmentRepo.findByClinicianAndSection(clinician, section)
                .map(ClinicianFacultyAssignment::getFaculty)
                .orElse(null);
    }

    public void saveAssignment(Long clinicianId, Long osFacultyId, Long endoFacultyId,
                                Long perioFacultyId, Long restoFacultyId) {
        User clinician = userRepository.findById(clinicianId).orElseThrow();
        upsert(clinician, "OS", osFacultyId);
        upsert(clinician, "ENDO", endoFacultyId);
        upsert(clinician, "PERIO", perioFacultyId);
        upsert(clinician, "RESTO", restoFacultyId);
    }

    private void upsert(User clinician, String section, Long facultyId) {
        Optional<ClinicianFacultyAssignment> existing = assignmentRepo.findByClinicianAndSection(clinician, section);
        if (facultyId == null) {
            existing.ifPresent(assignmentRepo::delete);
            return;
        }
        User faculty = userRepository.findById(facultyId).orElse(null);
        if (faculty == null) {
            existing.ifPresent(assignmentRepo::delete);
            return;
        }
        ClinicianFacultyAssignment assignment = existing.orElseGet(ClinicianFacultyAssignment::new);
        assignment.setClinician(clinician);
        assignment.setSection(section);
        assignment.setFaculty(faculty);
        assignmentRepo.save(assignment);
    }
}
