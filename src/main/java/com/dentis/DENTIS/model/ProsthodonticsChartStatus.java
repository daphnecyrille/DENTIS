package com.dentis.DENTIS.model;

public enum ProsthodonticsChartStatus {
    CREATED, IN_PROGRESS, SUBMITTED, APPROVED, REVISE;

    public String getDisplayName() {
        return switch (this) {
            case CREATED -> "Not Started";   // never saved yet (saving moves it to IN_PROGRESS)
            case IN_PROGRESS -> "In Progress";
            case SUBMITTED -> "Submitted";
            case APPROVED -> "Approved";
            case REVISE -> "Revise";
        };
    }

    public String getCssClass() {
        return switch (this) {
            case CREATED -> "created";
            case IN_PROGRESS -> "in-progress";
            case SUBMITTED -> "submitted";
            case APPROVED -> "approved";
            case REVISE -> "revise";
        };
    }
}
