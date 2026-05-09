package com.dentis.DENTIS.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "oral_surgery_charts")
@Getter
@Setter
@NoArgsConstructor
public class OralSurgeryChart {

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
    private OralSurgeryChartStatus status = OralSurgeryChartStatus.CREATED;

    // ── Form 1: Patient Workup ────────────────────────────────────────────────

    @Column(columnDefinition = "TEXT")
    private String chiefComplaint;

    @Column(columnDefinition = "TEXT")
    private String historyOfPresentIllness;

    // Pain type
    private Boolean painMild = false;
    private Boolean painSevere = false;
    private Boolean painThrobbing = false;
    private Boolean painDull = false;
    private Boolean painSharp = false;

    // Pain onset
    private Boolean painProvoked = false;
    private Boolean painUnprovoked = false;

    // Pain duration
    private Boolean painProlonged = false;
    private Boolean painIntermittent = false;

    // Pain caused by
    private Boolean painHeat = false;
    private Boolean painCold = false;
    private Boolean painPercussion = false;
    private Boolean painPalpation = false;
    private Boolean painTactile = false;
    private String painOthers;

    // Vital signs
    private String bp;
    private String pr;
    private String rr;
    private String temp;
    private String o2Sat;

    @Column(columnDefinition = "TEXT")
    private String physicalOralExam;

    @Column(columnDefinition = "TEXT")
    private String intraoralExam;

    @Column(columnDefinition = "TEXT")
    private String radiographicFindings;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    private String toothNumber;

    @Column(columnDefinition = "TEXT")
    private String treatment;

    @Column(columnDefinition = "TEXT")
    private String followUp;

    private LocalDate visitDate;

    // ── Form 2: Odontectomy Workup ────────────────────────────────────────────

    private LocalDate odontectomyDate;

    @Column(columnDefinition = "TEXT")
    private String clinicalFindings;

    @Column(name = "odontectomy_radiographic_findings", columnDefinition = "TEXT")
    private String odontectomyRadiographicFindings;

    @Column(columnDefinition = "TEXT")
    private String classificationOfImpaction;

    @Column(columnDefinition = "TEXT")
    private String diagnosisTreatmentPlan;

    // Checklist
    private Boolean hasAsChart = true;
    private Boolean hasIntraoralPhoto = true;
    private Boolean hasRadiograph = true;
    private String radiographType;
    private Boolean hasImpression = true;
    private Boolean hasOtherDiagnosticAids = true;
    private String otherDiagnosticAids;
    private String checkedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
