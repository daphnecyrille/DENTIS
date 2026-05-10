package com.dentis.DENTIS.service;

import com.dentis.DENTIS.model.AccountStatus;
import com.dentis.DENTIS.model.OralSurgeryChartType;
import com.dentis.DENTIS.model.Patient;
import com.dentis.DENTIS.model.Role;
import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.repository.PatientRepository;
import com.dentis.DENTIS.repository.UserRepository;
import com.dentis.DENTIS.service.OralSurgeryChartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final OralSurgeryChartService oralSurgeryChartService;

    public PatientService(PatientRepository patientRepository,
                          UserRepository userRepository,
                          OralSurgeryChartService oralSurgeryChartService) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.oralSurgeryChartService = oralSurgeryChartService;
    }

    public List<User> getFacultyBySection(String section) {
        return userRepository.findByRoleAndSectionAndStatusOrderByLastNameAsc(Role.FACULTY, section, AccountStatus.ACTIVE);
    }

    public void assignFaculty(Long patientId, Long facultyId) {
        Patient patient = patientRepository.findById(patientId).orElseThrow();
        User faculty = userRepository.findById(facultyId).orElseThrow();
        patient.setAssignedFaculty(faculty);
        patientRepository.save(patient);
    }

    public List<Patient> getPatientsForFaculty(User faculty) {
        return patientRepository.findByAssignedFacultyOrderByCreatedAtDesc(faculty);
    }

    public List<Patient> getUnassignedPatientsForFaculty(User faculty) {
        return patientRepository.findByAssignedFacultyAndAssignedClinicianIsNullOrderByCreatedAtDesc(faculty);
    }

    public void assignClinician(Long patientId, Long clinicianId, User faculty) {
        Patient patient = patientRepository.findById(patientId).orElseThrow();
        User clinician = userRepository.findById(clinicianId).orElseThrow();
        patient.setAssignedClinician(clinician);
        patientRepository.save(patient);
        // auto-create OSF1 and OSF2 if not already assigned
        if (oralSurgeryChartService.findByPatientAndType(patient, OralSurgeryChartType.OSF1).isEmpty()) {
            oralSurgeryChartService.createForPatient(patient, clinician, faculty, OralSurgeryChartType.OSF1);
        }
        if (oralSurgeryChartService.findByPatientAndType(patient, OralSurgeryChartType.OSF2).isEmpty()) {
            oralSurgeryChartService.createForPatient(patient, clinician, faculty, OralSurgeryChartType.OSF2);
        }
    }

    public List<Patient> getAssignedPatientsForFaculty(User faculty) {
        return patientRepository.findByAssignedFacultyAndAssignedClinicianIsNotNullOrderByCreatedAtDesc(faculty);
    }

    public List<Patient> getPatientsForClinician(User clinician) {
        return patientRepository.findByAssignedClinicianOrderByCreatedAtDesc(clinician);
    }

    public List<User> getAllClinicians() {
        return userRepository.findByRoleAndStatusOrderByLastNameAsc(Role.CLINICIAN, AccountStatus.ACTIVE);
    }

    public Patient save(Patient patient) {
        LocalDate today = LocalDate.now();
        patient.setRegistrationDate(today);

        long count = patientRepository.countByRegistrationDate(today);
        String dateStr = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        patient.setChartNo(String.format("%s-%05d", dateStr, count + 1));

        return patientRepository.save(patient);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAllByOrderByCreatedAtDesc();
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findById(id).orElseThrow();
    }

    public Patient update(Long id, Patient updated) {
        Patient existing = patientRepository.findById(id).orElseThrow();
        existing.setAge(updated.getAge());
        existing.setBirthdate(updated.getBirthdate());
        existing.setSex(updated.getSex());
        existing.setAddress(updated.getAddress());
        existing.setContactNumber(updated.getContactNumber());
        existing.setEmail(updated.getEmail());
        existing.setCivilStatus(updated.getCivilStatus());
        existing.setOccupation(updated.getOccupation());
        existing.setEducationalAttainment(updated.getEducationalAttainment());
        existing.setEmergencyContactName(updated.getEmergencyContactName());
        existing.setEmergencyContactRelation(updated.getEmergencyContactRelation());
        existing.setEmergencyContactNumber(updated.getEmergencyContactNumber());
        existing.setChiefComplaint(updated.getChiefComplaint());
        existing.setServiceCode(updated.getServiceCode());
        existing.setProposedTreatmentPlan(updated.getProposedTreatmentPlan());
        return patientRepository.save(existing);
    }
}
