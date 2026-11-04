package com.dentis.DENTIS.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** One important action in the system (approvals, signatures, submissions, assignments, account changes). Read-only history. */
@Entity
@Table(name = "activity_log", indexes = @Index(columnList = "at"))
@Getter
@Setter
@NoArgsConstructor
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime at = LocalDateTime.now();

    private Long actorId;
    private String actorName;
    private String actorRole;

    /** Short category for filtering: APPROVE, REVISE, SUBMIT, ASSIGN, REQUEST, ACCOUNT, PATIENT, EVALUATION */
    private String category;

    /** What happened, e.g. "Approved PF1 (signed)" */
    private String action;

    /** Which record, e.g. "2026-00003-PF1 · Dela Cruz, Juan A." */
    @Column(columnDefinition = "TEXT")
    private String target;
}
