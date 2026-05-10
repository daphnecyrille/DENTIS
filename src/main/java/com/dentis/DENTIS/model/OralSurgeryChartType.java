package com.dentis.DENTIS.model;

public enum OralSurgeryChartType {
    OSF1, OSF2;

    public String getDisplayName() {
        return switch (this) {
            case OSF1 -> "OS Chart — Patient Workup (OSF1)";
            case OSF2 -> "OS Chart — Odontectomy (OSF2)";
        };
    }
}
