package com.dentis.DENTIS.service;

import com.dentis.DENTIS.model.AccountStatus;
import com.dentis.DENTIS.model.OralSurgeryChartType;
import com.dentis.DENTIS.model.Patient;
import com.dentis.DENTIS.model.Role;
import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.repository.PatientRepository;
import com.dentis.DENTIS.repository.UserRepository;
import com.dentis.DENTIS.service.OralSurgeryChartService;
import com.dentis.DENTIS.service.EndodonticsChartService;
import com.dentis.DENTIS.service.PeriodonticChartService;
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
    private final EndodonticsChartService endodonticsChartService;
    private final OperativeChartService operativeChartService;
    private final PeriodonticChartService periodonticChartService;

    public PatientService(PatientRepository patientRepository,
                          UserRepository userRepository,
                          OralSurgeryChartService oralSurgeryChartService,
                          EndodonticsChartService endodonticsChartService,
                          OperativeChartService operativeChartService,
                          PeriodonticChartService periodonticChartService) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.oralSurgeryChartService = oralSurgeryChartService;
        this.endodonticsChartService = endodonticsChartService;
        this.operativeChartService = operativeChartService;
        this.periodonticChartService = periodonticChartService;
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

        String code = patient.getServiceCode() != null ? patient.getServiceCode().toUpperCase() : "";

        if ("OS".equals(code)) {
            if (oralSurgeryChartService.findByPatientAndType(patient, OralSurgeryChartType.OSF1).isEmpty()) {
                oralSurgeryChartService.createForPatient(patient, clinician, faculty, OralSurgeryChartType.OSF1);
            }
            if (oralSurgeryChartService.findByPatientAndType(patient, OralSurgeryChartType.OSF2).isEmpty()) {
                oralSurgeryChartService.createForPatient(patient, clinician, faculty, OralSurgeryChartType.OSF2);
            }
        } else if ("ENDO".equals(code)) {
            if (endodonticsChartService.findByPatient(patient).isEmpty()) {
                endodonticsChartService.createForPatient(patient, clinician, faculty);
            }
        } else if ("PERIO".equals(code)) {
            if (periodonticChartService.findAllByPatient(patient).isEmpty()) {
                periodonticChartService.createForPatient(patient, clinician, faculty);
            }
        } else if ("RESTO".equalsIgnoreCase(code)) {
            if (operativeChartService.findAllByPatient(patient).isEmpty()) {
                operativeChartService.createForPatient(patient, clinician, faculty);
            }
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

    public long countAll() {
        return patientRepository.count();
    }

    public long countBySection(String section) {
        return patientRepository.countByServiceCodeIgnoreCase(section);
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findById(id).orElseThrow();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
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
