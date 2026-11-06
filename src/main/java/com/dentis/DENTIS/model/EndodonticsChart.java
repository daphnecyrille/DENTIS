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

    /** Form 4 (ENDO4): Patient Information and Informed Consent. */
    @Enumerated(EnumType.STRING)
    private EndodonticsChartStatus form4Status = EndodonticsChartStatus.CREATED;

    @PostLoad
    private void onLoad() {
        if (form2Status == null) form2Status = EndodonticsChartStatus.CREATED;
        if (form4Status == null) form4Status = EndodonticsChartStatus.CREATED;
    }

    // ── Form 1: Patient Workup (Clinician) ──────────────────────────────────

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate visitDate;

    private String toothNumber;

    private String referredBy;


    /** "PAIN SYMPTOMS: TOOTH #" */
    private String painToothNumber;
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
    /** Tooth numbers of the Diagnostic Tests columns (FDI). */
    private String diagToothSuspected;
    private String diagToothAdjacent;
    private String diagToothContralateral;

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
    private String anesthesiaDetail;   // old single "details" box (kept for existing charts)

    // Anesthesia: infiltration / block / supplemental can each be ticked, each with its own location
    private String anesthesiaInfiltrationLocation;
    private String anesthesiaBlockLocation;
    private String anesthesiaSupplementalLocation;

    @Column(columnDefinition = "TEXT")
    private String proposedRestoration;

    @Column(columnDefinition = "TEXT")
    private String consultationsDone;

    // ── Old Form 2 fields: ENDO2 is now an EndoEvaluation (several per chart). Kept so existing
    //    data can be moved over once (EndoEvaluationService.moveOldEvaluations), then unused. ──

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

    @Column(columnDefinition = "TEXT")
    private String revisionReason;

    @Column(columnDefinition = "TEXT")
    private String form2RevisionReason;

    // ── Form 4 (ENDO4): Patient Information and Informed Consent ─────────────
    // The clinician fills in the plan and cost, the patient reads and signs, the clinician signs,
    // then it is submitted and the faculty signs when approving. Plan and cost lock once the patient signs.

    @Column(columnDefinition = "TEXT") private String consentPretreatment;
    @Column(columnDefinition = "TEXT") private String consentTreatmentProper;
    @Column(columnDefinition = "TEXT") private String consentPostTreatment;
    private String consentToothNumber;
    private String consentRctFee;
    private String consentRadiographs;
    private String consentRadiographFee;

    /** PATIENT, PARENT or GUARDIAN */
    private String consentSignerType;
    private String consentSignerName;
    @Column(columnDefinition = "TEXT") private String consentPatientSignature;
    private LocalDateTime consentPatientSignedAt;

    @Column(columnDefinition = "TEXT") private String consentClinicianSignature;
    private LocalDateTime consentClinicianSignedAt;
    private String consentClinicianSignedBy;

    @Column(columnDefinition = "TEXT") private String consentFacultySignature;
    private LocalDateTime consentFacultySignedAt;
    private String consentFacultySignedBy;

    @Column(columnDefinition = "TEXT") private String form4RevisionReason;

    public boolean isConsentPatientSigned() { return consentPatientSignature != null; }
    public boolean isConsentClinicianSigned() { return consentClinicianSignature != null; }

    /** The clinician can still change it (not submitted or approved). */
    public boolean isConsentEditable() {
        return form4Status != EndodonticsChartStatus.SUBMITTED && form4Status != EndodonticsChartStatus.APPROVED;
    }

    /** Same statuses as every other form: Not Started, In Progress, Awaiting Approval, Approved, For Revision. */
    public String getForm4Label() {
        return (form4Status == null ? EndodonticsChartStatus.CREATED : form4Status).getDisplayName();
    }

    public String getForm4Css() {
        return (form4Status == null ? EndodonticsChartStatus.CREATED : form4Status).getCssClass();
    }

    public String getSignerLabel() {
        if (consentSignerType == null) return "Patient";
        return switch (consentSignerType) { case "PARENT" -> "Parent"; case "GUARDIAN" -> "Guardian"; default -> "Patient"; };
    }

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Set automatically every time the chart is saved (Patient List "Last Updated"). */
    @org.hibernate.annotations.UpdateTimestamp
    private LocalDateTime updatedAt;
}
