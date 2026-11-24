package com.dentis.DENTIS.service;

import org.springframework.stereotype.Service;

import java.util.List;

/** Every chart form in the clinic with its status, for overviews and the pending-review list. */
@Service
public class ChartFormsService {

    /** One chart form. */
    public record Form(String label, String chartNo, String patient, String clinician, String faculty, Long facultyId,
                       String status, java.time.LocalDateTime when) { }

    private final OralSurgeryChartService osService;
    private final EndodonticsChartService endoService;
    private final PeriodonticChartService perioService;
    private final OperativeChartService operativeService;
    private final ProsthodonticsChartService prosthoService;

    public ChartFormsService(OralSurgeryChartService osService, EndodonticsChartService endoService,
                             PeriodonticChartService perioService, OperativeChartService operativeService,
                             ProsthodonticsChartService prosthoService) {
        this.osService = osService;
        this.endoService = endoService;
        this.perioService = perioService;
        this.operativeService = operativeService;
        this.prosthoService = prosthoService;
    }

    /** Every form of every chart (a chart with several forms gives several entries). */
    public List<Form> all() {
        List<Form> out = new java.util.ArrayList<>();
        java.util.function.Function<com.dentis.DENTIS.model.Patient, String> nm = p -> p == null ? "—" : p.getSurname() + ", " + p.getFirstName();
        java.util.function.Function<com.dentis.DENTIS.model.User, String> un = u -> u == null ? null : u.getFullName();
        java.util.function.Function<com.dentis.DENTIS.model.User, Long> ui = u -> u == null ? null : u.getId();
        for (var c : osService.findAllOrderByCreatedAtDesc())
            out.add(new Form(c.getChartType() != null ? c.getChartType().getDisplayName() : "Oral Surgery", c.getChartNo(), nm.apply(c.getPatient()),
                    un.apply(c.getClinician()), un.apply(c.getFaculty()), ui.apply(c.getFaculty()), c.getStatus().name(), c.getUpdatedAt()));
        for (var c : endoService.findAllOrderByCreatedAtDesc()) {
            out.add(new Form("Endo – Patient Workup", c.getChartNo(), nm.apply(c.getPatient()), un.apply(c.getClinician()), un.apply(c.getFaculty()), ui.apply(c.getFaculty()), c.getStatus().name(), c.getUpdatedAt()));
            out.add(new Form("Endo – Informed Consent", c.getChartNo() != null ? c.getChartNo().replace("-ENDO1", "-ENDO4") : null, nm.apply(c.getPatient()), un.apply(c.getClinician()), un.apply(c.getFaculty()), ui.apply(c.getFaculty()), c.getForm4Status().name(), c.getUpdatedAt()));
        }
        for (var c : perioService.findAllOrderByCreatedAtDesc()) {
            String no = c.getChartNo() != null ? c.getChartNo() : "";
            out.add(new Form("Perio – PF1", no, nm.apply(c.getPatient()), un.apply(c.getClinician()), un.apply(c.getFaculty()), ui.apply(c.getFaculty()), c.getStatus().name(), c.getUpdatedAt()));
            out.add(new Form("Perio – PF2", no.replace("-PF1", "-PF2"), nm.apply(c.getPatient()), un.apply(c.getClinician()), un.apply(c.getFaculty()), ui.apply(c.getFaculty()), c.getFormCStatus().name(), c.getUpdatedAt()));
            out.add(new Form("Perio – PF3", no.replace("-PF1", "-PF3"), nm.apply(c.getPatient()), un.apply(c.getClinician()), un.apply(c.getFaculty()), ui.apply(c.getFaculty()), c.getPf3Status().name(), c.getUpdatedAt()));
        }
        for (var c : operativeService.findAllOrderByCreatedAtDesc()) {
            String no = c.getChartNo() != null ? c.getChartNo() : "";
            out.add(new Form("Operative – Assessment Form for Carious Lesions (RESTO10)", no, nm.apply(c.getPatient()), un.apply(c.getClinician()), un.apply(c.getFaculty()), ui.apply(c.getFaculty()), c.getStatus().name(), c.getUpdatedAt()));
            out.add(new Form("Operative – RESTO6", no.replace("-RESTO10", "-RESTO6"), nm.apply(c.getPatient()), un.apply(c.getClinician()), un.apply(c.getFaculty()), ui.apply(c.getFaculty()), c.getForm2Status().name(), c.getUpdatedAt()));
        }
        for (var c : prosthoService.findAllOrderByCreatedAtDesc())
            out.add(new Form("Prostho – Case Booklet", c.getChartNo(), nm.apply(c.getPatient()), un.apply(c.getClinician()), un.apply(c.getFaculty()), ui.apply(c.getFaculty()), c.getStatus().name(), c.getUpdatedAt()));
        return out;
    }

    /** Submitted forms the faculty hasn't reviewed yet, oldest first. */
    public List<Form> pendingReview() {
        return all().stream().filter(f -> "SUBMITTED".equals(f.status()))
                .sorted(java.util.Comparator.comparing(Form::when, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())))
                .toList();
    }
}
