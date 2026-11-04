package com.dentis.DENTIS.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "prosthodontics_charts")
@Getter
@Setter
@NoArgsConstructor
public class ProsthodonticsChart {

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
    private ProsthodonticsChartStatus status = ProsthodonticsChartStatus.CREATED;

    // Case type: CD or RPD (from patient service code)
    private String caseType;

    // Case number (1, 2, or 3)
    private Integer caseNumber;

    // RPD sub-options
    private Boolean rpdMxWithFramework = false;
    private Boolean rpdMxWithout = false;
    private Boolean rpdMdWithFramework = false;
    private Boolean rpdMdWithout = false;
    private Boolean rpdMx = false;
    private Boolean rpdMd = false;

    // SD (single denture) sub-options
    private Boolean sdMx = false;
    private Boolean sdMd = false;

    // Cover info
    private String startedDate;
    private String finishedDate;
    private String grade;
    private String clearedBy;
    private String clearedDate;

    /** Drawn signature of the faculty who cleared the case (set when they clear it). */
    @Column(columnDefinition = "TEXT")
    private String clearedSignature;
    private LocalDateTime clearedAt;
    /** When the clinician first opened the cleared case (null = shown as a new result). */
    private LocalDateTime clearedViewedAt;

    // 1. Patient Consent
    private String patientConsentDate;
    private String patientConsentFilePath;

    // 2. Patient Workup
    private String patientWorkupDate;
    private String patientWorkupFilePath;

    // 3. Primary Impression and Diagnostic Cast
    private String primaryImpressionDate;
    private String primaryImpressionFilePath;

    // 4. Individual Tray
    private Boolean individualTrayNotApplicable = false;
    private String individualTrayDate;
    private String individualTrayFilePath;

    // 5. Mouth Preparations
    private Boolean mouthPreparationsNotApplicable = false;
    private String mouthPreparationsDate;
    @Column(columnDefinition = "TEXT")
    private String mouthPreparationsList;

    // 6. Final Impression and Master Cast
    private String finalImpressionDate;
    private String finalImpressionFilePath;

    // 7. Inspection of Wax Pattern from Laboratory
    private Boolean waxPatternLabNotApplicable = false;
    private String waxPatternLabDate;
    private String waxPatternLabFilePath;

    // 8. Wax Pattern on Diagnostic Cast
    private Boolean waxPatternDiagNotApplicable = false;
    private String waxPatternDiagDate;
    private String waxPatternDiagFilePath;

    // 9. Inspection and Fitting of Metal Framework
    private Boolean metalFrameworkNotApplicable = false;
    private String metalFrameworkDate;
    private String metalFrameworkFilePath;

    // 10. Trial Denture Base
    private Boolean trialDentureBaseNotApplicable = false;
    private String trialDentureBaseDate;
    private String trialDentureBaseFilePath;

    // 11. Development of Maxillary and Mandibular OCR
    private String ocrDate;
    private String ocrFilePath;

    // 12. Positional Relations
    private String positionalRelationsDate;
    private String positionalRelationsFilePath;

    // 13. Mounting
    private String mountingDate;
    private String mountingFilePath;

    // 14. Anterior Setup
    private Boolean anteriorSetupNotApplicable = false;
    private String anteriorSetupDate;
    private String anteriorSetupMould;
    private String anteriorSetupShade;
    private String anteriorSetupFilePath;

    // 15. Anterior Try-in
    private Boolean anteriorTryInNotApplicable = false;
    private String anteriorTryInDate;
    private String anteriorTryInFilePath;
    private String anteriorTryInSignaturePath;

    // 16. Protrusive Records Taking
    private Boolean protrusiveRecordsNotApplicable = false;
    private String protrusiveRecordsDate;
    private String hcaRight;
    private String hcaLeft;
    private String lcaRight;
    private String lcaLeft;

