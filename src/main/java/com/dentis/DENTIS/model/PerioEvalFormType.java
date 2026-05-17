package com.dentis.DENTIS.model;

public enum PerioEvalFormType {
    PF4("Scaling on Human Patients (32 Teeth)"),
    PF5("Scaling on Human Patients (40 Teeth)"),
    PF6("Scaling and Root Planing / Debridement"),
    PF7("Periodontal Reevaluation / Maintenance Visits"),
    PF9("Scaling on Periodontal Models"),
    PF10("Root Debridement on Periodontal Models"),
    PF17("Reevaluation of Gingivitis Patients (1st)"),
    PF18("Reevaluation of Gingivitis Patients (2nd)");

    private final String displayName;

    PerioEvalFormType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
