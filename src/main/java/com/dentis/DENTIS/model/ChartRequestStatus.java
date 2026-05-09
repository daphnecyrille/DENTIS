package com.dentis.DENTIS.model;

public enum ChartRequestStatus {
    PENDING, APPROVED, DENIED;

    public String getCssClass() {
        return switch (this) {
            case PENDING -> "pending";
            case APPROVED -> "approved";
            case DENIED -> "rejected";
        };
    }
}
