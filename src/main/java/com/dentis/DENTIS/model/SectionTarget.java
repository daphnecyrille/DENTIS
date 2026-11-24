package com.dentis.DENTIS.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Target number of completed cases each clinician should reach in a section (set by that section's faculty). */
@Entity
@Table(name = "section_targets")
@Getter
@Setter
@NoArgsConstructor
public class SectionTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** OS, ENDO, PERIO, RESTO or PROSTHO */
    @Column(nullable = false, unique = true)
    private String section;

    @Column(nullable = false)
    private int target;

    @ManyToOne
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    private LocalDateTime updatedAt;
}
