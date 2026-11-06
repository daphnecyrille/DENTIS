package com.dentis.DENTIS.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * One Operative evaluation (RESTO FORM 1, RESTO FORM 2 or the NOC sealant form) for an Operative chart.
 * The faculty adds it (one per tooth / procedure), grades it, then sends it to the clinician.
 */
@Entity
@Table(name = "resto_evaluations")
@Getter
@Setter
@NoArgsConstructor
public class RestoEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "operative_chart_id", nullable = false)
    private OperativeChart operativeChart;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RestoEvalFormType formType;

    /** 1 for the first form of this type on the chart, 2 for the second, ... */
    private Integer visitNo;

    /** "Tooth No., surface/s involved" */
    private String toothSurface;
    /** RESTO1: resto material */
    private String restoMaterial;
    /** RESTO1: I/III, II/IV, V or ONLAY (credit table column) */
    private String cavityClass;
    /** RESTO1: SHALLOW_050, SHALLOW_075, MODERATE or EXTENSIVE (credit multiplier) */
    private String cavitySize;
    /** RESTO2: PATIENT or SIMULATED */
    private String caseType;

    /** Ratings of parts I-VI (NOC uses I-IV), e.g. "4.5" */
    private String r1, r2, r3, r4, r5, r6;

    /** Who scored each part and when: JSON {"1":{"by":"...","at":"2026-11-06"}, ...} (paper: Faculty Signature/date per part) */
    @Column(columnDefinition = "TEXT")
    private String stampsJson;

    private String epaLevel;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    private LocalDate evalDate;

    private LocalDateTime createdAt = LocalDateTime.now();

    @org.hibernate.annotations.UpdateTimestamp
    private LocalDateTime updatedAt;

    /** When sent to the clinician (null = draft) and by whom (NOC: "Faculty Signature/date"). */
    private LocalDateTime sentAt;
    private String sentBy;
    private LocalDateTime viewedAt;

    private static final com.fasterxml.jackson.databind.ObjectMapper JSON = new com.fasterxml.jackson.databind.ObjectMapper();

    public boolean isSent() { return sentAt != null; }

    public String[] getRatingArray() { return new String[]{r1, r2, r3, r4, r5, r6}; }

    public String getRating(int i) { return getRatingArray()[i]; }

    public void setRating(int i, String v) {
        switch (i) { case 0 -> r1 = v; case 1 -> r2 = v; case 2 -> r3 = v; case 3 -> r4 = v; case 4 -> r5 = v; default -> r6 = v; }
    }

    private static Double num(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return null; }
    }

    /** Points of part i: Forms 1-2 = rating x max / 5 (e.g. 4.5 on 35% = 31.5); NOC = rating x weight. */
    public Double getPoints(int i) {
        List<RestoEvalFormType.Part> parts = formType.getParts();
        if (i >= parts.size()) return null;
        Double r = num(getRating(i));
        if (r == null) return null;
        double max = parts.get(i).max();
        double p = formType == RestoEvalFormType.NOC ? r * max / 100.0 : r * max / 5.0;
        return Math.round(p * 100) / 100.0;
    }

    /** Total Grade: Forms 1-2 out of 100; NOC on the 3.0-5.0 scale (weighted). Null until every part is rated. */
    public Double getTotal() {
        double t = 0;
        for (int i = 0; i < formType.getParts().size(); i++) {
            Double p = getPoints(i);
            if (p == null) return null;
            t += p;
        }
        return Math.round(t * 100) / 100.0;
    }

    /** Sum of the parts rated so far (shown while grading). */
    public Double getRunningTotal() {
        double t = 0; boolean any = false;
        for (int i = 0; i < formType.getParts().size(); i++) { Double p = getPoints(i); if (p != null) { t += p; any = true; } }
        return any ? Math.round(t * 100) / 100.0 : null;
    }

    // Credit tables from the paper forms (rows: 98-100, 92-97, 86-91, 80-85, 73-79, 67-72, 61-66, 60, below 60)
    private static final Map<String, double[]> CREDIT = Map.of(
            "I/III", new double[]{1, 0.96, 0.9, 0.84, 0.78, 0.72, 0.66, 0.6, 0},
            "ONLAY", new double[]{2, 1.92, 1.8, 1.68, 1.56, 1.44, 1.32, 1.2, 0},
            "II/IV", new double[]{1.5, 1.45, 1.4, 1.3, 1.2, 1.1, 1, 0.9, 0},
            "V", new double[]{0.5, 0.48, 0.45, 0.42, 0.39, 0.36, 0.33, 0.3, 0},
            "PATIENT", new double[]{4, 3.84, 3.6, 3.36, 3.12, 2.88, 2.64, 2.4, 0},
            "SIMULATED", new double[]{2, 1.92, 1.8, 1.68, 1.56, 1.44, 1.32, 1.2, 0});
    private static final Map<String, Double> MULTIPLIER = Map.of(
            "SHALLOW_050", 0.5, "SHALLOW_075", 0.75, "MODERATE", 1.0, "EXTENSIVE", 1.25);

    private static int row(double total) {
        if (total >= 98) return 0; if (total >= 92) return 1; if (total >= 86) return 2; if (total >= 80) return 3;
        if (total >= 73) return 4; if (total >= 67) return 5; if (total >= 61) return 6; if (total >= 60) return 7;
        return 8;
    }

    /** Credit from the table (Forms 1-2), before the cavity-size multiplier. */
    public Double getCredit() {
        Double t = getTotal();
        String col = formType == RestoEvalFormType.RESTO1 ? cavityClass : formType == RestoEvalFormType.RESTO2 ? caseType : null;
        if (t == null || col == null || !CREDIT.containsKey(col)) return null;
        return CREDIT.get(col)[row(t)];
    }

    public Double getMultiplier() { return cavitySize == null ? null : MULTIPLIER.get(cavitySize); }

    /** RESTO1: credit x multiplier; RESTO2: the credit. */
    public Double getFinalCredit() {
        Double c = getCredit();
        if (c == null) return null;
        if (formType != RestoEvalFormType.RESTO1) return c;
        Double m = getMultiplier();
        return m == null ? null : Math.round(c * m * 100) / 100.0;
    }

    /** The grade shown in lists: Forms 1-2 total out of 100, NOC on the 5.0 scale. */
    public String getTotalLabel() {
        Double t = getTotal();
        if (t == null) return null;
        return t == Math.floor(t) ? String.valueOf(t.intValue()) : String.valueOf(t);
    }

    public Map<String, Map<String, String>> getStamps() {
        if (stampsJson == null || stampsJson.isBlank()) return new HashMap<>();
        try {
            return JSON.readValue(stampsJson, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Map<String, String>>>() { });
        } catch (Exception e) { return new HashMap<>(); }
    }

    public void setStamps(Map<String, Map<String, String>> m) {
        try { stampsJson = JSON.writeValueAsString(m); } catch (Exception e) { throw new IllegalStateException(e); }
    }

    /** e.g. "Santos, Maria · Nov 6, 2026" for part i, or null. */
    public String getStampLabel(int i) {
        Map<String, String> s = getStamps().get(String.valueOf(i + 1));
        if (s == null) return null;
        String at = s.get("at");
        try { at = LocalDate.parse(at).format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy")); } catch (Exception ignored) { }
        return (s.get("by") != null ? s.get("by") : "Faculty") + (at != null ? " · " + at : "");
    }

    /** e.g. "2026-00011-RESTO1", "-RESTO1-2" for the second one. */
    public String getFormNo() {
        String base = operativeChart != null && operativeChart.getChartNo() != null
                ? operativeChart.getChartNo().replace("-RESTO10", "-" + formType.getCode()) : formType.getCode();
        return visitNo != null && visitNo > 1 ? base + "-" + visitNo : base;
    }

    public List<Integer> getPartIndexes() {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < formType.getParts().size(); i++) out.add(i);
        return out;
    }
}
