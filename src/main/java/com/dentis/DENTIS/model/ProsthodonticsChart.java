package com.dentis.DENTIS.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "prosthodontics_charts")
@Getter
@Setter
@NoArgsConstructor
public class ProsthodonticsChart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chart_no", unique = true)
    private String chartNo;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "clinician_id")
    private User clinician;

    @ManyToOne
    @JoinColumn(name = "faculty_id")
    private User faculty;

    @Enumerated(EnumType.STRING)
    private ProsthodonticsChartStatus status = ProsthodonticsChartStatus.CREATED;

    // Case type: CD or RPD (from patient service code)
    private String caseType;

    // Case number (1, 2, or 3)
    private Integer caseNumber;

    // RPD sub-options
    private Boolean rpdMxWithFramework = false;
    private Boolean rpdMxWithout = false;
    private Boolean rpdMdWithFramework = false;
    private Boolean rpdMdWithout = false;

    // Cover info
    private String startedDate;
    private String finishedDate;
    private String grade;
    private String clearedBy;
    private String clearedDate;

    // 1. Patient Consent
    private String patientConsentFilePath;

    // 2. Patient Workup
    private String patientWorkupDate;
    private String patientWorkupFilePath;

    // 3. Primary Impression and Diagnostic Cast
    private String primaryImpressionDate;
    private String primaryImpressionFilePath;

    // 4. Individual Tray
    private Boolean individualTrayNotApplicable = false;
    private String individualTrayDate;
    private String individualTrayFilePath;

    // 5. Mouth Preparations
    private Boolean mouthPreparationsNotApplicable = false;
    private String mouthPreparationsDate;
    @Column(columnDefinition = "TEXT")
    private String mouthPreparationsList;

    // 6. Final Impression and Master Cast
    private String finalImpressionDate;
    private String finalImpressionFilePath;

    // 7. Inspection of Wax Pattern from Laboratory
    private Boolean waxPatternLabNotApplicable = false;
    private String waxPatternLabDate;
    private String waxPatternLabFilePath;

    // 8. Wax Pattern on Diagnostic Cast
    private Boolean waxPatternDiagNotApplicable = false;
    private String waxPatternDiagDate;
    private String waxPatternDiagFilePath;

    // 9. Inspection and Fitting of Metal Framework
    private Boolean metalFrameworkNotApplicable = false;
    private String metalFrameworkDate;
    private String metalFrameworkFilePath;

    // 10. Trial Denture Base
    private Boolean trialDentureBaseNotApplicable = false;
    private String trialDentureBaseDate;
    private String trialDentureBaseFilePath;

    // 11. Development of Maxillary and Mandibular OCR
    private String ocrDate;
    private String ocrFilePath;

    // 12. Positional Relations
    private String positionalRelationsDate;
    private String positionalRelationsFilePath;

    // 13. Mounting
    private String mountingDate;
    private String mountingFilePath;

    // 14. Anterior Setup
    private Boolean anteriorSetupNotApplicable = false;
    private String anteriorSetupDate;
    private String anteriorSetupMould;
    private String anteriorSetupShade;
    private String anteriorSetupFilePath;

    // 15. Anterior Try-in
    private Boolean anteriorTryInNotApplicable = false;
    private String anteriorTryInDate;
    private String anteriorTryInFilePath;
    private String anteriorTryInSignaturePath;

    // 16. Protrusive Records Taking
    private Boolean protrusiveRecordsNotApplicable = false;
    private String protrusiveRecordsDate;
    private String hcaRight;
    private String hcaLeft;
    private String lcaRight;
    private String lcaLeft;

    // 17. Posterior Setup
    private String posteriorSetupDate;
    private String posteriorSetupMould;
    private String posteriorSetupShade;
    private String posteriorSetupFilePath;

    // 18. Posterior Try-in
    private String posteriorTryInDate;
    private String posteriorTryInFilePath;
    private String posteriorTryInSignaturePath;

    // 19. Wax-up and Festooning
    private String waxUpDate;
    private String waxUpFilePath;

    // 20. Processing
    private String processingDate;
    private String processingFilePath;

    // 21. CEF, Trimming and Polishing
    private String cefDate;
    private String cefFilePath;

    // 22. Installation
    private String installationDate;
    private String installationFilePath;
    private String installationSignaturePath;

    // 23. Post-installation Recall
    private String postInstallationDate;
    private String postInstallationFilePath;
    private String postInstallationSignaturePath;

    // 24. Remarks
    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(columnDefinition = "TEXT")
    private String revisionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
