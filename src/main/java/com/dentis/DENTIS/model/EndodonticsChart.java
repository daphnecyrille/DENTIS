package com.dentis.DENTIS.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "endodontics_charts")
@Getter
@Setter
@NoArgsConstructor
public class EndodonticsChart {

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

    @Enumerated(EnumType.STRING)
    private EndodonticsChartStatus status = EndodonticsChartStatus.CREATED;

    @Enumerated(EnumType.STRING)
    private EndodonticsChartStatus form2Status = EndodonticsChartStatus.CREATED;

    @PostLoad
    private void onLoad() {
        if (form2Status == null) form2Status = EndodonticsChartStatus.CREATED;
    }

    // ── Form 1: Patient Workup (Clinician) ──────────────────────────────────

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate visitDate;

    private String toothNumber;

    private String referredBy;
    private String clinicianContactNumber;

    @Column(columnDefinition = "TEXT")
    private String chiefComplaint;

    @Column(columnDefinition = "TEXT")
    private String historyOfPresentIllness;

    @Column(columnDefinition = "TEXT")
    private String medicalHistory;

    @Column(columnDefinition = "TEXT")
    private String dentalHistory;

    // Pain Symptoms
    private String painQuality;
    private String painLocation;
    private String painCourseFrequency;
    private String painOnset;
    private String painAffectedBy;
    private String levelOfIntensity;

    // Radiographic Examination
    private String radiographicExamination;
    private String radiographicExaminationOthers;

    // Attachment Apparatus
    private String attachmentApparatus;
    private String attachmentApparatusOthers;

    // Clinical Condition of the Tooth
    private String clinicalConditionOfTooth;
    private String clinicalConditionOthers;

    // Soft Tissue Examination
    private String softTissueExamination;

    // Diagnostic Tests — 8 rows × 3 columns
    private String diagPocketSuspected;
    private String diagPocketAdjacent;
    private String diagPocketContralateral;

    private String diagMobilitySuspected;
    private String diagMobilityAdjacent;
    private String diagMobilityContralateral;

    private String diagPercussionSuspected;
    private String diagPercussionAdjacent;
    private String diagPercussionContralateral;

    private String diagPalpationSuspected;
    private String diagPalpationAdjacent;
    private String diagPalpationContralateral;

    private String diagColdSuspected;
    private String diagColdAdjacent;
    private String diagColdContralateral;

    private String diagHeatSuspected;
    private String diagHeatAdjacent;
    private String diagHeatContralateral;

    private String diagEptSuspected;
    private String diagEptAdjacent;
    private String diagEptContralateral;

    private String diagTestCavitySuspected;
    private String diagTestCavityAdjacent;
    private String diagTestCavityContralateral;

    // Diagnosis
    private String pulpalDiagnosis;
    private String periapicalDiagnosis;

    @Column(columnDefinition = "TEXT")
    private String recommendedTreatment;

    // Proposed Treatment Plan
    @Column(columnDefinition = "TEXT")
    private String emergencyTreatment;

    @Column(columnDefinition = "TEXT")
    private String pretreatment;

    private String anesthesiaType;
    private String anesthesiaDetail;

    @Column(columnDefinition = "TEXT")
    private String proposedRestoration;

    @Column(columnDefinition = "TEXT")
    private String consultationsDone;

    // ── Form 2: Endodontic Evaluation (Faculty) ──────────────────────────────

    private String evalToothNumber;
    private String evalDiagnosis;

    private String evalScore1;
    private String evalScore2;
    private String evalScore3;
    private String evalScore4;
    private String evalScore5;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate evalDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime evalTime;

    @Column(columnDefinition = "TEXT")
    private String evalNotes;

    private String epaLevel;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
