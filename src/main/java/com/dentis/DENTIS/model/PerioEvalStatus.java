package com.dentis.DENTIS.model;

/**
 * DRAFT  – only the faculty can see it.
 * GRADED – sent to the clinician (read-only for them). The name is kept as GRADED
 *          so values already stored in the database stay valid.
 */
public enum PerioEvalStatus {
    DRAFT, GRADED;

    public boolean isSent() { return this == GRADED; }
}
