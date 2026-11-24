package com.dentis.DENTIS.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** A technical or system problem reported by a user, handled by the system administrator. */
@Entity
@Table(name = "issue_reports")
@Getter
@Setter
@NoArgsConstructor
public class IssueReport {

    public enum Status {
        OPEN, IN_PROGRESS, RESOLVED;
        public String getLabel() {
            return switch (this) { case OPEN -> "Open"; case IN_PROGRESS -> "In Progress"; case RESOLVED -> "Resolved"; };
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @Column(nullable = false, length = 150)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** The page the user was on when they reported it. */
    @Column(length = 300)
    private String page;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.OPEN;

    /** The admin's reply, shown to the reporter. */
    @Column(columnDefinition = "TEXT")
    private String adminNote;

    @ManyToOne
    @JoinColumn(name = "handled_by")
    private User handledBy;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
}
