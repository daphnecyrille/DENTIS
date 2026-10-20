package com.dentis.DENTIS.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Stores one evaluation form (PF4–PF18) per PeriodonticChart.
 * All grade fields are nullable Integer (1–5).
 * Criteria not used by a given formType remain null.
 */
@Entity
@Table(name = "perio_evaluations")
@Getter
@Setter
@NoArgsConstructor
public class PerioEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "perio_chart_id", nullable = false)
    private PeriodonticChart perioChart;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PerioEvalFormType formType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PerioEvalStatus status = PerioEvalStatus.DRAFT;

    private LocalDate date;

    /** "Scaling case number" (PF9) or "Root Debridement case no." (PF10). */
    private String caseNumber;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    /** EPA Grade field shown on PF5 and PF17. */
    private String epaGrade;

    /**
     * All grades of the form as JSON, e.g. {"determinedHistory":4.5,"healthEd1st":4}.
     * Grades may have decimals (1–5), so they are kept here rather than in the older Integer columns below.
     */
    @Column(columnDefinition = "TEXT")
    private String gradesJson;

    /** Computed total score stored on submit. */
    private Double totalScore;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Visit number for forms that can be added more than once (PF7, PF18); 1 for the others. */
    private Integer visitNo;

    /** When the clinician first opened the sent evaluation (null = still new/unread for them). */
    private LocalDateTime viewedAt;

    /** When the faculty sent the evaluation to the clinician (null while it is a draft). */
    private LocalDateTime sentAt;

    // ── Left / Single table criteria (all nullable, grade 1–5) ──────────────

    /** Determined the history of chief complaint — PF4(x1) PF5(x1) PF6(x1) */
    private Integer determinedHistory;

    /** Assessed the periodontal condition visually — PF4(x2) PF5(x2) PF6(x2) */
    private Integer assessedPerioVisually;

    /** Assessed the patient's oral hygiene status — PF4(x2) PF5(x2) PF6(x2) */
    private Integer assessedOralHygiene;

    /** Applied the clinical parameters — PF4(x4) PF5(x4) PF6(x4) PF7(x3) PF17(x3) PF18(x3) */
    private Integer appliedClinicalParameters;

    /** Diagnosed and classified the existing periodontal condition — PF4(x4) PF5(x4) PF6(x4) PF7(x4) PF17(x4) PF18(x4) */
    private Integer diagnosedClassified;

    /** Formulated an individualized holistic treatment plan — PF4(x3) PF5(x3) PF6(x3) */
    private Integer formulatedTreatmentPlan;

    /** Demonstrated skills in critical thinking — PF6(x4) PF7(x3) PF17(x3) PF18(x3) */
    private Integer demonstratedCriticalThinking;

    /** Determined the patient's further treatment needs — PF7(x2) PF17(x2) PF18(x2) */
    private Integer determinedFurtherTreatmentNeeds;

    /** Performed responsive oral health education — PF7(x1) PF17(x1) PF18(x1) */
    private Integer performedOralHealthEducation;

    /** Performed necessary treatment based on assessed needs — PF7(x2) PF17(x2) PF18(x2) */
    private Integer performedNecessaryTreatment;

    // ── Right table criteria – 1st / 2nd / 3rd attempt (PF4, PF5, PF6) ──────

    /** Performed responsive health education through aids — x2 */
    private Integer healthEd1st, healthEd2nd, healthEd3rd;

    /** Employed principles of instrumentation during debridement — x4 */
    private Integer instrumentation1st, instrumentation2nd, instrumentation3rd;

    /** Performed debridement while maintaining integrity of soft tissues — PF4(x3) PF5(x3) PF6(x4) */
    private Integer debridementTissues1st, debridementTissues2nd, debridementTissues3rd;

    /** Planed the root surface while maintaining integrity of soft tissues — PF6(x3) */
    private Integer rootPlaning1st, rootPlaning2nd, rootPlaning3rd;

    /** Employed the principles of infection control — x1 */
    private Integer infectionControlRight1st, infectionControlRight2nd, infectionControlRight3rd;

    /** Maintained awareness and sensitivity to patient's needs — x2 */
    private Integer awareness1st, awareness2nd, awareness3rd;

    // ── PF9 / PF10 criteria (Periodontal Models) ─────────────────────────────

    /** Demonstrated the correct instrument grasp — x1 */
    private Integer instrumentGrasp;

    /** Used an appropriate finger rest — x1 */
    private Integer fingerRest;

    /** Selected and adapted the correct instrument/working end/cutting edge — x1 */
    private Integer selectedInstrument;

    /** Employed the correct instrumentation stroke — x1 */
    private Integer instrumentationStroke;

    /** Demonstrated the proper operating position — x1 */
    private Integer operatingPosition;

    /** Performed debridement while maintaining integrity of (artificial) soft tissues — PF9(x3) PF10(x4) */
    private Integer debridementModel;

    /** Employed the principles of infection control — PF9(x2) PF10(x2) */
    private Integer infectionControlModel;
}
