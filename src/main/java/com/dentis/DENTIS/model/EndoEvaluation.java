package com.dentis.DENTIS.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * ENDO2 – Endodontic Evaluation & Treatment Planning (6 points).
 * Filled in by the faculty and sent to the clinician, like the Perio evaluation forms.
 * An Endo chart can have several (one per tooth the faculty checks).
 */
@Entity
@Table(name = "endo_evaluations")
@Getter
@Setter
@NoArgsConstructor
public class EndoEvaluation {

    /** Points per criterion, in order. */
    public static final String[] MAX = {"3.0", "1.5", "0.5", "0.5", "0.5"};

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "endo_chart_id", nullable = false)
    private EndodonticsChart endoChart;

    /** 1 for the first ENDO2 of the chart, 2 for the second, ... */
    private Integer visitNo;

    private String toothNumber;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    private String score1;
    private String score2;
    private String score3;
    private String score4;
    private String score5;

    private LocalDate evalDate;
    private LocalTime evalTime;

    @Column(columnDefinition = "TEXT")
    private String notes;

    /** "EPA Level 1" ... "EPA Level 5" */
    private String epaLevel;

    private LocalDateTime createdAt = LocalDateTime.now();

    @org.hibernate.annotations.UpdateTimestamp
    private LocalDateTime updatedAt;

    /** When the faculty sent it (null = draft, only the faculty sees it). */
    private LocalDateTime sentAt;

    /** When the clinician first opened it after it was sent (null = new for them). */
    private LocalDateTime viewedAt;

    public boolean isSent() { return sentAt != null; }

    public String[] getScores() { return new String[]{score1, score2, score3, score4, score5}; }

    /** Sum of the scores filled in, or null when none is. */
    public Double getTotal() {
        double t = 0; boolean any = false;
        for (String s : getScores()) {
            if (s == null || s.isBlank()) continue;
            try { t += Double.parseDouble(s.trim()); any = true; } catch (NumberFormatException ignored) { }
        }
        return any ? Math.round(t * 100) / 100.0 : null;
    }

    /** e.g. "2026-0012-ENDO2", and "-ENDO2-2" for the second one. */
    public String getFormNo() {
        String base = endoChart != null && endoChart.getChartNo() != null ? endoChart.getChartNo().replace("-ENDO1", "-ENDO2") : "ENDO2";
        return visitNo != null && visitNo > 1 ? base + "-" + visitNo : base;
    }
}
