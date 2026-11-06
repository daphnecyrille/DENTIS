package com.dentis.DENTIS.service;

import com.dentis.DENTIS.model.*;
import com.dentis.DENTIS.repository.EndodonticsChartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EndodonticsChartService {

    private final EndodonticsChartRepository repo;
    private final com.dentis.DENTIS.repository.EndoEvaluationRepository evalRepo;

    public EndodonticsChartService(EndodonticsChartRepository repo, com.dentis.DENTIS.repository.EndoEvaluationRepository evalRepo) {
        this.evalRepo = evalRepo;
        this.repo = repo;
    }

    public EndodonticsChart createForPatient(Patient patient, User clinician, User faculty) {
        EndodonticsChart chart = new EndodonticsChart();
        chart.setPatient(patient);
        chart.setClinician(clinician);
        chart.setFaculty(faculty);
        chart.setChartNo(patient.getChartNo() + "-ENDO1");
        chart.setChiefComplaint(patient.getChiefComplaint());
        return repo.save(chart);
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

    /**
     * The patient was reassigned to another faculty: their charts in this section go to that faculty too.
     * Charts the former faculty already fully approved stay with them, as the record of who approved them.
     */
    public void reassignFaculty(Patient patient, User faculty) {
        List<EndodonticsChart> charts = findAllByPatient(patient).stream()
                .filter(c -> !(c.getStatus() == EndodonticsChartStatus.APPROVED && c.getForm4Status() == EndodonticsChartStatus.APPROVED))
                .toList();
        charts.forEach(c -> c.setFaculty(faculty));
        repo.saveAll(charts);
    }

    /** True once the clinician has saved any form of this patient's charts. */
    public boolean hasStartedForms(Patient patient) {
        return findAllByPatient(patient).stream().anyMatch(c -> evalRepo.existsByEndoChart(c) || c.getStatus() != EndodonticsChartStatus.CREATED || c.getForm4Status() != EndodonticsChartStatus.CREATED);
    }

    /** Moves the patient's (not yet started) charts to a newly assigned clinician. */
    public void reassignClinician(Patient patient, User clinician) {
        List<EndodonticsChart> charts = findAllByPatient(patient);
        charts.forEach(c -> c.setClinician(clinician));
        repo.saveAll(charts);
    }

    public List<EndodonticsChart> findByClinicianOrderByCreatedAtDesc(User clinician) {
        return repo.findByClinicianOrderByCreatedAtDesc(clinician);
    }

    public List<EndodonticsChart> findByFacultyOrderByCreatedAtDesc(User faculty) {
        return repo.findByFacultyOrderByCreatedAtDesc(faculty);
    }

    /** Every chart of this section (all faculty of the section can see all its patients). */
    public List<EndodonticsChart> findAllOrderByCreatedAtDesc() {
        return repo.findAllByOrderByCreatedAtDesc();
    }

    public List<EndodonticsChart> findActionNeededByClinician(User clinician) {
        return repo.findByClinicianAndStatusInOrderByCreatedAtDesc(clinician,
                List.of(EndodonticsChartStatus.CREATED, EndodonticsChartStatus.IN_PROGRESS, EndodonticsChartStatus.REVISE));
    }

    public List<EndodonticsChart> findAwaitingApprovalByFaculty(User faculty) {
        return repo.findByFacultyAndStatusOrderByCreatedAtDesc(faculty, EndodonticsChartStatus.SUBMITTED);
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
        chart.setDiagToothSuspected(updated.getDiagToothSuspected());
        chart.setDiagToothAdjacent(updated.getDiagToothAdjacent());
        chart.setDiagToothContralateral(updated.getDiagToothContralateral());
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

    public EndodonticsChart submit(Long id) {
        EndodonticsChart chart = repo.findById(id).orElseThrow();
        chart.setStatus(EndodonticsChartStatus.SUBMITTED);
        return repo.save(chart);
    }

    public EndodonticsChart approve(Long id) {
        EndodonticsChart chart = repo.findById(id).orElseThrow();
        if (chart.getStatus() != EndodonticsChartStatus.SUBMITTED) return chart;   // only a submitted chart can be reviewed
        chart.setStatus(EndodonticsChartStatus.APPROVED);
        return repo.save(chart);
    }

    public EndodonticsChart revise(Long id, String reason) {
        EndodonticsChart chart = repo.findById(id).orElseThrow();
        if (chart.getStatus() != EndodonticsChartStatus.SUBMITTED) return chart;   // only a submitted chart can be reviewed
        chart.setStatus(EndodonticsChartStatus.REVISE);
        chart.setRevisionReason(reason);
        return repo.save(chart);
    }

    // ── Form 4 (ENDO4): Informed Consent ────────────────────────────────────

    /** Consents the clinician still has to finish (not submitted or approved). */
    public List<EndodonticsChart> findForm4ActionNeededByClinician(User clinician) {
        return repo.findByClinicianOrderByCreatedAtDesc(clinician).stream().filter(EndodonticsChart::isConsentEditable).toList();
    }

    /** Fills an empty plan from the Patient Workup (shown on the page; saved when the clinician saves). */
    public void prefillConsent(EndodonticsChart c) {
        if (c.isConsentPatientSigned()) return;
        if (blank(c.getConsentPretreatment())) c.setConsentPretreatment(c.getPretreatment());
        if (blank(c.getConsentTreatmentProper())) c.setConsentTreatmentProper(c.getRecommendedTreatment());
        if (blank(c.getConsentPostTreatment())) c.setConsentPostTreatment(c.getProposedRestoration());
        if (blank(c.getConsentToothNumber())) c.setConsentToothNumber(c.getToothNumber());
    }

    public EndodonticsChart saveConsentPlan(Long id, java.util.Map<String, String> p) {
        EndodonticsChart c = repo.findById(id).orElseThrow();
        if (!c.isConsentEditable() || c.isConsentPatientSigned()) return c;   // locked once the patient signs
        c.setConsentPretreatment(trim(p.get("consentPretreatment")));
        c.setConsentTreatmentProper(trim(p.get("consentTreatmentProper")));
        c.setConsentPostTreatment(trim(p.get("consentPostTreatment")));
        c.setConsentToothNumber(trim(p.get("consentToothNumber")));
        c.setConsentRctFee(trim(p.get("consentRctFee")));
        c.setConsentRadiographs(trim(p.get("consentRadiographs")));
        c.setConsentRadiographFee(trim(p.get("consentRadiographFee")));
        if (c.getForm4Status() == EndodonticsChartStatus.CREATED) c.setForm4Status(EndodonticsChartStatus.IN_PROGRESS);
        return repo.save(c);
    }

    public EndodonticsChart patientSignConsent(Long id, String type, String name, String signature) {
        EndodonticsChart c = repo.findById(id).orElseThrow();
        if (!c.isConsentEditable() || c.isConsentPatientSigned() || !PeriodonticChartService.isSignature(signature)) return c;
        if (!"PATIENT".equals(type) && blank(name)) return c;
        String who = java.util.Set.of("PATIENT", "PARENT", "GUARDIAN").contains(type) ? type : "PATIENT";
        c.setConsentSignerType(who);
        // the patient signing for themselves: always the name on their record
        c.setConsentSignerName("PATIENT".equals(who) && c.getPatient() != null
                ? c.getPatient().getFirstName() + " " + c.getPatient().getSurname() : name.trim());
        c.setConsentPatientSignature(signature);
        c.setConsentPatientSignedAt(java.time.LocalDateTime.now());
        if (c.getForm4Status() == EndodonticsChartStatus.CREATED) c.setForm4Status(EndodonticsChartStatus.IN_PROGRESS);
        return repo.save(c);
    }

    public EndodonticsChart clinicianSignConsent(Long id, String signature, User clinician) {
        EndodonticsChart c = repo.findById(id).orElseThrow();
        if (!c.isConsentEditable() || !c.isConsentPatientSigned() || !PeriodonticChartService.isSignature(signature)) return c;
        c.setConsentClinicianSignature(signature);
        c.setConsentClinicianSignedAt(java.time.LocalDateTime.now());
        c.setConsentClinicianSignedBy(clinician != null ? clinician.getFullName() : null);
        return repo.save(c);
    }

    /** Clears both signatures so the plan can be changed; the patient signs again. */
    public EndodonticsChart restartConsent(Long id) {
        EndodonticsChart c = repo.findById(id).orElseThrow();
        if (!c.isConsentEditable()) return c;
        c.setConsentPatientSignature(null); c.setConsentPatientSignedAt(null);
        c.setConsentSignerType(null); c.setConsentSignerName(null);
        c.setConsentClinicianSignature(null); c.setConsentClinicianSignedAt(null); c.setConsentClinicianSignedBy(null);
        c.setForm4Status(EndodonticsChartStatus.IN_PROGRESS);
        return repo.save(c);
    }

    public EndodonticsChart submitConsent(Long id) {
        EndodonticsChart c = repo.findById(id).orElseThrow();
        if (c.isConsentEditable() && c.isConsentPatientSigned() && c.isConsentClinicianSigned())
            c.setForm4Status(EndodonticsChartStatus.SUBMITTED);
        return repo.save(c);
    }

    public EndodonticsChart approveConsent(Long id, String signature, User faculty) {
        EndodonticsChart c = repo.findById(id).orElseThrow();
        if (c.getForm4Status() != EndodonticsChartStatus.SUBMITTED || !PeriodonticChartService.isSignature(signature)) return c;
        c.setForm4Status(EndodonticsChartStatus.APPROVED);
        c.setConsentFacultySignature(signature);
        c.setConsentFacultySignedAt(java.time.LocalDateTime.now());
        c.setConsentFacultySignedBy(faculty != null ? faculty.getFullName() : null);
        return repo.save(c);
    }

    public EndodonticsChart reviseConsent(Long id, String reason) {
        EndodonticsChart c = repo.findById(id).orElseThrow();
        if (c.getForm4Status() != EndodonticsChartStatus.SUBMITTED) return c;
        c.setForm4Status(EndodonticsChartStatus.REVISE);
        c.setForm4RevisionReason(reason);
        return repo.save(c);
    }

    private static boolean blank(String s) { return s == null || s.isBlank(); }
    private static String trim(String s) { return s == null || s.isBlank() ? null : s.trim(); }
}
