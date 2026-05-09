package com.dentis.DENTIS.service;

import com.dentis.DENTIS.model.Patient;
import com.dentis.DENTIS.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
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
}
