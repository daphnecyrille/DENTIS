package com.dentis.DENTIS.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chart_no", nullable = false, unique = true)
    private String chartNo;

    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;

    @Column(nullable = false)
    private String surname;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "middle_initial")
    private String middleInitial;

    private Integer age;

    private LocalDate birthdate;

    private String sex;

    private String address;

    @Column(name = "contact_number")
    private String contactNumber;

    private String email;

    @Column(name = "civil_status")
    private String civilStatus;

    private String occupation;

    @Column(name = "educational_attainment")
    private String educationalAttainment;

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_relation")
    private String emergencyContactRelation;

    @Column(name = "emergency_contact_number")
    private String emergencyContactNumber;

    @Column(name = "chief_complaint", columnDefinition = "TEXT")
    private String chiefComplaint;

    @Column(name = "history_of_present_illness", columnDefinition = "TEXT")
    private String historyOfPresentIllness;

    @Column(name = "service_code")
    private String serviceCode;

    @Column(name = "proposed_treatment_plan", columnDefinition = "TEXT")
    private String proposedTreatmentPlan;

    @ManyToOne
    @JoinColumn(name = "assigned_faculty_id")
    private User assignedFaculty;

    @ManyToOne
    @JoinColumn(name = "assigned_clinician_id")
    private User assignedClinician;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
