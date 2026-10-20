package com.dentis.DENTIS.service;

import com.dentis.DENTIS.model.PerioEvalFormType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Layout and scoring of each periodontics evaluation form (PF4, PF5, PF6, PF7, PF17), as printed on the 2026 forms.
 * Left table: one grade per criterion, Score = grade x weight, subtotal x leftMultiplier.
 * Right table (PF4–PF6): up to 3 attempts per criterion, averaged, x weight, subtotal x rightMultiplier.
 * Total = left + right (out of 100).
 */
public class PerioEvalForm {

    private final String code;
    private final String courses;          // e.g. "171.1 & 171.2"; empty if none printed
    private final String title;
    private final List<Map<String, Object>> left;
    private final List<Map<String, Object>> right;
    private final double leftMultiplier;
    private final String leftMultiplierLabel;
    private final double rightMultiplier;
    private final String rightMultiplierLabel;
    /** PF6 only: "If RP is NA, the multiplier is 0.615" (root planing criterion left out). */
    private final String rootPlaningField;
    private final double rightMultiplierNoRp;

    private PerioEvalForm(String code, String courses, String title,
                          List<Map<String, Object>> left, double leftMultiplier, String leftMultiplierLabel,
                          List<Map<String, Object>> right, double rightMultiplier, String rightMultiplierLabel,
                          String rootPlaningField, double rightMultiplierNoRp) {
        this.code = code; this.courses = courses; this.title = title;
        this.left = left; this.leftMultiplier = leftMultiplier; this.leftMultiplierLabel = leftMultiplierLabel;
        this.right = right; this.rightMultiplier = rightMultiplier; this.rightMultiplierLabel = rightMultiplierLabel;
        this.rootPlaningField = rootPlaningField; this.rightMultiplierNoRp = rightMultiplierNoRp;
    }

    public String getCode() { return code; }
    public String getCourses() { return courses; }
    public String getTitle() { return title; }
    public List<Map<String, Object>> getLeft() { return left; }
    public List<Map<String, Object>> getRight() { return right; }
    public boolean isHasRight() { return !right.isEmpty(); }
    public double getLeftMultiplier() { return leftMultiplier; }
    public String getLeftMultiplierLabel() { return leftMultiplierLabel; }
    public double getRightMultiplier() { return rightMultiplier; }
    public String getRightMultiplierLabel() { return rightMultiplierLabel; }
    public String getRootPlaningField() { return rootPlaningField; }
    public boolean isHasRootPlaning() { return rootPlaningField != null; }
    public double getRightMultiplierNoRp() { return rightMultiplierNoRp; }

    // ── Criteria shared by the forms ────────────────────────────────────────

    private static Map<String, Object> c(String field, String label, int weight) {
        return Map.of("field", field, "label", label, "weight", weight);
    }

    private static final Map<String, Object> HISTORY      = c("determinedHistory", "Determined the history of chief complaint", 1);
    private static final Map<String, Object> VISUAL       = c("assessedPerioVisually", "Assessed the periodontal condition visually", 2);
    private static final Map<String, Object> ORAL_HYGIENE = c("assessedOralHygiene", "Assessed the patient’s oral hygiene status", 2);
    private static final Map<String, Object> PLAN         = c("formulatedTreatmentPlan", "Formulated an individualized holistic treatment plan", 3);
    private static final String DIAGNOSED_LABEL = "Diagnosed and classified the existing periodontal condition";
    private static final String CLINICAL_LABEL  = "Applied the clinical parameters";
    private static final String CRITICAL_LABEL  = "Demonstrated skills in critical thinking";
    private static final String HEALTH_ED_LABEL = "Performed responsive health education through the employment of various aids";
    private static final String INSTRUMENT_LABEL = "Employed the principles of instrumentation during debridement while maintaining correct operating position";
    private static final String DEBRIDE_LABEL   = "Performed debridement while maintaining integrity of soft tissues";
    private static final Map<String, Object> INFECTION = c("infectionControlRight", "Employed the principles of infection control", 1);
    private static final Map<String, Object> AWARENESS = c("awareness", "Maintained awareness and sensitivity to patient’s needs", 2);

    /** PF4 and PF5 have the same criteria; only the multipliers differ. */
    private static final List<Map<String, Object>> SCALING_LEFT = List.of(
        HISTORY, VISUAL, ORAL_HYGIENE,
        c("appliedClinicalParameters", CLINICAL_LABEL, 4), c("diagnosedClassified", DIAGNOSED_LABEL, 4), PLAN);
    private static final List<Map<String, Object>> SCALING_RIGHT = List.of(
        c("healthEd", HEALTH_ED_LABEL, 2), c("instrumentation", INSTRUMENT_LABEL, 4),
        c("debridementTissues", DEBRIDE_LABEL, 3), INFECTION, AWARENESS);