    // 17. Posterior Setup
    private String posteriorSetupDate;
    private String posteriorSetupMould;
    private String posteriorSetupShade;
    private String posteriorSetupFilePath;

    // 18. Posterior Try-in
    private String posteriorTryInDate;
    private String posteriorTryInFilePath;
    private String posteriorTryInSignaturePath;

    // 19. Wax-up and Festooning
    private String waxUpDate;
    private String waxUpFilePath;

    // 20. Processing
    private String processingDate;
    private String processingFilePath;

    // 21. CEF, Trimming and Polishing
    private String cefDate;
    private String cefFilePath;

    // 22. Installation
    private String installationDate;
    private String installationFilePath;
    private String installationSignaturePath;

    // 23. Post-installation Recall
    private String postInstallationDate;
    private String postInstallationFilePath;
    private String postInstallationSignaturePath;

    // 24. Remarks
    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(columnDefinition = "TEXT")
    private String revisionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Booklet view: one entry per procedure row ─────────────────────────────

    /** One procedure row of the booklet (date, uploaded page/photo, N/A, extra details, patient signature). */
    public static class Step {
        private final String name, date, file, extra, patientSignature;
        private final boolean notApplicable, needsFile;

        Step(String name, String date, String file, Boolean notApplicable, String extra, String patientSignature, boolean needsFile) {
            this.name = name; this.date = blank(date) ? null : date; this.file = blank(file) ? null : file;
            this.notApplicable = Boolean.TRUE.equals(notApplicable);
            this.extra = blank(extra) ? null : extra; this.patientSignature = blank(patientSignature) ? null : patientSignature;
            this.needsFile = needsFile;
        }
        public String getName() { return name; }
        public String getDate() { return date; }
        public String getFile() { return file; }
        public String getExtra() { return extra; }
        public String getPatientSignature() { return patientSignature; }
        public boolean isNotApplicable() { return notApplicable; }
        public boolean isNeedsFile() { return needsFile; }
        public boolean isPdf() { return file != null && file.toLowerCase().endsWith(".pdf"); }
        public boolean isDone() { return !notApplicable && date != null && (file != null || !needsFile); }
        /** e.g. "no date, no photo"; empty when the step is done or not applicable */
        public String getMissing() {
            if (notApplicable || isDone()) return "";
            java.util.List<String> m = new java.util.ArrayList<>();
            if (date == null) m.add("no date");
            if (needsFile && file == null) m.add("no photo");
            return String.join(", ", m);
        }
    }

    private static boolean blank(String s) { return s == null || s.isBlank(); }
    private static String join(String sep, String... parts) {
        java.util.List<String> out = new java.util.ArrayList<>();
        for (String p : parts) if (!blank(p)) out.add(p);
        return String.join(sep, out);
    }

