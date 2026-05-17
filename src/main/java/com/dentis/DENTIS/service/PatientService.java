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
import com.dentis.DENTIS.service.ProsthodonticsChartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final OralSurgeryChartService oralSurgeryChartService;
    private final EndodonticsChartService endodonticsChartService;
    private final OperativeChartService operativeChartService;
    private final PeriodonticChartService periodonticChartService;
    private final ProsthodonticsChartService prosthodonticsChartService;

    public PatientService(PatientRepository patientRepository,
                          UserRepository userRepository,
                          OralSurgeryChartService oralSurgeryChartService,
                          EndodonticsChartService endodonticsChartService,
                          OperativeChartService operativeChartService,
                          PeriodonticChartService periodonticChartService,
                          ProsthodonticsChartService prosthodonticsChartService) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.oralSurgeryChartService = oralSurgeryChartService;
        this.endodonticsChartService = endodonticsChartService;
        this.operativeChartService = operativeChartService;
        this.periodonticChartService = periodonticChartService;
        this.prosthodonticsChartService = prosthodonticsChartService;
    }

    private String getSectionForServiceCode(String code) {
        if (code == null) return "";
        if ("CD".equalsIgnoreCase(code) || "RPD".equalsIgnoreCase(code)) return "PROSTHO";
        return code;
    }

    public List<User> getFacultyBySection(String section) {
        String resolvedSection = getSectionForServiceCode(section);
        return userRepository.findByRoleAndSectionAndStatusOrderByLastNameAsc(Role.FACULTY, resolvedSection, AccountStatus.ACTIVE);
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
        } else if ("CD".equalsIgnoreCase(code) || "RPD".equalsIgnoreCase(code)) {
            if (prosthodonticsChartService.findAllByPatient(patient).isEmpty()) {
                prosthodonticsChartService.createForPatient(patient, clinician, faculty);
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

        LocalDate startOfYear = today.withDayOfYear(1);
        LocalDate endOfYear = today.withDayOfYear(today.lengthOfYear());
        String year = today.format(DateTimeFormatter.ofPattern("yyyy"));
        long count = patientRepository.countByRegistrationDateBetween(startOfYear, endOfYear);
        String chartNo;
        long seq = count + 1;
        do {
            chartNo = String.format("%s-%05d", year, seq++);
        } while (patientRepository.findByChartNo(chartNo).isPresent());
        patient.setChartNo(chartNo);

        return patientRepository.save(patient);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAllByOrderByCreatedAtDesc();
    }

    public long countAll() {
        return patientRepository.countByOriginalPatientIsNull();
    }

    public long countBySection(String section) {
        return patientRepository.countByServiceCodeIgnoreCaseAndOriginalPatientIsNull(section);
    }

    public Map<Long, Long> getUniquePatientCountByClinician() {
        Map<Long, Long> map = new java.util.LinkedHashMap<>();
        for (Object[] row : patientRepository.countUniqueRootPatientsByClinician()) {
            map.put((Long) row[0], (Long) row[1]);
        }
        return map;
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findById(id).orElseThrow();
    }

    public List<Patient> getRecallsByOriginal(Patient original) {
        return patientRepository.findByOriginalPatient(original);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public Patient readmit(Long patientId, Patient form) {
        Patient patient = patientRepository.findById(patientId).orElseThrow();
        // Always trace back to the root original patient so recall numbers are sequential
        Patient original = patient.getOriginalPatient() != null ? patient.getOriginalPatient() : patient;
        long recallCount = patientRepository.countByOriginalPatient(original);
        String newChartNo = original.getChartNo() + "-R" + (recallCount + 1);

        Patient recall = new Patient();
        recall.setChartNo(newChartNo);
        recall.setRegistrationDate(form.getRegistrationDate() != null ? form.getRegistrationDate() : LocalDate.now());
        recall.setOriginalPatient(original);

        // Copy updated personal info from the form
        recall.setSurname(form.getSurname());
        recall.setFirstName(form.getFirstName());
        recall.setMiddleInitial(form.getMiddleInitial());
        recall.setAge(form.getAge());
        recall.setBirthdate(form.getBirthdate());
        recall.setSex(form.getSex());
        recall.setAddress(form.getAddress());
        recall.setContactNumber(form.getContactNumber());
        recall.setEmail(form.getEmail());
        recall.setCivilStatus(form.getCivilStatus());
        recall.setOccupation(form.getOccupation());
        recall.setEducationalAttainment(form.getEducationalAttainment());
        recall.setEmergencyContactName(form.getEmergencyContactName());
        recall.setEmergencyContactRelation(form.getEmergencyContactRelation());
        recall.setEmergencyContactNumber(form.getEmergencyContactNumber());
        recall.setChiefComplaint(form.getChiefComplaint());
        recall.setHistoryOfPresentIllness(form.getHistoryOfPresentIllness());
        recall.setServiceCode(form.getServiceCode());
        recall.setProposedTreatmentPlan(form.getProposedTreatmentPlan());

        return patientRepository.save(recall);
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
