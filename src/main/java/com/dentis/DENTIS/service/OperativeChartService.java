package com.dentis.DENTIS.service;

import com.dentis.DENTIS.model.*;
import com.dentis.DENTIS.repository.OperativeChartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class OperativeChartService {

    private final OperativeChartRepository repo;

    public OperativeChartService(OperativeChartRepository repo) {
        this.repo = repo;
    }

    public OperativeChart createForPatient(Patient patient, User clinician, User faculty) {
        OperativeChart chart = new OperativeChart();
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
        return String.format("RESTO10-%s-%05d", dateStr, count + 1);
    }

    public OperativeChart getById(Long id) {
        return repo.findById(id).orElseThrow();
    }

    public List<OperativeChart> findAllByPatient(Patient patient) {
        return repo.findByPatientOrderByCreatedAtDesc(patient);
    }

    public List<OperativeChart> findByClinicianOrderByCreatedAtDesc(User clinician) {
        return repo.findByClinicianOrderByCreatedAtDesc(clinician);
    }

    public List<OperativeChart> findByFacultyOrderByCreatedAtDesc(User faculty) {
        return repo.findByFacultyOrderByCreatedAtDesc(faculty);
    }

    public List<OperativeChart> findForm1ActionNeededByClinician(User clinician) {
        return repo.findByClinicianAndStatusInOrderByCreatedAtDesc(clinician,
                List.of(OperativeChartStatus.CREATED,
                        OperativeChartStatus.IN_PROGRESS,
                        OperativeChartStatus.REVISE));
    }

    public List<OperativeChart> findForm2ActionNeededByClinician(User clinician) {
        return repo.findByClinicianAndForm2StatusInOrderByCreatedAtDesc(clinician,
                List.of(OperativeChartStatus.CREATED,
                        OperativeChartStatus.IN_PROGRESS,
                        OperativeChartStatus.REVISE));
    }

    public List<OperativeChart> findForm1AwaitingApprovalByFaculty(User faculty) {
        return repo.findByFacultyAndStatusOrderByCreatedAtDesc(faculty, OperativeChartStatus.SUBMITTED);
    }

    public List<OperativeChart> findForm2AwaitingApprovalByFaculty(User faculty) {
        return repo.findByFacultyAndForm2StatusOrderByCreatedAtDesc(faculty, OperativeChartStatus.SUBMITTED);
    }

    // ── Form A (operativea) ───────────────────────────────────────────────────

    public OperativeChart saveFormA(Long id, OperativeChart updated) {
        OperativeChart c = repo.findById(id).orElseThrow();
        c.setDent171(updated.getDent171());
        c.setDent172(updated.getDent172());
        c.setVisitDate(updated.getVisitDate());
        c.setChiefComplaint(updated.getChiefComplaint());
        c.setHistoryOfPresentIllness(updated.getHistoryOfPresentIllness());
        c.setMedicalHistory(updated.getMedicalHistory());
        c.setDentalHistory(updated.getDentalHistory());
        c.setPainToothNumber(updated.getPainToothNumber());
        c.setPainOnsetProvoked(updated.getPainOnsetProvoked());
        c.setPainOnsetSpontaneous(updated.getPainOnsetSpontaneous());
        c.setPainAffectedByCold(updated.getPainAffectedByCold());
        c.setPainColdIncreased(updated.getPainColdIncreased());
        c.setPainColdDecreased(updated.getPainColdDecreased());
        c.setPainAffectedByHeat(updated.getPainAffectedByHeat());
        c.setPainHeatIncreased(updated.getPainHeatIncreased());
        c.setPainHeatDecreased(updated.getPainHeatDecreased());
        c.setPainAffectedByFoodImpaction(updated.getPainAffectedByFoodImpaction());
        c.setPainAffectedByChewingBiting(updated.getPainAffectedByChewingBiting());
        c.setPainAffectedByPalpation(updated.getPainAffectedByPalpation());
        c.setPainAffectedByBodyPosition(updated.getPainAffectedByBodyPosition());
        c.setPainAffectedByOthers(updated.getPainAffectedByOthers());
        c.setPainAffectedByOthersSpec(updated.getPainAffectedByOthersSpec());
        c.setPainDurationSeconds(updated.getPainDurationSeconds());
        c.setPainDurationMinutes(updated.getPainDurationMinutes());
        c.setPainDurationHours(updated.getPainDurationHours());
        c.setPainQualitySharp(updated.getPainQualitySharp());
        c.setPainQualityDull(updated.getPainQualityDull());
        c.setPainQualityThrobbing(updated.getPainQualityThrobbing());
        c.setPainIntensity(updated.getPainIntensity());
        c.setPainFrequencyConstant(updated.getPainFrequencyConstant());
        c.setPainFrequencyMomentary(updated.getPainFrequencyMomentary());
        c.setPainFrequencyIntermittent(updated.getPainFrequencyIntermittent());
        c.setPainLocationLocalized(updated.getPainLocationLocalized());
        c.setPainLocationDiffused(updated.getPainLocationDiffused());
        c.setPainLocationReferred(updated.getPainLocationReferred());
        c.setPainLocationReferredTo(updated.getPainLocationReferredTo());
        c.setRadioExtentUpToPulp(updated.getRadioExtentUpToPulp());
        c.setRadioExtent1mm(updated.getRadioExtent1mm());
        c.setRadioExtent2mm(updated.getRadioExtent2mm());
        c.setRadioExtent3mmOrMore(updated.getRadioExtent3mmOrMore());
        c.setRadioCervicalFloorSupra(updated.getRadioCervicalFloorSupra());
        c.setRadioCervicalFloorEqui(updated.getRadioCervicalFloorEqui());
        c.setRadioCervicalFloorSub(updated.getRadioCervicalFloorSub());
        c.setRadioRecurrentCariesPresent(updated.getRadioRecurrentCariesPresent());
        c.setRadioRecurrentCariesAbsent(updated.getRadioRecurrentCariesAbsent());
        c.setRadioPeriapicalNormal(updated.getRadioPeriapicalNormal());
        c.setRadioPeriapicalPDLThickened(updated.getRadioPeriapicalPDLThickened());
        c.setRadioPeriapicalWithRadiolucency(updated.getRadioPeriapicalWithRadiolucency());
        c.setRadioPeriapicalWithRadiopacity(updated.getRadioPeriapicalWithRadiopacity());
        c.setRadioBoneLossNone(updated.getRadioBoneLossNone());
        c.setRadioBoneLossHorizontal(updated.getRadioBoneLossHorizontal());
        c.setRadioBoneLossVertical(updated.getRadioBoneLossVertical());
        c.setRadioFractureLinePresent(updated.getRadioFractureLinePresent());
        c.setRadioFractureLineAbsent(updated.getRadioFractureLineAbsent());
        c.setSoftTissueNormal(updated.getSoftTissueNormal());
        c.setSoftTissueSinusTract(updated.getSoftTissueSinusTract());
        c.setSoftTissueInflamed(updated.getSoftTissueInflamed());
        c.setSoftTissueWithSwelling(updated.getSoftTissueWithSwelling());
        c.setSoftTissueSwellingIntraoral(updated.getSoftTissueSwellingIntraoral());
        c.setSoftTissueSwellingExtraoral(updated.getSoftTissueSwellingExtraoral());
        c.setSoftTissuePPDNormal(updated.getSoftTissuePPDNormal());
        c.setSoftTissuePPDWithPocket(updated.getSoftTissuePPDWithPocket());
        c.setSoftTissueMobility0(updated.getSoftTissueMobility0());
        c.setSoftTissueMobilityFirst(updated.getSoftTissueMobilityFirst());
        c.setSoftTissueMobilitySecond(updated.getSoftTissueMobilitySecond());
        c.setSoftTissueMobilityThird(updated.getSoftTissueMobilityThird());
        if (c.getStatus() == OperativeChartStatus.CREATED) {
            c.setStatus(OperativeChartStatus.IN_PROGRESS);
        }
        return repo.save(c);
    }

    // ── Form B (operativeb) ───────────────────────────────────────────────────

    public OperativeChart saveFormB(Long id, OperativeChart updated) {
        OperativeChart c = repo.findById(id).orElseThrow();
        c.setDiagInvolvedToothNumber(updated.getDiagInvolvedToothNumber());
        c.setDiagInvolvedPocketDepth(updated.getDiagInvolvedPocketDepth());
        c.setDiagInvolvedMobility(updated.getDiagInvolvedMobility());
        c.setDiagInvolvedPercussion(updated.getDiagInvolvedPercussion());
        c.setDiagInvolvedPalpation(updated.getDiagInvolvedPalpation());
        c.setDiagInvolvedCold(updated.getDiagInvolvedCold());
        c.setDiagInvolvedHeat(updated.getDiagInvolvedHeat());
        c.setDiagInvolvedEPT(updated.getDiagInvolvedEPT());
        c.setDiagAdjacentToothNumber(updated.getDiagAdjacentToothNumber());
        c.setDiagAdjacentPocketDepth(updated.getDiagAdjacentPocketDepth());
        c.setDiagAdjacentMobility(updated.getDiagAdjacentMobility());
        c.setDiagAdjacentPercussion(updated.getDiagAdjacentPercussion());
        c.setDiagAdjacentPalpation(updated.getDiagAdjacentPalpation());
        c.setDiagAdjacentCold(updated.getDiagAdjacentCold());
        c.setDiagAdjacentHeat(updated.getDiagAdjacentHeat());
        c.setDiagAdjacentEPT(updated.getDiagAdjacentEPT());
        c.setDiagContralateralToothNumber(updated.getDiagContralateralToothNumber());
        c.setDiagContralateralPocketDepth(updated.getDiagContralateralPocketDepth());
        c.setDiagContralateralMobility(updated.getDiagContralateralMobility());
        c.setDiagContralateralPercussion(updated.getDiagContralateralPercussion());
        c.setDiagContralateralPalpation(updated.getDiagContralateralPalpation());
        c.setDiagContralateralCold(updated.getDiagContralateralCold());
        c.setDiagContralateralHeat(updated.getDiagContralateralHeat());
        c.setDiagContralateralEPT(updated.getDiagContralateralEPT());
        if (c.getStatus() == OperativeChartStatus.CREATED) {
            c.setStatus(OperativeChartStatus.IN_PROGRESS);
        }
        return repo.save(c);
    }

    // ── Form C (operativec) ───────────────────────────────────────────────────

    public OperativeChart saveFormC(Long id, OperativeChart updated) {
        OperativeChart c = repo.findById(id).orElseThrow();
        c.setCraDate(updated.getCraDate());
        c.setIcdas0TeethCount(updated.getIcdas0TeethCount());
        c.setIcdasATeethCount(updated.getIcdasATeethCount());
        c.setIcdasBTeethCount(updated.getIcdasBTeethCount());
        c.setIcdasCTeethCount(updated.getIcdasCTeethCount());
        c.setIcdasHighest0(updated.getIcdasHighest0());
        c.setIcdasHighestA(updated.getIcdasHighestA());
        c.setIcdasHighestB(updated.getIcdasHighestB());
        c.setIcdasHighestC(updated.getIcdasHighestC());
        c.setRfVisiblePlaque(updated.getRfVisiblePlaque());
        c.setRfMedicallyCompromised(updated.getRfMedicallyCompromised());
        c.setRfNoFluorideExposure(updated.getRfNoFluorideExposure());
        c.setRfMotherSiblingCaries(updated.getRfMotherSiblingCaries());
        c.setRfCrowdingDeepPits(updated.getRfCrowdingDeepPits());
        c.setRfDryMouth(updated.getRfDryMouth());
        c.setRfDentalAppliances(updated.getRfDentalAppliances());
        c.setRfMultipleRestorations(updated.getRfMultipleRestorations());
        c.setRfSugarySnacks(updated.getRfSugarySnacks());
        c.setRfOverhangingRestorations(updated.getRfOverhangingRestorations());
        c.setRiskFactors0(updated.getRiskFactors0());
        c.setRiskFactors1or2(updated.getRiskFactors1or2());
        c.setRiskFactors3orMore(updated.getRiskFactors3orMore());
        c.setPrevPrimary(updated.getPrevPrimary());
        c.setPrevSecondary(updated.getPrevSecondary());
        c.setPrevTertiary(updated.getPrevTertiary());
        c.setRecallLowRisk(updated.getRecallLowRisk());
        c.setRecallModerateRisk(updated.getRecallModerateRisk());
        c.setRecallHighRisk(updated.getRecallHighRisk());
        if (c.getForm2Status() == OperativeChartStatus.CREATED) {
            c.setForm2Status(OperativeChartStatus.IN_PROGRESS);
        }
        return repo.save(c);
    }

    // ── Form 10 (Form A/B) status transitions ─────────────────────────────────

    public OperativeChart submitForm1(Long id) {
        OperativeChart c = repo.findById(id).orElseThrow();
        c.setStatus(OperativeChartStatus.SUBMITTED);
        return repo.save(c);
    }

    public OperativeChart approveForm1(Long id) {
        OperativeChart c = repo.findById(id).orElseThrow();
        c.setStatus(OperativeChartStatus.APPROVED);
        return repo.save(c);
    }

    public OperativeChart reviseForm1(Long id) {
        OperativeChart c = repo.findById(id).orElseThrow();
        c.setStatus(OperativeChartStatus.REVISE);
        return repo.save(c);
    }

    // ── Form 6 (Form C) status transitions ────────────────────────────────────

    public OperativeChart submitForm2(Long id) {
        OperativeChart c = repo.findById(id).orElseThrow();
        c.setForm2Status(OperativeChartStatus.SUBMITTED);
        return repo.save(c);
    }

    public OperativeChart approveForm2(Long id) {
        OperativeChart c = repo.findById(id).orElseThrow();
        c.setForm2Status(OperativeChartStatus.APPROVED);
        return repo.save(c);
    }

    public OperativeChart reviseForm2(Long id) {
        OperativeChart c = repo.findById(id).orElseThrow();
        c.setForm2Status(OperativeChartStatus.REVISE);
        return repo.save(c);
    }
}
