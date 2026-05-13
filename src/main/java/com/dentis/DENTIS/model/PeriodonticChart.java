package com.dentis.DENTIS.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "periodontic_charts")
@Getter
@Setter
@NoArgsConstructor
public class PeriodonticChart {

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

    /** Status for Form A+B (Periodontal Chart — PF1). */
    @Enumerated(EnumType.STRING)
    private PeriodonticChartStatus status = PeriodonticChartStatus.CREATED;

    /** Status for Form C (PPD/CAL/BOP Chart — PF2). */
    @Enumerated(EnumType.STRING)
    @Column(name = "form_c_status")
    private PeriodonticChartStatus formCStatus = PeriodonticChartStatus.CREATED;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Form A: Periodontal Chart ─────────────────────────────────────────────

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate formADate;

    @Column(columnDefinition = "TEXT")
    private String chiefComplaint;

    @Column(columnDefinition = "TEXT")
    private String historyOfChiefComplaint;

    private String modifyingFactors;
    private String predisposingFactors;
    private String riskFactors;

    /** JSON: serialized upper+lower grid rows (B, L, empty, extra). */
    @Column(columnDefinition = "TEXT")
    private String formAGridJson;

    /** JSON: canvas annotation positions+symbols for Form A. */
    @Column(columnDefinition = "TEXT")
    private String formACanvasJson;

    // Occlusal examination — Buccal / Lingual
    @Column(columnDefinition = "TEXT") private String occColorBuccal;
    @Column(columnDefinition = "TEXT") private String occColorLingual;
    @Column(columnDefinition = "TEXT") private String occTextureBuccal;
    @Column(columnDefinition = "TEXT") private String occTextureLingual;
    @Column(columnDefinition = "TEXT") private String occAdaptationBuccal;
    @Column(columnDefinition = "TEXT") private String occAdaptationLingual;
    @Column(columnDefinition = "TEXT") private String occConsistencyBuccal;
    @Column(columnDefinition = "TEXT") private String occConsistencyLingual;
    @Column(columnDefinition = "TEXT") private String occFormBuccal;
    @Column(columnDefinition = "TEXT") private String occFormLingual;

    @Column(columnDefinition = "TEXT") private String diagnosis;
    @Column(columnDefinition = "TEXT") private String treatmentPlan;

    // ── Form B: Plaque / Bleeding Chart ──────────────────────────────────────

    private String plaqueScorePercent;

    /** JSON: box-spot states (+/-/X/blank) for upper+lower grid. */
    @Column(columnDefinition = "TEXT")
    private String formBGridJson;

    /** JSON: canvas X-mark positions for Form B. */
    @Column(columnDefinition = "TEXT")
    private String formBCanvasJson;

    private String plaqueIndexPercent;

    // Plaque table — 6 index teeth × 4 surfaces (MB, MidB, DB, L)
    private String pt12MB; private String pt12MidB; private String pt12DB; private String pt12L;
    private String pt16MB; private String pt16MidB; private String pt16DB; private String pt16L;
    private String pt24MB; private String pt24MidB; private String pt24DB; private String pt24L;
    private String pt32MB; private String pt32MidB; private String pt32DB; private String pt32L;
    private String pt36MB; private String pt36MidB; private String pt36DB; private String pt36L;
    private String pt44MB; private String pt44MidB; private String pt44DB; private String pt44L;

    @Column(columnDefinition = "TEXT") private String otherFindings;

    @Column(columnDefinition = "TEXT") private String recall1Diagnosis;
    @Column(columnDefinition = "TEXT") private String recall1Findings;
    @Column(columnDefinition = "TEXT") private String recall1Recommendation;

    @Column(columnDefinition = "TEXT") private String recall2Diagnosis;
    @Column(columnDefinition = "TEXT") private String recall2Findings;
    @Column(columnDefinition = "TEXT") private String recall2Recommendation;

    // ── Form C: PPD / CAL / BOP Measurement Chart ────────────────────────────

    /** JSON: all c-grid-row input values for the left (upper) panel. */
    @Column(columnDefinition = "TEXT")
    private String formCLeftGridJson;

    /** JSON: all c-grid-row input values for the right (lower) panel. */
    @Column(columnDefinition = "TEXT")
    private String formCRightGridJson;

    /** JSON: canvas circle/half/full marks for the left canvas. */
    @Column(columnDefinition = "TEXT")
    private String formCLeftCanvasJson;

    /** JSON: canvas marks for the right canvas. */
    @Column(columnDefinition = "TEXT")
    private String formCRightCanvasJson;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate formCDate;

    private Boolean isInitialExamination;
    private Boolean isReevaluation;
    private Boolean isMaintenanceVisit;
}
