package com.dentis.DENTIS.service;

import com.dentis.DENTIS.model.*;
import com.dentis.DENTIS.repository.ProsthodonticsChartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ProsthodonticsChartService {

    private final ProsthodonticsChartRepository repo;

    // Absolute path so it resolves correctly regardless of JVM working directory.
    // Files are stored outside the classpath static folder and served via a
    // custom resource handler configured in WebMvcConfig.
    private static final String UPLOAD_DIR =
            System.getProperty("user.dir") + "/src/main/resources/static/uploads/prosthodontics/";

    public ProsthodonticsChartService(ProsthodonticsChartRepository repo) {
        this.repo = repo;
    }

    public ProsthodonticsChart createForPatient(Patient patient, User clinician, User faculty) {
        ProsthodonticsChart chart = new ProsthodonticsChart();
        chart.setPatient(patient);
        chart.setClinician(clinician);
        chart.setFaculty(faculty);
        chart.setCaseType(patient.getServiceCode() != null ? patient.getServiceCode().toUpperCase() : "CD");
        chart.setChartNo(patient.getChartNo() + "-PROSTHO");
        return repo.save(chart);
    }

    public ProsthodonticsChart getById(Long id) {
        return repo.findById(id).orElseThrow();
    }

    public Optional<ProsthodonticsChart> findByPatient(Patient patient) {
        return repo.findTopByPatientOrderByCreatedAtDesc(patient);
    }

    public List<ProsthodonticsChart> findAllByPatient(Patient patient) {
        return repo.findByPatientOrderByCreatedAtDesc(patient);
    }

    /**
     * The patient was reassigned to another faculty: their charts in this section go to that faculty too.
     * Charts the former faculty already fully approved stay with them, as the record of who approved them.
     */
    public void reassignFaculty(Patient patient, User faculty) {
        List<ProsthodonticsChart> charts = findAllByPatient(patient).stream()
                .filter(c -> !(c.getStatus() == ProsthodonticsChartStatus.APPROVED))
                .toList();
        charts.forEach(c -> c.setFaculty(faculty));
        repo.saveAll(charts);
    }

    /** True once the clinician has saved any form of this patient's charts. */
    public boolean hasStartedForms(Patient patient) {
        return findAllByPatient(patient).stream().anyMatch(c -> c.getStatus() != ProsthodonticsChartStatus.CREATED);
    }

    /** Moves the patient's (not yet started) charts to a newly assigned clinician. */
    public void reassignClinician(Patient patient, User clinician) {
        List<ProsthodonticsChart> charts = findAllByPatient(patient);
        charts.forEach(c -> c.setClinician(clinician));
        repo.saveAll(charts);
    }

    public List<ProsthodonticsChart> findByClinicianOrderByCreatedAtDesc(User clinician) {
        return repo.findByClinicianOrderByCreatedAtDesc(clinician);
    }

    public List<ProsthodonticsChart> findByFacultyOrderByCreatedAtDesc(User faculty) {
        return repo.findByFacultyOrderByCreatedAtDesc(faculty);
    }

    /** Every chart of this section (all faculty of the section can see all its patients). */
    public List<ProsthodonticsChart> findAllOrderByCreatedAtDesc() {
        return repo.findAllByOrderByCreatedAtDesc();
    }

    public List<ProsthodonticsChart> findActionNeededByClinician(User clinician) {
        return repo.findByClinicianAndStatusInOrderByCreatedAtDesc(clinician,
                List.of(ProsthodonticsChartStatus.CREATED, ProsthodonticsChartStatus.IN_PROGRESS, ProsthodonticsChartStatus.REVISE));
    }

    public List<ProsthodonticsChart> findAwaitingApprovalByFaculty(User faculty) {
        return repo.findByFacultyAndStatusOrderByCreatedAtDesc(faculty, ProsthodonticsChartStatus.SUBMITTED);
    }

    public String saveFile(MultipartFile file, Long chartId, String fieldName) throws IOException {
        if (file == null || file.isEmpty()) return null;
        Path uploadPath = Paths.get(UPLOAD_DIR + chartId + "/");
        Files.createDirectories(uploadPath);
        // Sanitise: keep only the bare filename (no path separators from client)
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) originalName = "file";
        originalName = Paths.get(originalName).getFileName().toString()
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        String filename = fieldName + "_" + UUID.randomUUID() + "_" + originalName;
        Files.copy(file.getInputStream(), uploadPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/prosthodontics/" + chartId + "/" + filename;
    }

    public ProsthodonticsChart save(Long id, ProsthodonticsChart updated,
                                    MultipartFile patientConsentFile,
                                    MultipartFile patientWorkupFile,
                                    MultipartFile primaryImpressionFile,
                                    MultipartFile individualTrayFile,
                                    MultipartFile finalImpressionFile,
                                    MultipartFile waxPatternLabFile,
                                    MultipartFile waxPatternDiagFile,
                                    MultipartFile metalFrameworkFile,
                                    MultipartFile trialDentureBaseFile,
                                    MultipartFile ocrFile,
                                    MultipartFile positionalRelationsFile,
                                    MultipartFile mountingFile,
                                    MultipartFile anteriorSetupFile,
                                    MultipartFile anteriorTryInFile,
                                    MultipartFile anteriorTryInSignatureFile,
                                    MultipartFile posteriorSetupFile,
                                    MultipartFile posteriorTryInFile,
                                    MultipartFile posteriorTryInSignatureFile,
                                    MultipartFile waxUpFile,
                                    MultipartFile processingFile,
                                    MultipartFile cefFile,
                                    MultipartFile installationFile,
                                    MultipartFile installationSignatureFile,
                                    MultipartFile postInstallationFile,
                                    MultipartFile postInstallationSignatureFile) throws IOException {

        ProsthodonticsChart chart = repo.findById(id).orElseThrow();

        chart.setCaseNumber(updated.getCaseNumber());
        if (updated.getCaseType() != null && java.util.List.of("CD", "SD", "RPD").contains(updated.getCaseType()))
            chart.setCaseType(updated.getCaseType());
        boolean sd = "SD".equals(chart.getCaseType()), rpd = "RPD".equals(chart.getCaseType());
        chart.setSdMx(sd && Boolean.TRUE.equals(updated.getSdMx()));
        chart.setSdMd(sd && Boolean.TRUE.equals(updated.getSdMd()));
        chart.setRpdMx(rpd && Boolean.TRUE.equals(updated.getRpdMx()));
        chart.setRpdMd(rpd && Boolean.TRUE.equals(updated.getRpdMd()));
        chart.setRpdMxWithFramework(rpd && Boolean.TRUE.equals(updated.getRpdMxWithFramework()));
        chart.setRpdMxWithout(rpd && Boolean.TRUE.equals(updated.getRpdMxWithout()));
        chart.setRpdMdWithFramework(rpd && Boolean.TRUE.equals(updated.getRpdMdWithFramework()));
        chart.setRpdMdWithout(rpd && Boolean.TRUE.equals(updated.getRpdMdWithout()));
        chart.setStartedDate(updated.getStartedDate());
        chart.setFinishedDate(updated.getFinishedDate());
        // Grade / Cleared by / Cleared Date are set by the faculty when they clear the case

        chart.setPatientConsentDate(updated.getPatientConsentDate());
        chart.setPatientWorkupDate(updated.getPatientWorkupDate());
        chart.setPrimaryImpressionDate(updated.getPrimaryImpressionDate());
        chart.setIndividualTrayNotApplicable(updated.getIndividualTrayNotApplicable());
        chart.setIndividualTrayDate(updated.getIndividualTrayDate());
        chart.setMouthPreparationsNotApplicable(updated.getMouthPreparationsNotApplicable());
        chart.setMouthPreparationsDate(updated.getMouthPreparationsDate());
        chart.setMouthPreparationsList(updated.getMouthPreparationsList());
        chart.setFinalImpressionDate(updated.getFinalImpressionDate());
        chart.setWaxPatternLabNotApplicable(updated.getWaxPatternLabNotApplicable());
        chart.setWaxPatternLabDate(updated.getWaxPatternLabDate());
        chart.setWaxPatternDiagNotApplicable(updated.getWaxPatternDiagNotApplicable());
        chart.setWaxPatternDiagDate(updated.getWaxPatternDiagDate());
        chart.setMetalFrameworkNotApplicable(updated.getMetalFrameworkNotApplicable());
        chart.setMetalFrameworkDate(updated.getMetalFrameworkDate());
        chart.setTrialDentureBaseNotApplicable(updated.getTrialDentureBaseNotApplicable());
        chart.setTrialDentureBaseDate(updated.getTrialDentureBaseDate());
        chart.setOcrDate(updated.getOcrDate());
        chart.setPositionalRelationsDate(updated.getPositionalRelationsDate());
        chart.setMountingDate(updated.getMountingDate());
        chart.setAnteriorSetupNotApplicable(updated.getAnteriorSetupNotApplicable());
        chart.setAnteriorSetupDate(updated.getAnteriorSetupDate());
        chart.setAnteriorSetupMould(updated.getAnteriorSetupMould());
        chart.setAnteriorSetupShade(updated.getAnteriorSetupShade());
        chart.setAnteriorTryInNotApplicable(updated.getAnteriorTryInNotApplicable());
        chart.setAnteriorTryInDate(updated.getAnteriorTryInDate());
        chart.setProtrusiveRecordsNotApplicable(updated.getProtrusiveRecordsNotApplicable());
        chart.setProtrusiveRecordsDate(updated.getProtrusiveRecordsDate());
        chart.setHcaRight(updated.getHcaRight());
        chart.setHcaLeft(updated.getHcaLeft());
        chart.setLcaRight(updated.getLcaRight());
        chart.setLcaLeft(updated.getLcaLeft());
        chart.setPosteriorSetupDate(updated.getPosteriorSetupDate());
        chart.setPosteriorSetupMould(updated.getPosteriorSetupMould());
        chart.setPosteriorSetupShade(updated.getPosteriorSetupShade());
        chart.setPosteriorTryInDate(updated.getPosteriorTryInDate());
        chart.setWaxUpDate(updated.getWaxUpDate());
        chart.setProcessingDate(updated.getProcessingDate());
        chart.setCefDate(updated.getCefDate());
        chart.setInstallationDate(updated.getInstallationDate());
        chart.setPostInstallationDate(updated.getPostInstallationDate());
        chart.setRemarks(updated.getRemarks());

        // File uploads — only overwrite if a new file was provided
        if (patientConsentFile != null && !patientConsentFile.isEmpty())
            chart.setPatientConsentFilePath(saveFile(patientConsentFile, id, "patientConsent"));
        if (patientWorkupFile != null && !patientWorkupFile.isEmpty())
            chart.setPatientWorkupFilePath(saveFile(patientWorkupFile, id, "patientWorkup"));
        if (primaryImpressionFile != null && !primaryImpressionFile.isEmpty())
            chart.setPrimaryImpressionFilePath(saveFile(primaryImpressionFile, id, "primaryImpression"));
        if (individualTrayFile != null && !individualTrayFile.isEmpty())
            chart.setIndividualTrayFilePath(saveFile(individualTrayFile, id, "individualTray"));
        if (finalImpressionFile != null && !finalImpressionFile.isEmpty())
            chart.setFinalImpressionFilePath(saveFile(finalImpressionFile, id, "finalImpression"));
        if (waxPatternLabFile != null && !waxPatternLabFile.isEmpty())
            chart.setWaxPatternLabFilePath(saveFile(waxPatternLabFile, id, "waxPatternLab"));
        if (waxPatternDiagFile != null && !waxPatternDiagFile.isEmpty())
            chart.setWaxPatternDiagFilePath(saveFile(waxPatternDiagFile, id, "waxPatternDiag"));
        if (metalFrameworkFile != null && !metalFrameworkFile.isEmpty())
            chart.setMetalFrameworkFilePath(saveFile(metalFrameworkFile, id, "metalFramework"));
        if (trialDentureBaseFile != null && !trialDentureBaseFile.isEmpty())
            chart.setTrialDentureBaseFilePath(saveFile(trialDentureBaseFile, id, "trialDentureBase"));
        if (ocrFile != null && !ocrFile.isEmpty())
            chart.setOcrFilePath(saveFile(ocrFile, id, "ocr"));
        if (positionalRelationsFile != null && !positionalRelationsFile.isEmpty())
            chart.setPositionalRelationsFilePath(saveFile(positionalRelationsFile, id, "positionalRelations"));
        if (mountingFile != null && !mountingFile.isEmpty())
            chart.setMountingFilePath(saveFile(mountingFile, id, "mounting"));
        if (anteriorSetupFile != null && !anteriorSetupFile.isEmpty())
            chart.setAnteriorSetupFilePath(saveFile(anteriorSetupFile, id, "anteriorSetup"));
        if (anteriorTryInFile != null && !anteriorTryInFile.isEmpty())
            chart.setAnteriorTryInFilePath(saveFile(anteriorTryInFile, id, "anteriorTryIn"));
        if (anteriorTryInSignatureFile != null && !anteriorTryInSignatureFile.isEmpty())
            chart.setAnteriorTryInSignaturePath(saveFile(anteriorTryInSignatureFile, id, "anteriorTryInSig"));
        if (posteriorSetupFile != null && !posteriorSetupFile.isEmpty())
            chart.setPosteriorSetupFilePath(saveFile(posteriorSetupFile, id, "posteriorSetup"));
        if (posteriorTryInFile != null && !posteriorTryInFile.isEmpty())
            chart.setPosteriorTryInFilePath(saveFile(posteriorTryInFile, id, "posteriorTryIn"));
        if (posteriorTryInSignatureFile != null && !posteriorTryInSignatureFile.isEmpty())
            chart.setPosteriorTryInSignaturePath(saveFile(posteriorTryInSignatureFile, id, "posteriorTryInSig"));
        if (waxUpFile != null && !waxUpFile.isEmpty())
            chart.setWaxUpFilePath(saveFile(waxUpFile, id, "waxUp"));
        if (processingFile != null && !processingFile.isEmpty())
            chart.setProcessingFilePath(saveFile(processingFile, id, "processing"));
        if (cefFile != null && !cefFile.isEmpty())
            chart.setCefFilePath(saveFile(cefFile, id, "cef"));
        if (installationFile != null && !installationFile.isEmpty())
            chart.setInstallationFilePath(saveFile(installationFile, id, "installation"));
        if (installationSignatureFile != null && !installationSignatureFile.isEmpty())
            chart.setInstallationSignaturePath(saveFile(installationSignatureFile, id, "installationSig"));
        if (postInstallationFile != null && !postInstallationFile.isEmpty())
            chart.setPostInstallationFilePath(saveFile(postInstallationFile, id, "postInstallation"));
        if (postInstallationSignatureFile != null && !postInstallationSignatureFile.isEmpty())
            chart.setPostInstallationSignaturePath(saveFile(postInstallationSignatureFile, id, "postInstallationSig"));

        // Only advance from CREATED → IN_PROGRESS; never revert SUBMITTED / APPROVED / REVISE
        if (chart.getStatus() == ProsthodonticsChartStatus.CREATED) {
            chart.setStatus(ProsthodonticsChartStatus.IN_PROGRESS);
        }
        return repo.save(chart);
    }

    /** Clears uploaded files the clinician removed (field names as in the form, e.g. "patientWorkup", "anteriorTryInSignature"). */
    public void removeFiles(Long id, List<String> fields) {
        if (fields == null || fields.isEmpty()) return;
        ProsthodonticsChart c = repo.findById(id).orElseThrow();
        if (c.getStatus() == ProsthodonticsChartStatus.APPROVED) return;
        for (String f : fields) {
            switch (f) {
                case "patientConsent" -> c.setPatientConsentFilePath(null);
                case "patientWorkup" -> c.setPatientWorkupFilePath(null);
                case "primaryImpression" -> c.setPrimaryImpressionFilePath(null);
                case "individualTray" -> c.setIndividualTrayFilePath(null);
                case "finalImpression" -> c.setFinalImpressionFilePath(null);
                case "waxPatternLab" -> c.setWaxPatternLabFilePath(null);
                case "waxPatternDiag" -> c.setWaxPatternDiagFilePath(null);
                case "metalFramework" -> c.setMetalFrameworkFilePath(null);
                case "trialDentureBase" -> c.setTrialDentureBaseFilePath(null);
                case "ocr" -> c.setOcrFilePath(null);
                case "positionalRelations" -> c.setPositionalRelationsFilePath(null);
                case "mounting" -> c.setMountingFilePath(null);
                case "anteriorSetup" -> c.setAnteriorSetupFilePath(null);
                case "anteriorTryIn" -> c.setAnteriorTryInFilePath(null);
                case "anteriorTryInSignature" -> c.setAnteriorTryInSignaturePath(null);
                case "posteriorSetup" -> c.setPosteriorSetupFilePath(null);
                case "posteriorTryIn" -> c.setPosteriorTryInFilePath(null);
                case "posteriorTryInSignature" -> c.setPosteriorTryInSignaturePath(null);
                case "waxUp" -> c.setWaxUpFilePath(null);
                case "processing" -> c.setProcessingFilePath(null);
                case "cef" -> c.setCefFilePath(null);
                case "installation" -> c.setInstallationFilePath(null);
                case "postInstallation" -> c.setPostInstallationFilePath(null);
                default -> { }
            }
        }
        repo.save(c);
    }

    // ── Clinician: cleared cases (the grade) show with their evaluations ─────

    public List<ProsthodonticsChart> clearedForClinician(User clinician) {
        if (clinician == null) return List.of();
        return repo.findByClinicianAndClearedAtIsNotNullOrderByClearedAtDesc(clinician).stream()
                .filter(c -> c.getStatus() == ProsthodonticsChartStatus.APPROVED).toList();
    }

    public List<ProsthodonticsChart> newClearedForClinician(User clinician) {
        return clearedForClinician(clinician).stream().filter(c -> c.getClearedViewedAt() == null).toList();
    }

    /** The clinician opened their cleared case: no longer "New". */
    public void markClearedViewed(ProsthodonticsChart chart, User viewer) {
        if (chart.getClearedAt() == null || chart.getClearedViewedAt() != null) return;
        if (viewer == null || chart.getClinician() == null || !chart.getClinician().getId().equals(viewer.getId())) return;
        chart.setClearedViewedAt(java.time.LocalDateTime.now());
        repo.save(chart);
    }

    public ProsthodonticsChart submit(Long id) {
        ProsthodonticsChart chart = repo.findById(id).orElseThrow();
        chart.setStatus(ProsthodonticsChartStatus.SUBMITTED);
        return repo.save(chart);
    }

    /** Clears the case: the faculty gives the grade and signs "Cleared by (SGN)"; the date is filled in. */
    public ProsthodonticsChart clear(Long id, String grade, String signature, User faculty) {
        ProsthodonticsChart chart = repo.findById(id).orElseThrow();
        if (chart.getStatus() != ProsthodonticsChartStatus.SUBMITTED) return chart;   // only a submitted chart can be reviewed
        if (grade == null || grade.isBlank() || !PeriodonticChartService.isSignature(signature))
            throw new IllegalArgumentException("A grade and a drawn signature are required to clear the case.");
        chart.setGrade(grade.trim());
        chart.setClearedSignature(signature);
        chart.setClearedAt(java.time.LocalDateTime.now());
        chart.setClearedDate(java.time.LocalDate.now().toString());
        chart.setClearedBy(faculty != null ? faculty.getFullName() : null);
        chart.setStatus(ProsthodonticsChartStatus.APPROVED);
        return repo.save(chart);
    }

    public ProsthodonticsChart revise(Long id, String reason) {
        ProsthodonticsChart chart = repo.findById(id).orElseThrow();
        if (chart.getStatus() != ProsthodonticsChartStatus.SUBMITTED) return chart;   // only a submitted chart can be reviewed
        chart.setStatus(ProsthodonticsChartStatus.REVISE);
        chart.setRevisionReason(reason);
        return repo.save(chart);
    }
}
