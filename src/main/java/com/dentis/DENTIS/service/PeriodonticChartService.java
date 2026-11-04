package com.dentis.DENTIS.service;

import com.dentis.DENTIS.model.*;
import com.dentis.DENTIS.repository.PeriodonticChartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        chart.setChartNo(patient.getChartNo() + "-PF1");
        chart.setChiefComplaint(patient.getChiefComplaint());
        return repo.save(chart);
    }

    public PeriodonticChart getById(Long id) {
        return repo.findById(id).orElseThrow();
    }

    public List<PeriodonticChart> findAllByPatient(Patient patient) {
        return repo.findByPatientOrderByCreatedAtDesc(patient);
    }

    /**
     * The patient was reassigned to another faculty: their charts in this section go to that faculty too.
     * Charts the former faculty already fully approved stay with them, as the record of who approved them.
     */
    public void reassignFaculty(Patient patient, User faculty) {
        List<PeriodonticChart> charts = findAllByPatient(patient).stream()
                .filter(c -> !(c.getStatus() == PeriodonticChartStatus.APPROVED && c.getFormCStatus() == PeriodonticChartStatus.APPROVED && c.getPf3Status() == PeriodonticChartStatus.APPROVED))
                .toList();
        charts.forEach(c -> c.setFaculty(faculty));
        repo.saveAll(charts);
    }

    public List<PeriodonticChart> findByClinicianOrderByCreatedAtDesc(User clinician) {
        return repo.findByClinicianOrderByCreatedAtDesc(clinician);
    }

    public List<PeriodonticChart> findByFacultyOrderByCreatedAtDesc(User faculty) {
        return repo.findByFacultyOrderByCreatedAtDesc(faculty);
    }

    /** Every chart of this section (all faculty of the section can see all its patients). */
    public List<PeriodonticChart> findAllOrderByCreatedAtDesc() {
        return repo.findAllByOrderByCreatedAtDesc();
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

    /** Charts where PF3 (Gingivitis Reevaluation) still needs work.
     *  Filtered in Java because charts made before PF3 existed have no PF3 status in the database. */
    public List<PeriodonticChart> findPf3ActionNeededByClinician(User clinician) {
        return repo.findByClinicianOrderByCreatedAtDesc(clinician).stream()
                .filter(c -> c.getPf3Status() == PeriodonticChartStatus.CREATED
                          || c.getPf3Status() == PeriodonticChartStatus.IN_PROGRESS
                          || c.getPf3Status() == PeriodonticChartStatus.REVISE)
                .toList();
    }

    /** Charts where Form A/B (PF1) is awaiting faculty approval. */
    public List<PeriodonticChart> findFormABAwaitingApprovalByFaculty(User faculty) {
        return repo.findByFacultyAndStatusOrderByCreatedAtDesc(faculty, PeriodonticChartStatus.SUBMITTED);
    }

    /** Charts where Form C (PF2) is awaiting faculty approval. */
    public List<PeriodonticChart> findFormCAwaitingApprovalByFaculty(User faculty) {
        return repo.findByFacultyAndFormCStatusOrderByCreatedAtDesc(faculty, PeriodonticChartStatus.SUBMITTED);
    }

    /** Charts where PF3 (Gingivitis Reevaluation) is awaiting faculty approval. */
    public List<PeriodonticChart> findPf3AwaitingApprovalByFaculty(User faculty) {
        return repo.findByFacultyAndPf3StatusOrderByCreatedAtDesc(faculty, PeriodonticChartStatus.SUBMITTED);
    }

    // ── Form A ───────────────────────────────────────────────────────────────

    public PeriodonticChart saveFormA(Long id, PeriodonticChart updated) {
        PeriodonticChart chart = repo.findById(id).orElseThrow();
        chart.setFormADate(updated.getFormADate());
        chart.setChiefComplaint(updated.getChiefComplaint());
        chart.setHistoryOfChiefComplaint(updated.getHistoryOfChiefComplaint());
        chart.setEcigaretteUse(updated.getEcigaretteUse());
        chart.setModifyingFactors(updated.getModifyingFactors());
        chart.setPredisposingFactors(updated.getPredisposingFactors());
        chart.setRiskFactors(updated.getRiskFactors());
        chart.setFormAGridJson(updated.getFormAGridJson());
        chart.setPerioChartNotes(updated.getPerioChartNotes());
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
        // Patient signature: only a drawn PNG is accepted; a new drawing updates the signing time
        String sig = updated.getPatientSignature();
        if (sig != null && !sig.equals(chart.getPatientSignature())) {
            if (sig.isBlank()) {
                chart.setPatientSignature(null);
                chart.setPatientSignedAt(null);
            } else if (isSignature(sig)) {
                chart.setPatientSignature(sig);
                chart.setPatientSignedAt(java.time.LocalDateTime.now());
            }
        }
        if (chart.getStatus() == PeriodonticChartStatus.CREATED) {
            chart.setStatus(PeriodonticChartStatus.IN_PROGRESS);
        }
        return repo.save(chart);
    }

    // ── Form B ───────────────────────────────────────────────────────────────

    public PeriodonticChart saveFormB(Long id, PeriodonticChart updated) {
        PeriodonticChart chart = repo.findById(id).orElseThrow();
        chart.setPlaqueScorePercent(updated.getPlaqueScorePercent());
        chart.setPlaqueChartNotes(updated.getPlaqueChartNotes());
        chart.setFormBGridJson(updated.getFormBGridJson());
        chart.setFormBCanvasJson(updated.getFormBCanvasJson());
        chart.setFormBRecallChartsJson(updated.getFormBRecallChartsJson());
        // plaqueIndexPercent is no longer edited separately (same value as plaqueScorePercent); left untouched so old data is kept
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
        chart.setBrushingFrequency(updated.getBrushingFrequency());
        chart.setInterdentalAid(updated.getInterdentalAid());
        chart.setMouthrinse(updated.getMouthrinse());
        chart.setOtherFindings(updated.getOtherFindings());
        // Recall 1/2 moved to PF3 — recall fields are no longer edited on PF1 page 2 (old data is kept)
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
        chart.setFormCNotes(updated.getFormCNotes());
        if (chart.getFormCStatus() == PeriodonticChartStatus.CREATED) {
            chart.setFormCStatus(PeriodonticChartStatus.IN_PROGRESS);
        }
        return repo.save(chart);
    }

    // ── PF3: Gingivitis Reevaluation Chart ─────────────────────────────────

    public PeriodonticChart savePf3(Long id, PeriodonticChart updated) {
        PeriodonticChart chart = repo.findById(id).orElseThrow();
        if (chart.getPf3CurrentRecall() == 0) return chart;      // both recalls signed: nothing can change
        boolean r1Locked = chart.isPf3Recall1Locked();           // Recall 1 signed by the faculty: keep it as signed
        chart.setPf3GridJson(r1Locked ? keepRecall1Marks(chart.getPf3GridJson(), updated.getPf3GridJson())
                                      : updated.getPf3GridJson());
        if (r1Locked) {                                          // Recall 2 opens once Recall 1 is signed
            chart.setPf3PlaqueRecall2(updated.getPf3PlaqueRecall2());
            chart.setPf3BopRecall2(updated.getPf3BopRecall2());
            chart.setRecall2Date(updated.getRecall2Date());
            chart.setRecall2Diagnosis(updated.getRecall2Diagnosis());
            chart.setRecall2Findings(updated.getRecall2Findings());
            chart.setRecall2Recommendation(updated.getRecall2Recommendation());
        }
        if (!r1Locked) {
            chart.setPf3PlaqueRecall1(updated.getPf3PlaqueRecall1());
            chart.setPf3BopRecall1(updated.getPf3BopRecall1());
            chart.setRecall1Date(updated.getRecall1Date());
            chart.setRecall1Diagnosis(updated.getRecall1Diagnosis());
            chart.setRecall1Findings(updated.getRecall1Findings());
            chart.setRecall1Recommendation(updated.getRecall1Recommendation());
        }
        if (chart.getPf3Status() == PeriodonticChartStatus.CREATED) {
            chart.setPf3Status(PeriodonticChartStatus.IN_PROGRESS);
        }
        return repo.save(chart);
    }

    private static final com.fasterxml.jackson.databind.ObjectMapper JSON = new com.fasterxml.jackson.databind.ObjectMapper();

    /** PF3 chart marks are keyed "section.r1.bop" etc.; once Recall 1 is signed its rows keep their saved values. */
    @SuppressWarnings("unchecked")
    private static String keepRecall1Marks(String savedJson, String newJson) {
        try {
            java.util.Map<String, Object> saved = savedJson == null || savedJson.isBlank()
                    ? new java.util.LinkedHashMap<>() : JSON.readValue(savedJson, java.util.LinkedHashMap.class);
            java.util.Map<String, Object> incoming = newJson == null || newJson.isBlank()
                    ? new java.util.LinkedHashMap<>() : JSON.readValue(newJson, java.util.LinkedHashMap.class);
            incoming.keySet().removeIf(k -> k.contains(".r1."));
            saved.forEach((k, v) -> { if (k.contains(".r1.")) incoming.put(k, v); });
            return JSON.writeValueAsString(incoming);
        } catch (Exception e) {
            return savedJson;   // unreadable data: keep what was saved
        }
    }

    // ── Form A/B (PF1) status transitions ────────────────────────────────────

    public PeriodonticChart submitFormAB(Long id) {
        PeriodonticChart chart = repo.findById(id).orElseThrow();
        chart.setStatus(PeriodonticChartStatus.SUBMITTED);
        return repo.save(chart);
    }

    /** Approves PF1; the faculty must draw a signature, which goes in PF1's Faculty Signature box. */
    public PeriodonticChart approveFormAB(Long id, String signature, User faculty) {
        PeriodonticChart chart = repo.findById(id).orElseThrow();
        if (chart.getStatus() != PeriodonticChartStatus.SUBMITTED) return chart;   // only a submitted chart can be reviewed
        if (!isSignature(signature)) throw new IllegalArgumentException("A drawn signature is required to approve.");
        chart.setStatus(PeriodonticChartStatus.APPROVED);
        chart.setFacultySignature(signature);
        chart.setFacultySignedAt(java.time.LocalDateTime.now());
        chart.setFacultySignedBy(faculty != null ? faculty.getFullName() : null);
        return repo.save(chart);
    }

    public PeriodonticChart reviseFormAB(Long id, String reason) {
        PeriodonticChart chart = repo.findById(id).orElseThrow();
        if (chart.getStatus() != PeriodonticChartStatus.SUBMITTED) return chart;   // only a submitted chart can be reviewed
        chart.setStatus(PeriodonticChartStatus.REVISE);
        chart.setRevisionReason(reason);
        chart.setFacultySignature(null);       // not approved, so no faculty signature
        chart.setFacultySignedAt(null);
        chart.setFacultySignedBy(null);
        return repo.save(chart);
    }

    /** A signature must be a PNG drawn on the page (data URL), under ~400 KB. */
    public static boolean isSignature(String s) {
        return s != null && s.startsWith("data:image/png;base64,") && s.length() < 400_000
                && s.substring(22).matches("[A-Za-z0-9+/=]+");
    }

    // ── Form C (PF2) status transitions ──────────────────────────────────────

    public PeriodonticChart submitFormC(Long id) {
        PeriodonticChart chart = repo.findById(id).orElseThrow();
        chart.setFormCStatus(PeriodonticChartStatus.SUBMITTED);
        return repo.save(chart);
    }

    public PeriodonticChart approveFormC(Long id) {
        PeriodonticChart chart = repo.findById(id).orElseThrow();
        if (chart.getFormCStatus() != PeriodonticChartStatus.SUBMITTED) return chart;   // only a submitted chart can be reviewed
        chart.setFormCStatus(PeriodonticChartStatus.APPROVED);
        return repo.save(chart);
    }

    public PeriodonticChart reviseFormC(Long id, String reason) {
        PeriodonticChart chart = repo.findById(id).orElseThrow();
        if (chart.getFormCStatus() != PeriodonticChartStatus.SUBMITTED) return chart;   // only a submitted chart can be reviewed
        chart.setFormCStatus(PeriodonticChartStatus.REVISE);
        chart.setFormCRevisionReason(reason);
        return repo.save(chart);
    }

    // ── PF3 (Gingivitis Reevaluation) status transitions ────────────────────

    public PeriodonticChart submitPf3(Long id) {
        PeriodonticChart chart = repo.findById(id).orElseThrow();
        chart.setPf3Status(PeriodonticChartStatus.SUBMITTED);
        return repo.save(chart);
    }

    /**
     * Approves the PF3 recall that is waiting (Recall 1, then Recall 2) with the faculty's drawn signature.
     * After Recall 1 the chart goes back to In Progress for the clinician's Recall 2 visit; after Recall 2 it is Approved.
     */
    public PeriodonticChart approvePf3(Long id, String signature, User faculty) {
        PeriodonticChart chart = repo.findById(id).orElseThrow();
        if (chart.getPf3Status() != PeriodonticChartStatus.SUBMITTED) return chart;   // only a submitted chart can be reviewed
        if (!isSignature(signature)) throw new IllegalArgumentException("A drawn signature is required to approve.");
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        String by = faculty != null ? faculty.getFullName() : null;
        if (chart.getPf3CurrentRecall() == 1) {
            chart.setPf3Recall1Signature(signature);
            chart.setPf3Recall1SignedAt(now);
            chart.setPf3Recall1SignedBy(by);
            chart.setPf3Status(PeriodonticChartStatus.IN_PROGRESS);
        } else if (chart.getPf3CurrentRecall() == 2) {
            chart.setPf3Recall2Signature(signature);
            chart.setPf3Recall2SignedAt(now);
            chart.setPf3Recall2SignedBy(by);
            chart.setPf3Status(PeriodonticChartStatus.APPROVED);
        }
        return repo.save(chart);
    }

    public PeriodonticChart revisePf3(Long id, String reason) {
        PeriodonticChart chart = repo.findById(id).orElseThrow();
        if (chart.getPf3Status() != PeriodonticChartStatus.SUBMITTED) return chart;   // only a submitted chart can be reviewed
        chart.setPf3Status(PeriodonticChartStatus.REVISE);
        chart.setPf3RevisionReason(reason);
        return repo.save(chart);
    }
}
