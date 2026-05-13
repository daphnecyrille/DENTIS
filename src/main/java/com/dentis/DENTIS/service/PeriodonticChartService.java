package com.dentis.DENTIS.service;

import com.dentis.DENTIS.model.*;
import com.dentis.DENTIS.repository.PeriodonticChartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class PeriodonticChartService {

    private final PeriodonticChartRepository repo;

    public PeriodonticChartService(PeriodonticChartRepository repo) {
        this.repo = repo;
    }

    public PeriodonticChart createForPatient(Patient patient, User clinician, User faculty) {
        PeriodonticChart chart = new PeriodonticChart();
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
        return String.format("PF1-%s-%05d", dateStr, count + 1);
    }

    public PeriodonticChart getById(Long id) {
        return repo.findById(id).orElseThrow();
    }

    public List<PeriodonticChart> findAllByPatient(Patient patient) {
        return repo.findByPatientOrderByCreatedAtDesc(patient);
    }

    public List<PeriodonticChart> findByClinicianOrderByCreatedAtDesc(User clinician) {
        return repo.findByClinicianOrderByCreatedAtDesc(clinician);
    }

    public List<PeriodonticChart> findByFacultyOrderByCreatedAtDesc(User faculty) {
        return repo.findByFacultyOrderByCreatedAtDesc(faculty);
    }

    /** Charts where Form A/B (PF1) still needs submission. */
    public List<PeriodonticChart> findFormABActionNeededByClinician(User clinician) {
        return repo.findByClinicianAndStatusInOrderByCreatedAtDesc(clinician,
                List.of(PeriodonticChartStatus.CREATED,
                        PeriodonticChartStatus.IN_PROGRESS,
                        PeriodonticChartStatus.REVISE));
    }

    /** Charts where Form C (PF2) still needs submission. */
    public List<PeriodonticChart> findFormCActionNeededByClinician(User clinician) {
        return repo.findByClinicianAndFormCStatusInOrderByCreatedAtDesc(clinician,
                List.of(PeriodonticChartStatus.CREATED,
                        PeriodonticChartStatus.IN_PROGRESS,
                        PeriodonticChartStatus.REVISE));
    }

    /** Charts where Form A/B (PF1) is awaiting faculty approval. */
    public List<PeriodonticChart> findFormABAwaitingApprovalByFaculty(User faculty) {
        return repo.findByFacultyAndStatusOrderByCreatedAtDesc(faculty, PeriodonticChartStatus.SUBMITTED);
    }

    /** Charts where Form C (PF2) is awaiting faculty approval. */
    public List<PeriodonticChart> findFormCAwaitingApprovalByFaculty(User faculty) {
        return repo.findByFacultyAndFormCStatusOrderByCreatedAtDesc(faculty, PeriodonticChartStatus.SUBMITTED);
    }

    // ── Form A ───────────────────────────────────────────────────────────────

    public PeriodonticChart saveFormA(Long id, PeriodonticChart updated) {
        PeriodonticChart chart = repo.findById(id).orElseThrow();
        chart.setFormADate(updated.getFormADate());
        chart.setChiefComplaint(updated.getChiefComplaint());
        chart.setHistoryOfChiefComplaint(updated.getHistoryOfChiefComplaint());
        chart.setModifyingFactors(updated.getModifyingFactors());
        chart.setPredisposingFactors(updated.getPredisposingFactors());
        chart.setRiskFactors(updated.getRiskFactors());
        chart.setFormAGridJson(updated.getFormAGridJson());
        chart.setFormACanvasJson(updated.getFormACanvasJson());
        chart.setOccColorBuccal(updated.getOccColorBuccal());
        chart.setOccColorLingual(updated.getOccColorLingual());
        chart.setOccTextureBuccal(updated.getOccTextureBuccal());
        chart.setOccTextureLingual(updated.getOccTextureLingual());
        chart.setOccAdaptationBuccal(updated.getOccAdaptationBuccal());
        chart.setOccAdaptationLingual(updated.getOccAdaptationLingual());
        chart.setOccConsistencyBuccal(updated.getOccConsistencyBuccal());
        chart.setOccConsistencyLingual(updated.getOccConsistencyLingual());
        chart.setOccFormBuccal(updated.getOccFormBuccal());
        chart.setOccFormLingual(updated.getOccFormLingual());
        chart.setDiagnosis(updated.getDiagnosis());
        chart.setTreatmentPlan(updated.getTreatmentPlan());
        if (chart.getStatus() == PeriodonticChartStatus.CREATED) {
            chart.setStatus(PeriodonticChartStatus.IN_PROGRESS);
        }
        return repo.save(chart);
    }

    // ── Form B ───────────────────────────────────────────────────────────────

    public PeriodonticChart saveFormB(Long id, PeriodonticChart updated) {
        PeriodonticChart chart = repo.findById(id).orElseThrow();
        chart.setPlaqueScorePercent(updated.getPlaqueScorePercent());
        chart.setFormBGridJson(updated.getFormBGridJson());
        chart.setFormBCanvasJson(updated.getFormBCanvasJson());
        chart.setPlaqueIndexPercent(updated.getPlaqueIndexPercent());
        chart.setPt12MB(updated.getPt12MB()); chart.setPt12MidB(updated.getPt12MidB());
        chart.setPt12DB(updated.getPt12DB()); chart.setPt12L(updated.getPt12L());
        chart.setPt16MB(updated.getPt16MB()); chart.setPt16MidB(updated.getPt16MidB());
        chart.setPt16DB(updated.getPt16DB()); chart.setPt16L(updated.getPt16L());
        chart.setPt24MB(updated.getPt24MB()); chart.setPt24MidB(updated.getPt24MidB());
        chart.setPt24DB(updated.getPt24DB()); chart.setPt24L(updated.getPt24L());
        chart.setPt32MB(updated.getPt32MB()); chart.setPt32MidB(updated.getPt32MidB());
        chart.setPt32DB(updated.getPt32DB()); chart.setPt32L(updated.getPt32L());
        chart.setPt36MB(updated.getPt36MB()); chart.setPt36MidB(updated.getPt36MidB());
        chart.setPt36DB(updated.getPt36DB()); chart.setPt36L(updated.getPt36L());
        chart.setPt44MB(updated.getPt44MB()); chart.setPt44MidB(updated.getPt44MidB());
        chart.setPt44DB(updated.getPt44DB()); chart.setPt44L(updated.getPt44L());
        chart.setOtherFindings(updated.getOtherFindings());
        chart.setRecall1Diagnosis(updated.getRecall1Diagnosis());
        chart.setRecall1Findings(updated.getRecall1Findings());
        chart.setRecall1Recommendation(updated.getRecall1Recommendation());
        chart.setRecall2Diagnosis(updated.getRecall2Diagnosis());
        chart.setRecall2Findings(updated.getRecall2Findings());
        chart.setRecall2Recommendation(updated.getRecall2Recommendation());
        if (chart.getStatus() == PeriodonticChartStatus.CREATED) {
            chart.setStatus(PeriodonticChartStatus.IN_PROGRESS);
        }
        return repo.save(chart);
    }

    // ── Form C ───────────────────────────────────────────────────────────────

    public PeriodonticChart saveFormC(Long id, PeriodonticChart updated) {
        PeriodonticChart chart = repo.findById(id).orElseThrow();
        chart.setFormCLeftGridJson(updated.getFormCLeftGridJson());
        chart.setFormCRightGridJson(updated.getFormCRightGridJson());
        chart.setFormCLeftCanvasJson(updated.getFormCLeftCanvasJson());
        chart.setFormCRightCanvasJson(updated.getFormCRightCanvasJson());
        chart.setFormCDate(updated.getFormCDate());
        chart.setIsInitialExamination(updated.getIsInitialExamination());
        chart.setIsReevaluation(updated.getIsReevaluation());
        chart.setIsMaintenanceVisit(updated.getIsMaintenanceVisit());
        if (chart.getFormCStatus() == PeriodonticChartStatus.CREATED) {
            chart.setFormCStatus(PeriodonticChartStatus.IN_PROGRESS);
        }
        return repo.save(chart);
    }

    // ── Form A/B (PF1) status transitions ────────────────────────────────────

    public PeriodonticChart submitFormAB(Long id) {
        PeriodonticChart chart = repo.findById(id).orElseThrow();
        chart.setStatus(PeriodonticChartStatus.SUBMITTED);
        return repo.save(chart);
    }

    public PeriodonticChart approveFormAB(Long id) {
        PeriodonticChart chart = repo.findById(id).orElseThrow();
        chart.setStatus(PeriodonticChartStatus.APPROVED);
        return repo.save(chart);
    }

    public PeriodonticChart reviseFormAB(Long id) {
        PeriodonticChart chart = repo.findById(id).orElseThrow();
        chart.setStatus(PeriodonticChartStatus.REVISE);
        return repo.save(chart);
    }

    // ── Form C (PF2) status transitions ──────────────────────────────────────

    public PeriodonticChart submitFormC(Long id) {
        PeriodonticChart chart = repo.findById(id).orElseThrow();
        chart.setFormCStatus(PeriodonticChartStatus.SUBMITTED);
        return repo.save(chart);
    }

    public PeriodonticChart approveFormC(Long id) {
        PeriodonticChart chart = repo.findById(id).orElseThrow();
        chart.setFormCStatus(PeriodonticChartStatus.APPROVED);
        return repo.save(chart);
    }

    public PeriodonticChart reviseFormC(Long id) {
        PeriodonticChart chart = repo.findById(id).orElseThrow();
        chart.setFormCStatus(PeriodonticChartStatus.REVISE);
        return repo.save(chart);
    }
}
