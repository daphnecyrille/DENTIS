package com.dentis.DENTIS.model;

public enum ProsthodonticsChartStatus {
    CREATED, IN_PROGRESS, SUBMITTED, APPROVED, REVISE;

    public String getDisplayName() {
        return switch (this) {
            case CREATED -> "In Progress";
            case IN_PROGRESS -> "In Progress";
            case SUBMITTED -> "Submitted";
            case APPROVED -> "Approved";
            case REVISE -> "Revise";
        };
    }

    public String getCssClass() {
        return switch (this) {
            case CREATED, IN_PROGRESS -> "in-progress";
            case SUBMITTED -> "submitted";
            case APPROVED -> "approved";
            case REVISE -> "revise";
        };
    }
}
