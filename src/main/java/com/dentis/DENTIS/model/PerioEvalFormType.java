package com.dentis.DENTIS.model;

public enum PerioEvalFormType {
    PF4("Scaling on Human Patients (171.1 & 171.2)"),
    PF5("Scaling on Human Patients (172.1 & 172.2)"),
    PF6("Scaling and Root Planing / Debridement"),
    PF7("Periodontal Reevaluation / Maintenance Visit"),
    PF9("Scaling on Periodontal Models"),            // old form, no longer offered
    PF10("Root Debridement on Periodontal Models"),  // old form, no longer offered
    PF17("Reevaluation of Gingivitis Patients"),     // printed as PF17 on the 2026 form (file name says PF18)
    PF18("Reevaluation of Gingivitis Patients (old)"); // old form, no longer offered

    private final String displayName;

    PerioEvalFormType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Clinic section the form belongs to (used by the Evaluations page filters). */
    public String getSection() {
        return "Perio";
    }

    /** The 2026 forms the faculty can add. */
    public boolean isOffered() {
        return this == PF4 || this == PF5 || this == PF6 || this == PF7 || this == PF17;
    }

    /** Forms usually done once per visit / recall (labelled "Visit n"). Any form can be added more than once. */
    public boolean isRepeatable() {
        return this == PF7 || this == PF17;
    }

    /** Forms whose page has been built. */
    public boolean hasPage() {
        return isOffered();
    }
}
