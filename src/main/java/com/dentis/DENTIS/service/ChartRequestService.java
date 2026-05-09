package com.dentis.DENTIS.service;

import com.dentis.DENTIS.model.*;
import com.dentis.DENTIS.repository.ChartRequestRepository;
import com.dentis.DENTIS.repository.PatientRepository;
import com.dentis.DENTIS.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ChartRequestService {

    private final ChartRequestRepository chartRequestRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    public ChartRequestService(ChartRequestRepository chartRequestRepository,
                               PatientRepository patientRepository,
                               UserRepository userRepository) {
        this.chartRequestRepository = chartRequestRepository;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
    }

    public List<User> getClinicians() {
        return userRepository.findByRoleAndStatusOrderByLastNameAsc(Role.CLINICIAN, AccountStatus.ACTIVE);
    }

    public ChartRequest submit(ChartRequest request, Long clinicianId) {
        if (clinicianId != null) {
            userRepository.findById(clinicianId).ifPresent(request::setClinician);
        }
        patientRepository.findByChartNo(request.getChartNo()).ifPresent(p -> {
            request.setPatient(p);
            String name = p.getSurname() + ", " + p.getFirstName()
                    + (p.getMiddleInitial() != null && !p.getMiddleInitial().isBlank()
                        ? " " + p.getMiddleInitial() + "." : "");
            request.setPatientName(name);
        });
        request.setStatus(ChartRequestStatus.PENDING);
        return chartRequestRepository.save(request);
    }

    public List<ChartRequest> getAllRequests() {
        return chartRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    public void approve(Long id) {
        ChartRequest r = chartRequestRepository.findById(id).orElseThrow();
        r.setStatus(ChartRequestStatus.APPROVED);
        chartRequestRepository.save(r);
    }

    public void deny(Long id) {
        ChartRequest r = chartRequestRepository.findById(id).orElseThrow();
        r.setStatus(ChartRequestStatus.DENIED);
        chartRequestRepository.save(r);
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findById(id).orElseThrow();
    }

    public List<Patient> getApprovedPatients() {
        return chartRequestRepository.findByStatusOrderByCreatedAtDesc(ChartRequestStatus.APPROVED)
                .stream()
                .map(ChartRequest::getPatient)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<ChartRequest> getPendingRequests() {
        return chartRequestRepository.findByStatusOrderByCreatedAtDesc(ChartRequestStatus.PENDING);
    }

    public List<ChartRequest> getClinicianRequests(User clinician) {
        return chartRequestRepository.findByClinicianAndStatusInOrderByCreatedAtDesc(
                clinician, List.of(ChartRequestStatus.PENDING, ChartRequestStatus.DENIED));
    }

    public List<ChartRequest> getApprovedRequests() {
        return chartRequestRepository.findByStatusOrderByCreatedAtDesc(ChartRequestStatus.APPROVED);
    }

    public List<ChartRequest> getApprovedRequestsForClinicianAndPatient(User clinician, Long patientId) {
        return chartRequestRepository.findApprovedForClinicianAndPatient(clinician, patientId, ChartRequestStatus.APPROVED);
    }
}