    public java.util.List<Step> getSteps() {
        java.util.List<Step> s = new java.util.ArrayList<>();
        s.add(new Step("Patient Consent", patientConsentDate, patientConsentFilePath, false, null, null, true));
        s.add(new Step("Patient Workup", patientWorkupDate, patientWorkupFilePath, false, null, null, true));
        s.add(new Step("Primary Impression and Diagnostic Cast", primaryImpressionDate, primaryImpressionFilePath, false, null, null, true));
        s.add(new Step("Individual Tray", individualTrayDate, individualTrayFilePath, individualTrayNotApplicable, null, null, true));
        s.add(new Step("Mouth Preparations", mouthPreparationsDate, null, mouthPreparationsNotApplicable, mouthPreparationsList, null, false));
        s.add(new Step("Final Impression and Master Cast", finalImpressionDate, finalImpressionFilePath, false, null, null, true));
        s.add(new Step("Inspection of Wax Pattern from Laboratory", waxPatternLabDate, waxPatternLabFilePath, waxPatternLabNotApplicable, null, null, true));
        s.add(new Step("Wax Pattern on Diagnostic Cast", waxPatternDiagDate, waxPatternDiagFilePath, waxPatternDiagNotApplicable, null, null, true));
        s.add(new Step("Inspection and Fitting of Metal Framework", metalFrameworkDate, metalFrameworkFilePath, metalFrameworkNotApplicable, null, null, true));
        s.add(new Step("Trial Denture Base", trialDentureBaseDate, trialDentureBaseFilePath, trialDentureBaseNotApplicable, null, null, true));
        s.add(new Step("Development of Maxillary and Mandibular OCR", ocrDate, ocrFilePath, false, null, null, true));
        s.add(new Step("Positional Relations", positionalRelationsDate, positionalRelationsFilePath, false, null, null, true));
        s.add(new Step("Mounting", mountingDate, mountingFilePath, false, null, null, true));
        s.add(new Step("Anterior Setup", anteriorSetupDate, anteriorSetupFilePath, anteriorSetupNotApplicable,
                join(" · ", blank(anteriorSetupMould) ? null : "Mould: " + anteriorSetupMould, blank(anteriorSetupShade) ? null : "Shade: " + anteriorSetupShade), null, true));
        s.add(new Step("Anterior Try-in", anteriorTryInDate, anteriorTryInFilePath, anteriorTryInNotApplicable, null, anteriorTryInSignaturePath, true));
        s.add(new Step("Protrusive Records Taking", protrusiveRecordsDate, null, protrusiveRecordsNotApplicable,
                join(" · ", blank(hcaRight) && blank(hcaLeft) ? null : "HCA R " + nz(hcaRight) + " L " + nz(hcaLeft),
                            blank(lcaRight) && blank(lcaLeft) ? null : "LCA R " + nz(lcaRight) + " L " + nz(lcaLeft)), null, false));
        s.add(new Step("Posterior Setup", posteriorSetupDate, posteriorSetupFilePath, false,
                join(" · ", blank(posteriorSetupMould) ? null : "Mould: " + posteriorSetupMould, blank(posteriorSetupShade) ? null : "Shade: " + posteriorSetupShade), null, true));
        s.add(new Step("Posterior Try-in", posteriorTryInDate, posteriorTryInFilePath, false, null, posteriorTryInSignaturePath, true));
        s.add(new Step("Wax-up and Festooning", waxUpDate, waxUpFilePath, false, null, null, true));
        s.add(new Step("Processing", processingDate, processingFilePath, false, null, null, true));
        s.add(new Step("CEF, Trimming and Polishing", cefDate, cefFilePath, false, null, null, true));
        s.add(new Step("Installation", installationDate, installationFilePath, false, null, null, true));
        s.add(new Step("Post-installation Recall", postInstallationDate, postInstallationFilePath, false, null, null, true));
        return s;
    }
    private static String nz(String s) { return blank(s) ? "—" : s; }

    /** No. of Days on the cover: Started to Finished (null until both dates are filled in). */
    public Long getNumberOfDays() {
        try {
            if (blank(startedDate) || blank(finishedDate)) return null;
            long d = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.parse(startedDate), java.time.LocalDate.parse(finishedDate));
            return d >= 0 ? d : null;
        } catch (Exception e) { return null; }
    }

    public long getStepsDone() { return getSteps().stream().filter(Step::isDone).count(); }
    public long getStepsNotApplicable() { return getSteps().stream().filter(Step::isNotApplicable).count(); }
    public long getStepsApplicable() { return getSteps().size() - getStepsNotApplicable(); }
    /** "Step: no date, no photo" for each step still incomplete (used for the reminder before submitting). */
    public java.util.List<String> getMissingSteps() {
        return getSteps().stream().filter(st -> !st.getMissing().isEmpty())
                .map(st -> st.getName() + ": " + st.getMissing()).toList();
    }

    /** Set automatically every time the chart is saved (Patient List "Last Updated"). */
    @org.hibernate.annotations.UpdateTimestamp
    private LocalDateTime updatedAt;
}
