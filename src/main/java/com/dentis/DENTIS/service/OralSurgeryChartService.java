package com.dentis.DENTIS.service;

import com.dentis.DENTIS.model.*;
import com.dentis.DENTIS.repository.OralSurgeryChartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OralSurgeryChartService {

    private final OralSurgeryChartRepository repo;

    public OralSurgeryChartService(OralSurgeryChartRepository repo) {
        this.repo = repo;
    }

    public OralSurgeryChart createForPatient(Patient patient, User clinician, User faculty,
                                             OralSurgeryChartType chartType) {
        OralSurgeryChart chart = new OralSurgeryChart();
        chart.setPatient(patient);
        chart.setClinician(clinician);
        chart.setFaculty(faculty);
        chart.setChartType(chartType);
        chart.setChartNo(patient.getChartNo() + "-" + chartType.name());   // e.g. 2026-00008-OSF1
        chart.setChiefComplaint(patient.getChiefComplaint());
        return repo.save(chart);
    }

    public OralSurgeryChart getById(Long id) {
        return repo.findById(id).orElseThrow();
    }

    public Optional<OralSurgeryChart> findByPatient(Patient patient) {
        return repo.findByPatientOrderByCreatedAtDesc(patient).stream().findFirst();
    }

    public Optional<OralSurgeryChart> findByPatientAndType(Patient patient, OralSurgeryChartType type) {
        return repo.findByPatientAndChartType(patient, type);
    }

    /** The patient's OS charts, OSF1 first, then OSF2. */
    public List<OralSurgeryChart> findAllByPatient(Patient patient) {
        return repo.findByPatientOrderByCreatedAtDesc(patient).stream()
                .sorted(java.util.Comparator.comparing((OralSurgeryChart c) -> c.getChartType() == null ? "" : c.getChartType().name()))
                .toList();
    }

    /** One time: charts numbered with the old "-OS1" / "-OS2" endings get "-OSF1" / "-OSF2", like the paper forms. */
    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void renumberOldCharts() {
        for (OralSurgeryChart c : repo.findAll()) {
            String no = c.getChartNo();
            if (no != null && (no.endsWith("-OS1") || no.endsWith("-OS2"))) {
                c.setChartNo(no.substring(0, no.length() - 3) + "OSF" + no.charAt(no.length() - 1));
                repo.save(c);
            }
        }
    }

    /**
     * The patient was reassigned to another faculty: their charts in this section go to that faculty too.
     * Charts the former faculty already fully approved stay with them, as the record of who approved them.
     */
    public void reassignFaculty(Patient patient, User faculty) {
        List<OralSurgeryChart> charts = findAllByPatient(patient).stream()
                .filter(c -> !(c.getStatus() == OralSurgeryChartStatus.APPROVED))
                .toList();
        charts.forEach(c -> c.setFaculty(faculty));
        repo.saveAll(charts);
    }

    /** True once the clinician has saved any form of this patient's charts. */
    public boolean hasStartedForms(Patient patient) {
        return findAllByPatient(patient).stream().anyMatch(c -> c.getStatus() != OralSurgeryChartStatus.CREATED);
    }

    /** Moves the patient's (not yet started) charts to a newly assigned clinician. */
    public void reassignClinician(Patient patient, User clinician) {
        List<OralSurgeryChart> charts = findAllByPatient(patient);
        charts.forEach(c -> c.setClinician(clinician));
        repo.saveAll(charts);
    }

    public List<OralSurgeryChart> findByClinicianOrderByCreatedAtDesc(User clinician) {
        return repo.findByClinicianOrderByCreatedAtDesc(clinician);
    }

    public List<OralSurgeryChart> findByFacultyOrderByCreatedAtDesc(User faculty) {
        return repo.findByFacultyOrderByCreatedAtDesc(faculty);
    }

    /** Every chart of this section (all faculty of the section can see all its patients). */
    public List<OralSurgeryChart> findAllOrderByCreatedAtDesc() {
        return repo.findAllByOrderByCreatedAtDesc();
    }

    public List<OralSurgeryChart> findActionNeededByClinician(User clinician) {
        return repo.findByClinicianAndStatusInOrderByCreatedAtDesc(clinician,
                List.of(OralSurgeryChartStatus.CREATED, OralSurgeryChartStatus.IN_PROGRESS, OralSurgeryChartStatus.REVISE));
    }

    public List<OralSurgeryChart> findAwaitingApprovalByFaculty(User faculty) {
        return repo.findByFacultyAndStatusOrderByCreatedAtDesc(faculty, OralSurgeryChartStatus.SUBMITTED);
    }

    public OralSurgeryChart saveForm1(Long id, OralSurgeryChart updated) {
        OralSurgeryChart chart = repo.findById(id).orElseThrow();
        chart.setChiefComplaint(updated.getChiefComplaint());
        chart.setHistoryOfPresentIllness(updated.getHistoryOfPresentIllness());
        chart.setPainMild(updated.getPainMild());
        chart.setPainSevere(updated.getPainSevere());
        chart.setPainThrobbing(updated.getPainThrobbing());
        chart.setPainDull(updated.getPainDull());
        chart.setPainSharp(updated.getPainSharp());
        chart.setPainProvoked(updated.getPainProvoked());
        chart.setPainUnprovoked(updated.getPainUnprovoked());
        chart.setPainProlonged(updated.getPainProlonged());
        chart.setPainIntermittent(updated.getPainIntermittent());
        chart.setPainHeat(updated.getPainHeat());
        chart.setPainCold(updated.getPainCold());
        chart.setPainPercussion(updated.getPainPercussion());
        chart.setPainPalpation(updated.getPainPalpation());
        chart.setPainTactile(updated.getPainTactile());
        chart.setPainOthers(updated.getPainOthers());
        chart.setBp(updated.getBp());
        chart.setPr(updated.getPr());
        chart.setRr(updated.getRr());
        chart.setTemp(updated.getTemp());
        chart.setO2Sat(updated.getO2Sat());
        chart.setPhysicalOralExam(updated.getPhysicalOralExam());
        chart.setIntraoralExam(updated.getIntraoralExam());
        chart.setRadiographicFindings(updated.getRadiographicFindings());
        chart.setDiagnosis(updated.getDiagnosis());
        chart.setToothNumber(updated.getToothNumber());
        chart.setTreatment(updated.getTreatment());
        chart.setFollowUp(updated.getFollowUp());
        chart.setVisitDate(updated.getVisitDate() != null ? updated.getVisitDate() : java.time.LocalDate.now());   // left empty: today
        chart.setStatus(OralSurgeryChartStatus.IN_PROGRESS);
        return repo.save(chart);
    }

    public OralSurgeryChart saveForm2(Long id, OralSurgeryChart updated) {
        OralSurgeryChart chart = repo.findById(id).orElseThrow();
        chart.setOdontectomyDate(updated.getOdontectomyDate() != null ? updated.getOdontectomyDate() : java.time.LocalDate.now());   // left empty: today
        chart.setClinicalFindings(updated.getClinicalFindings());
        chart.setOdontectomyRadiographicFindings(updated.getOdontectomyRadiographicFindings());
        chart.setClassificationOfImpaction(updated.getClassificationOfImpaction());
        chart.setDiagnosisTreatmentPlan(updated.getDiagnosisTreatmentPlan());
        chart.setHasAsChart(updated.getHasAsChart());
        chart.setHasIntraoralPhoto(updated.getHasIntraoralPhoto());
        chart.setHasRadiograph(updated.getHasRadiograph());
        chart.setRadiographType(updated.getRadiographType());
        chart.setHasImpression(updated.getHasImpression());
        chart.setHasOtherDiagnosticAids(updated.getHasOtherDiagnosticAids());
        chart.setOtherDiagnosticAids(updated.getOtherDiagnosticAids());
        chart.setChiefComplaint(updated.getChiefComplaint());
        chart.setHistoryOfPresentIllness(updated.getHistoryOfPresentIllness());
        chart.setStatus(OralSurgeryChartStatus.IN_PROGRESS);
        return repo.save(chart);
    }

    public OralSurgeryChart submit(Long id) {
        OralSurgeryChart chart = repo.findById(id).orElseThrow();
        chart.setStatus(OralSurgeryChartStatus.SUBMITTED);
        return repo.save(chart);
    }

    public OralSurgeryChart approve(Long id) { return approve(id, null, null); }

    /** Approves and records who approved it (the faculty) and when. */
    public OralSurgeryChart approve(Long id, String signature, User faculty) {
        OralSurgeryChart chart = repo.findById(id).orElseThrow();
        if (chart.getStatus() != OralSurgeryChartStatus.SUBMITTED) return chart;   // only a submitted chart can be reviewed
        // the approving faculty's name and date: OSF1 "Faculty", OSF2 "Patient Workup Checked By"
        chart.setFacultySignedAt(java.time.LocalDateTime.now());
        chart.setFacultySignedBy(faculty != null ? faculty.getFullName() : null);
        chart.setStatus(OralSurgeryChartStatus.APPROVED);
        return repo.save(chart);
    }

    public OralSurgeryChart revise(Long id, String reason) {
        OralSurgeryChart chart = repo.findById(id).orElseThrow();
        if (chart.getStatus() != OralSurgeryChartStatus.SUBMITTED) return chart;   // only a submitted chart can be reviewed
        chart.setStatus(OralSurgeryChartStatus.REVISE);
        chart.setRevisionReason(reason);
        return repo.save(chart);
    }
}