    /** PF7 and PF17 have the same criteria. */
    private static final List<Map<String, Object>> REEVAL_LEFT = List.of(
        c("appliedClinicalParameters", CLINICAL_LABEL, 3), c("diagnosedClassified", DIAGNOSED_LABEL, 4),
        c("determinedFurtherTreatmentNeeds", "Determined the patient’s further treatment needs", 2),
        c("performedOralHealthEducation", "Performed responsive oral health education", 1),
        c("performedNecessaryTreatment", "Performed necessary treatment based on the assessed needs of the patient", 2),
        c("demonstratedCriticalThinking", CRITICAL_LABEL, 3));

    private static final Map<PerioEvalFormType, PerioEvalForm> FORMS = new EnumMap<>(PerioEvalFormType.class);
    static {
        // PF4: left max 80 x0.625 = 50, right max 60 x0.833 = 50 (5/6 so a perfect score is exactly 50)
        FORMS.put(PerioEvalFormType.PF4, new PerioEvalForm("PF4", "171.1 & 171.2",
            "Evaluation Form for Scaling on Human Patients",
            SCALING_LEFT, 0.625, "x0.625", SCALING_RIGHT, 5.0 / 6.0, "x0.833", null, 0));
        // PF5: left max 80 x0.5 = 40, right max 60 x1 = 60 (no multiplier printed under the right table)
        FORMS.put(PerioEvalFormType.PF5, new PerioEvalForm("PF5", "172.1 & 172.2",
            "Evaluation Form for Scaling on Human Patients",
            SCALING_LEFT, 0.5, "x0.5", SCALING_RIGHT, 1.0, "", null, 0));
        // PF6: left max 100 x0.6 = 60, right max 80 x0.5 = 40; without root planing max 65 x0.615 = 40
        FORMS.put(PerioEvalFormType.PF6, new PerioEvalForm("PF6", "",
            "Evaluation Form for Scaling and Root Planing / Debridement",
            List.of(HISTORY, VISUAL, ORAL_HYGIENE,
                    c("appliedClinicalParameters", CLINICAL_LABEL, 4), c("diagnosedClassified", DIAGNOSED_LABEL, 4), PLAN,
                    c("demonstratedCriticalThinking", CRITICAL_LABEL, 4)),
            0.6, "x0.6",
            List.of(c("healthEd", HEALTH_ED_LABEL, 2), c("instrumentation", INSTRUMENT_LABEL, 4),
                    c("debridementTissues", DEBRIDE_LABEL, 4),
                    c("rootPlaning", "Planed the root surface while maintaining integrity of the soft tissues", 3),
                    INFECTION, AWARENESS),
            0.5, "x0.5", "rootPlaning", 40.0 / 65.0));
        // PF7 / PF17: one table, max 75 x1.333 = 100 (4/3 so a perfect score is exactly 100)
        FORMS.put(PerioEvalFormType.PF7, new PerioEvalForm("PF7", "",
            "Evaluation Form for Periodontal Reevaluation / Maintenance Visits",
            REEVAL_LEFT, 4.0 / 3.0, "x1.333", List.of(), 0, "", null, 0));
        FORMS.put(PerioEvalFormType.PF17, new PerioEvalForm("PF17", "",
            "Evaluation Form for Reevaluation of Gingivitis Patients",
            REEVAL_LEFT, 4.0 / 3.0, "x1.333", List.of(), 0, "", null, 0));
    }

    /** The form spec, or null for the old forms that are no longer offered. */
    public static PerioEvalForm of(PerioEvalFormType type) {
        return FORMS.get(type);
    }

    public static final List<String> ATTEMPTS = List.of("1st", "2nd", "3rd");
    /** Key in the grades JSON marking root planing as not applicable (PF6). */
    public static final String RP_NA = "rootPlaningNA";

    /** Left + right subtotal, rounded to 2 decimals. */
    public double total(Map<String, Double> g) {
        double l = 0, r = 0;
        for (Map<String, Object> c : left) l += g.getOrDefault((String) c.get("field"), 0.0) * (Integer) c.get("weight");
        boolean noRp = isHasRootPlaning() && g.containsKey(RP_NA);
        for (Map<String, Object> c : right) {
            String f = (String) c.get("field");
            if (noRp && f.equals(rootPlaningField)) continue;
            double sum = 0; int n = 0;
            for (String a : ATTEMPTS) { Double v = g.get(f + a); if (v != null) { sum += v; n++; } }
            if (n > 0) r += sum / n * (Integer) c.get("weight");
        }
        double t = l * leftMultiplier + r * (noRp ? rightMultiplierNoRp : rightMultiplier);
        return Math.round(t * 100.0) / 100.0;
    }
}
