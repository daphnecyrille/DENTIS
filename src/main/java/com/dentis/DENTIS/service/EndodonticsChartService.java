package com.dentis.DENTIS.service;

import com.dentis.DENTIS.model.*;
import com.dentis.DENTIS.repository.EndodonticsChartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EndodonticsChartService {

    private final EndodonticsChartRepository repo;

    public EndodonticsChartService(EndodonticsChartRepository repo) {
        this.repo = repo;
    }

    public EndodonticsChart createForPatient(Patient patient, User clinician, User faculty) {
        EndodonticsChart chart = new EndodonticsChart();
        chart.setPatient(patient);
        chart.setClinician(clinician);
        chart.setFaculty(faculty);
        chart.setChartNo(generateChartNo());
        chart.setChiefComplaint(patient.getChiefComplaint());
        return repo.save(chart);
    }

    private String generateChartNo() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        long count = repo.countByCreatedAtBetween(start, end);
        String dateStr = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("ENDO1-%s-%05d", dateStr, count + 1);
    }

    public EndodonticsChart getById(Long id) {
        return repo.findById(id).orElseThrow();
    }

    public Optional<EndodonticsChart> findByPatient(Patient patient) {
        return repo.findByPatient(patient);
    }

    public List<EndodonticsChart> findAllByPatient(Patient patient) {
        return repo.findByPatientOrderByCreatedAtDesc(patient);
    }

    public List<EndodonticsChart> findByClinicianOrderByCreatedAtDesc(User clinician) {
        return repo.findByClinicianOrderByCreatedAtDesc(clinician);
    }

    public List<EndodonticsChart> findByFacultyOrderByCreatedAtDesc(User faculty) {
        return repo.findByFacultyOrderByCreatedAtDesc(faculty);
    }

    public List<EndodonticsChart> findActionNeededByClinician(User clinician) {
        return repo.findByClinicianAndStatusInOrderByCreatedAtDesc(clinician,
                List.of(EndodonticsChartStatus.CREATED, EndodonticsChartStatus.IN_PROGRESS, EndodonticsChartStatus.REVISE));
    }

    public List<EndodonticsChart> findForm2ActionNeededByClinician(User clinician) {
        return repo.findByClinicianAndForm2StatusIn(clinician,
                List.of(EndodonticsChartStatus.CREATED, EndodonticsChartStatus.IN_PROGRESS, EndodonticsChartStatus.REVISE));
    }

    public List<EndodonticsChart> findAwaitingApprovalByFaculty(User faculty) {
        return repo.findByFacultyAndStatusOrderByCreatedAtDesc(faculty, EndodonticsChartStatus.SUBMITTED);
    }

    public List<EndodonticsChart> findAwaitingApproval2ByFaculty(User faculty) {
        return repo.findByFacultyAndForm2Status(faculty, EndodonticsChartStatus.SUBMITTED);
    }

    public EndodonticsChart saveForm1(Long id, EndodonticsChart updated) {
        EndodonticsChart chart = repo.findById(id).orElseThrow();
        chart.setVisitDate(updated.getVisitDate());
        chart.setToothNumber(updated.getToothNumber());
        chart.setReferredBy(updated.getReferredBy());
        chart.setClinicianContactNumber(updated.getClinicianContactNumber());
        chart.setChiefComplaint(updated.getChiefComplaint());
        chart.setHistoryOfPresentIllness(updated.getHistoryOfPresentIllness());
        chart.setMedicalHistory(updated.getMedicalHistory());
        chart.setDentalHistory(updated.getDentalHistory());
        chart.setPainQuality(updated.getPainQuality());
        chart.setPainLocation(updated.getPainLocation());
        chart.setPainCourseFrequency(updated.getPainCourseFrequency());
        chart.setPainOnset(updated.getPainOnset());
        chart.setPainAffectedBy(updated.getPainAffectedBy());
        chart.setLevelOfIntensity(updated.getLevelOfIntensity());
        chart.setRadiographicExamination(updated.getRadiographicExamination());
        chart.setRadiographicExaminationOthers(updated.getRadiographicExaminationOthers());
        chart.setAttachmentApparatus(updated.getAttachmentApparatus());
        chart.setAttachmentApparatusOthers(updated.getAttachmentApparatusOthers());
        chart.setClinicalConditionOfTooth(updated.getClinicalConditionOfTooth());
        chart.setClinicalConditionOthers(updated.getClinicalConditionOthers());
        chart.setSoftTissueExamination(updated.getSoftTissueExamination());
        chart.setDiagPocketSuspected(updated.getDiagPocketSuspected());
        chart.setDiagPocketAdjacent(updated.getDiagPocketAdjacent());
        chart.setDiagPocketContralateral(updated.getDiagPocketContralateral());
        chart.setDiagMobilitySuspected(updated.getDiagMobilitySuspected());
        chart.setDiagMobilityAdjacent(updated.getDiagMobilityAdjacent());
        chart.setDiagMobilityContralateral(updated.getDiagMobilityContralateral());
        chart.setDiagPercussionSuspected(updated.getDiagPercussionSuspected());
        chart.setDiagPercussionAdjacent(updated.getDiagPercussionAdjacent());
        chart.setDiagPercussionContralateral(updated.getDiagPercussionContralateral());
        chart.setDiagPalpationSuspected(updated.getDiagPalpationSuspected());
        chart.setDiagPalpationAdjacent(updated.getDiagPalpationAdjacent());
        chart.setDiagPalpationContralateral(updated.getDiagPalpationContralateral());
        chart.setDiagColdSuspected(updated.getDiagColdSuspected());
        chart.setDiagColdAdjacent(updated.getDiagColdAdjacent());
        chart.setDiagColdContralateral(updated.getDiagColdContralateral());
        chart.setDiagHeatSuspected(updated.getDiagHeatSuspected());
        chart.setDiagHeatAdjacent(updated.getDiagHeatAdjacent());
        chart.setDiagHeatContralateral(updated.getDiagHeatContralateral());
        chart.setDiagEptSuspected(updated.getDiagEptSuspected());
        chart.setDiagEptAdjacent(updated.getDiagEptAdjacent());
        chart.setDiagEptContralateral(updated.getDiagEptContralateral());
        chart.setDiagTestCavitySuspected(updated.getDiagTestCavitySuspected());
        chart.setDiagTestCavityAdjacent(updated.getDiagTestCavityAdjacent());
        chart.setDiagTestCavityContralateral(updated.getDiagTestCavityContralateral());
        chart.setPulpalDiagnosis(updated.getPulpalDiagnosis());
        chart.setPeriapicalDiagnosis(updated.getPeriapicalDiagnosis());
        chart.setRecommendedTreatment(updated.getRecommendedTreatment());
        chart.setEmergencyTreatment(updated.getEmergencyTreatment());
        chart.setPretreatment(updated.getPretreatment());
        chart.setAnesthesiaType(updated.getAnesthesiaType());
        chart.setAnesthesiaDetail(updated.getAnesthesiaDetail());
        chart.setProposedRestoration(updated.getProposedRestoration());
        chart.setConsultationsDone(updated.getConsultationsDone());
        chart.setStatus(EndodonticsChartStatus.IN_PROGRESS);
        return repo.save(chart);
    }

    public EndodonticsChart saveForm2(Long id, EndodonticsChart updated) {
        EndodonticsChart chart = repo.findById(id).orElseThrow();
        chart.setEvalToothNumber(updated.getEvalToothNumber());
        chart.setEvalDiagnosis(updated.getEvalDiagnosis());
        chart.setEvalScore1(updated.getEvalScore1());
        chart.setEvalScore2(updated.getEvalScore2());
        chart.setEvalScore3(updated.getEvalScore3());
        chart.setEvalScore4(updated.getEvalScore4());
        chart.setEvalScore5(updated.getEvalScore5());
        chart.setEvalDate(updated.getEvalDate());
        chart.setEvalTime(updated.getEvalTime());
        chart.setEvalNotes(updated.getEvalNotes());
        chart.setEpaLevel(updated.getEpaLevel());
        chart.setForm2Status(EndodonticsChartStatus.IN_PROGRESS);
        return repo.save(chart);
    }

    public EndodonticsChart submitForm2(Long id) {
        EndodonticsChart chart = repo.findById(id).orElseThrow();
        chart.setForm2Status(EndodonticsChartStatus.SUBMITTED);
        return repo.save(chart);
    }

    public EndodonticsChart submit(Long id) {
        EndodonticsChart chart = repo.findById(id).orElseThrow();
        chart.setStatus(EndodonticsChartStatus.SUBMITTED);
        return repo.save(chart);
    }

    public EndodonticsChart approve(Long id) {
        EndodonticsChart chart = repo.findById(id).orElseThrow();
        chart.setStatus(EndodonticsChartStatus.APPROVED);
        return repo.save(chart);
    }

    public EndodonticsChart revise(Long id) {
        EndodonticsChart chart = repo.findById(id).orElseThrow();
        chart.setStatus(EndodonticsChartStatus.REVISE);
        return repo.save(chart);
    }

    public EndodonticsChart approveForm2(Long id) {
        EndodonticsChart chart = repo.findById(id).orElseThrow();
        chart.setForm2Status(EndodonticsChartStatus.APPROVED);
        return repo.save(chart);
    }

    public EndodonticsChart reviseForm2(Long id) {
        EndodonticsChart chart = repo.findById(id).orElseThrow();
        chart.setForm2Status(EndodonticsChartStatus.REVISE);
        return repo.save(chart);
    }
}
