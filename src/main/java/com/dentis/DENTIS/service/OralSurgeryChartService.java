package com.dentis.DENTIS.service;

import com.dentis.DENTIS.model.*;
import com.dentis.DENTIS.repository.OralSurgeryChartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        chart.setChartNo(generateChartNo(chartType));
        return repo.save(chart);
    }

    private String generateChartNo(OralSurgeryChartType type) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        long count = repo.countByCreatedAtBetween(start, end);
        String dateStr = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = type == OralSurgeryChartType.OSF1 ? "OSF1" : "OSF2";
        return String.format("%s-%s-%05d", prefix, dateStr, count + 1);
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

    public List<OralSurgeryChart> findAllByPatient(Patient patient) {
        return repo.findByPatientOrderByCreatedAtDesc(patient);
    }

    public List<OralSurgeryChart> findByClinicianOrderByCreatedAtDesc(User clinician) {
        return repo.findByClinicianOrderByCreatedAtDesc(clinician);
    }

    public List<OralSurgeryChart> findByFacultyOrderByCreatedAtDesc(User faculty) {
        return repo.findByFacultyOrderByCreatedAtDesc(faculty);
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
        chart.setVisitDate(updated.getVisitDate());
        chart.setStatus(OralSurgeryChartStatus.IN_PROGRESS);
        return repo.save(chart);
    }

    public OralSurgeryChart saveForm2(Long id, OralSurgeryChart updated) {
        OralSurgeryChart chart = repo.findById(id).orElseThrow();
        chart.setOdontectomyDate(updated.getOdontectomyDate());
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
        chart.setCheckedBy(updated.getCheckedBy());
        chart.setStatus(OralSurgeryChartStatus.SUBMITTED);
        return repo.save(chart);
    }
}
