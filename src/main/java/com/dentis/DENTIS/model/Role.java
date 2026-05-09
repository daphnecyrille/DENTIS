package com.dentis.DENTIS.model;

public enum Role {
    CLINICIAN,
    FACULTY,
    CLINIC_MANAGER,
    ADMIN;

    public String getDisplayName() {
        return switch (this) {
            case CLINICIAN -> "Clinician";
            case FACULTY -> "Faculty";
            case CLINIC_MANAGER -> "Clinic Manager";
            case ADMIN -> "Admin";
        };
    }
}
