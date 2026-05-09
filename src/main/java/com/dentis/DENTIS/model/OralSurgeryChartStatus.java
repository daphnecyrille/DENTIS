package com.dentis.DENTIS.model;

public enum OralSurgeryChartStatus {
    CREATED,
    IN_PROGRESS,
    SUBMITTED;

    public String getDisplayName() {
        return switch (this) {
            case CREATED -> "Pending";
            case IN_PROGRESS -> "In Progress";
            case SUBMITTED -> "Submitted";
        };
    }

    public String getCssClass() {
        return switch (this) {
            case CREATED -> "pending";
            case IN_PROGRESS -> "in-progress";
            case SUBMITTED -> "submitted";
        };
    }
}
