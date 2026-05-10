package com.dentis.DENTIS.model;

public enum OralSurgeryChartStatus {
    CREATED,
    IN_PROGRESS,
    SUBMITTED,
    APPROVED,
    REVISE;

    public String getDisplayName() {
        return switch (this) {
            case CREATED -> "In Progress";
            case IN_PROGRESS -> "In Progress";
            case SUBMITTED -> "Awaiting Approval";
            case APPROVED -> "Approved";
            case REVISE -> "For Revision";
        };
    }

    public String getCssClass() {
        return switch (this) {
            case CREATED -> "created";
            case IN_PROGRESS -> "in-progress";
            case SUBMITTED -> "pending";
            case APPROVED -> "approved";
            case REVISE -> "revise";
        };
    }
}
