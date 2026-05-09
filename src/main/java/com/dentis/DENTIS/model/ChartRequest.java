package com.dentis.DENTIS.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "chart_requests")
public class ChartRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "clinician_id")
    private User clinician;
    private LocalDate requestDate;
    private String purpose;
    private String chartNo;
    private String patientName;
    private String section;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Enumerated(EnumType.STRING)
    private ChartRequestStatus status;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = ChartRequestStatus.PENDING;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getClinician() { return clinician; }
    public void setClinician(User clinician) { this.clinician = clinician; }

    public LocalDate getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDate requestDate) { this.requestDate = requestDate; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getChartNo() { return chartNo; }
    public void setChartNo(String chartNo) { this.chartNo = chartNo; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public ChartRequestStatus getStatus() { return status; }
    public void setStatus(ChartRequestStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
