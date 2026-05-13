package com.dentis.DENTIS.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clinician_faculty_assignments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"clinician_id", "section"}))
@Getter
@Setter
@NoArgsConstructor
public class ClinicianFacultyAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "clinician_id", nullable = false)
    private User clinician;

    @ManyToOne
    @JoinColumn(name = "faculty_id")
    private User faculty;

    @Column(nullable = false)
    private String section;
}
