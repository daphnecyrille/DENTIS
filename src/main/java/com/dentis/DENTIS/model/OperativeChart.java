package com.dentis.DENTIS.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "operative_charts")
@Getter
@Setter
@NoArgsConstructor
public class OperativeChart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chart_no", unique = true)
    private String chartNo;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "clinician_id")
    private User clinician;

    @ManyToOne
    @JoinColumn(name = "faculty_id")
    private User faculty;

    /** Status for Form 10 (Assessment Form — operativea + operativeb). */
    @Enumerated(EnumType.STRING)
    private OperativeChartStatus status = OperativeChartStatus.CREATED;

    /** Status for Form 6 (Caries Risk Assessment — operativec). */
    @Enumerated(EnumType.STRING)
    @Column(name = "form2_status")
    private OperativeChartStatus form2Status = OperativeChartStatus.CREATED;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Form A (operativea-clinician) — Patient info & examination ───────────

    private Boolean dent171;
    private Boolean dent172;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate visitDate;

    @Column(columnDefinition = "TEXT") private String chiefComplaint;
    @Column(columnDefinition = "TEXT") private String historyOfPresentIllness;
    @Column(columnDefinition = "TEXT") private String medicalHistory;
    @Column(columnDefinition = "TEXT") private String dentalHistory;

    // Pain Symptoms
    private String painToothNumber;
    private Boolean painOnsetProvoked;
    private Boolean painOnsetSpontaneous;

    private Boolean painAffectedByCold;
    private Boolean painColdIncreased;
    private Boolean painColdDecreased;
    private Boolean painAffectedByHeat;
    private Boolean painHeatIncreased;
    private Boolean painHeatDecreased;
    private Boolean painAffectedByFoodImpaction;
    private Boolean painAffectedByChewingBiting;
    private Boolean painAffectedByPalpation;
    private Boolean painAffectedByBodyPosition;
    private Boolean painAffectedByOthers;
    private String  painAffectedByOthersSpec;

    private Boolean painDurationSeconds;
    private Boolean painDurationMinutes;
    private Boolean painDurationHours;

    private Boolean painQualitySharp;
    private Boolean painQualityDull;
    private Boolean painQualityThrobbing;

    private Integer painIntensity;

    private Boolean painFrequencyConstant;
    private Boolean painFrequencyMomentary;
    private Boolean painFrequencyIntermittent;

    private Boolean painLocationLocalized;
    private Boolean painLocationDiffused;
    private Boolean painLocationReferred;
    private String  painLocationReferredTo;

    // Radiographic Examination
    private Boolean radioExtentUpToPulp;
    private Boolean radioExtent1mm;
    private Boolean radioExtent2mm;
    private Boolean radioExtent3mmOrMore;

    private Boolean radioCervicalFloorSupra;
    private Boolean radioCervicalFloorEqui;
    private Boolean radioCervicalFloorSub;

    private Boolean radioRecurrentCariesPresent;
    private Boolean radioRecurrentCariesAbsent;

    private Boolean radioPeriapicalNormal;
    private Boolean radioPeriapicalPDLThickened;
    private Boolean radioPeriapicalWithRadiolucency;
    private Boolean radioPeriapicalWithRadiopacity;

    private Boolean radioBoneLossNone;
    private Boolean radioBoneLossHorizontal;
    private Boolean radioBoneLossVertical;

    private Boolean radioFractureLinePresent;
    private Boolean radioFractureLineAbsent;

    // Soft Tissue Examination
    private Boolean softTissueNormal;
    private Boolean softTissueSinusTract;
    private Boolean softTissueInflamed;
    private Boolean softTissueWithSwelling;
    private Boolean softTissueSwellingIntraoral;
    private Boolean softTissueSwellingExtraoral;
    private Boolean softTissuePPDNormal;
    private Boolean softTissuePPDWithPocket;
    private Boolean softTissueMobility0;
    private Boolean softTissueMobilityFirst;
    private Boolean softTissueMobilitySecond;
    private Boolean softTissueMobilityThird;

    // ── Form B (operativeb-clinician) — Diagnostic Tests ─────────────────────

    private String diagInvolvedToothNumber;
    private String diagInvolvedPocketDepth;
    private String diagInvolvedMobility;
    private String diagInvolvedPercussion;
    private String diagInvolvedPalpation;
    private String diagInvolvedCold;
    private String diagInvolvedHeat;
    private String diagInvolvedEPT;

    private String diagAdjacentToothNumber;
    private String diagAdjacentPocketDepth;
    private String diagAdjacentMobility;
    private String diagAdjacentPercussion;
    private String diagAdjacentPalpation;
    private String diagAdjacentCold;
    private String diagAdjacentHeat;
    private String diagAdjacentEPT;

    private String diagContralateralToothNumber;
    private String diagContralateralPocketDepth;
    private String diagContralateralMobility;
    private String diagContralateralPercussion;
    private String diagContralateralPalpation;
    private String diagContralateralCold;
    private String diagContralateralHeat;
    private String diagContralateralEPT;

    // ── Form C (operativec-clinician) — Caries Risk Assessment ───────────────

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate craDate;

    private String icdas0TeethCount;
    private String icdasATeethCount;
    private String icdasBTeethCount;
    private String icdasCTeethCount;

    private Boolean icdasHighest0;
    private Boolean icdasHighestA;
    private Boolean icdasHighestB;
    private Boolean icdasHighestC;

    // Risk Factor Assessment
    private Boolean rfVisiblePlaque;
    private Boolean rfMedicallyCompromised;
    private Boolean rfNoFluorideExposure;
    private Boolean rfMotherSiblingCaries;
    private Boolean rfCrowdingDeepPits;
    private Boolean rfDryMouth;
    private Boolean rfDentalAppliances;
    private Boolean rfMultipleRestorations;
    private Boolean rfSugarySnacks;
    private Boolean rfOverhangingRestorations;

    // Caries Risk Matrix — Number of Risk Factors row selected
    private Boolean riskFactors0;
    private Boolean riskFactors1or2;
    private Boolean riskFactors3orMore;

    // Caries Management Recommendation
    private Boolean prevPrimary;
    private Boolean prevSecondary;
    private Boolean prevTertiary;

    // Recall Visits
    private Boolean recallLowRisk;
    private Boolean recallModerateRisk;
    private Boolean recallHighRisk;
}
