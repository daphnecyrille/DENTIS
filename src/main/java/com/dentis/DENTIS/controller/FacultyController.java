package com.dentis.DENTIS.controller;

import com.dentis.DENTIS.model.OralSurgeryChart;
import com.dentis.DENTIS.model.Patient;
import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.repository.UserRepository;
import com.dentis.DENTIS.service.ClinicianFacultyAssignmentService;
import com.dentis.DENTIS.service.EndodonticsChartService;
import com.dentis.DENTIS.service.OperativeChartService;
import com.dentis.DENTIS.service.OralSurgeryChartService;
import com.dentis.DENTIS.service.PatientService;
import com.dentis.DENTIS.service.PeriodonticChartService;
import com.dentis.DENTIS.service.ProsthodonticsChartService;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class FacultyController {

    private final PatientService patientService;
    private final OralSurgeryChartService oralSurgeryChartService;
    private final EndodonticsChartService endodonticsChartService;
    private final OperativeChartService operativeChartService;
    private final PeriodonticChartService periodonticChartService;
    private final UserRepository userRepository;
    private final ClinicianFacultyAssignmentService assignmentService;
    private final ProsthodonticsChartService prosthodonticsChartService;

    public FacultyController(PatientService patientService,
                             OralSurgeryChartService oralSurgeryChartService,
                             EndodonticsChartService endodonticsChartService,
                             OperativeChartService operativeChartService,
                             PeriodonticChartService periodonticChartService,
                             UserRepository userRepository,
                             ClinicianFacultyAssignmentService assignmentService,
                             ProsthodonticsChartService prosthodonticsChartService) {
        this.patientService = patientService;
        this.oralSurgeryChartService = oralSurgeryChartService;
        this.endodonticsChartService = endodonticsChartService;
        this.operativeChartService = operativeChartService;
        this.periodonticChartService = periodonticChartService;
        this.userRepository = userRepository;
        this.assignmentService = assignmentService;
        this.prosthodonticsChartService = prosthodonticsChartService;
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            String email = oAuth2User.getAttribute("email");
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }

    @GetMapping("/faculty-dashboard")
    public String dashboard(Model model, Authentication authentication,
                            @RequestParam(value = "assigned", required = false) Long assignedId) {
        User faculty = getCurrentUser(authentication);
        model.addAttribute("currentUser", faculty);
        if (assignedId != null) model.addAttribute("justAssigned", patientService.getPatientById(assignedId));
        model.addAttribute("advisees", adviseeOptions(faculty));
        model.addAttribute("clinicianPatientCounts", patientService.getUniquePatientCountByClinician());

        // ── Dashboard summary: one review queue (oldest first), recently approved, counts ──
        List<DashRow> rows = facultyRows(faculty);
        List<DashRow> review = rows.stream().filter(r -> "SUBMITTED".equals(r.getStatus()))
                .sorted(java.util.Comparator.comparing(DashRow::getWhen, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())))
                .toList();
        List<DashRow> approved = rows.stream().filter(r -> "APPROVED".equals(r.getStatus()))
                .sorted(java.util.Comparator.comparing(DashRow::getWhen, java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())))
                .limit(15).toList();
        List<com.dentis.DENTIS.model.Patient> toAssign = faculty != null ? patientService.getUnassignedPatientsForFaculty(faculty) : List.of();
        long activePatients = rows.stream().filter(r -> !"APPROVED".equals(r.getStatus()))
                .map(DashRow::getPatientId).distinct().count();
        model.addAttribute("reviewRows", review);
        model.addAttribute("approvedRows", approved);
        model.addAttribute("toAssign", toAssign);
        model.addAttribute("activePatientCount", activePatients);
        model.addAttribute("approvedCount", rows.stream().filter(r -> "APPROVED".equals(r.getStatus())).count());
        return "dashboard-faculty";
    }

    /** One form of a chart assigned to this faculty (a chart with two forms gives two rows). */
    public static class DashRow {
        private final String form, chartNo, patient, clinician, status, url;
        private final Long patientId;
        private final java.time.LocalDateTime when;
        DashRow(String form, String chartNo, com.dentis.DENTIS.model.Patient p, User clinician, Enum<?> status,
                java.time.LocalDateTime when, String url) {
            this.form = form; this.chartNo = chartNo; this.url = url; this.when = when;
            this.patientId = p != null ? p.getId() : null;
            this.patient = p == null ? "—" : p.getSurname() + ", " + p.getFirstName()
                    + (p.getMiddleInitial() != null && !p.getMiddleInitial().isBlank() ? " " + p.getMiddleInitial() + "." : "");
            this.clinician = clinician != null ? clinician.getFullName() : "—";
            this.status = status != null ? status.name() : "CREATED";
        }
        public String getForm() { return form; }
        public String getChartNo() { return chartNo; }
        public String getPatient() { return patient; }
        public String getClinician() { return clinician; }
        public String getStatus() { return status; }
        public String getUrl() { return url; }
        public Long getPatientId() { return patientId; }
        public java.time.LocalDateTime getWhen() { return when; }
    }

    private List<DashRow> facultyRows(User faculty) {
        List<DashRow> out = new java.util.ArrayList<>();
        if (faculty == null) return out;
        for (var c : oralSurgeryChartService.findByFacultyOrderByCreatedAtDesc(faculty)) {
            boolean osf2 = c.getChartType() != null && c.getChartType().name().equals("OSF2");
            out.add(new DashRow(c.getChartType() != null ? c.getChartType().getDisplayName() : "Oral Surgery", c.getChartNo(), c.getPatient(),
                    c.getClinician(), c.getStatus(), c.getUpdatedAt(), (osf2 ? "/oralsurgery2-view-faculty/" : "/oralsurgery-view-faculty/") + c.getId()));
        }
        for (var c : endodonticsChartService.findByFacultyOrderByCreatedAtDesc(faculty)) {
            out.add(new DashRow("Endodontics – Patient Workup", c.getChartNo(), c.getPatient(), c.getClinician(), c.getStatus(),
                    c.getUpdatedAt(), "/endodontics-view-faculty/" + c.getId()));
            out.add(new DashRow("Endodontics – Evaluation & Treatment Planning", c.getChartNo() != null ? c.getChartNo().replace("-ENDO1", "-ENDO2") : null,
                    c.getPatient(), c.getClinician(), c.getForm2Status(), c.getUpdatedAt(), "/endodontics2-view-faculty/" + c.getId()));
        }
        for (var c : periodonticChartService.findByFacultyOrderByCreatedAtDesc(faculty)) {
            String no = c.getChartNo() != null ? c.getChartNo() : "";
            out.add(new DashRow("Periodontics – Periodontal Chart (PF1)", no, c.getPatient(), c.getClinician(), c.getStatus(),
                    c.getUpdatedAt(), "/periodontics-a/" + c.getId() + "/view"));
            out.add(new DashRow("Periodontics – PPD/CAL/BOP Chart (PF2)", no.replace("-PF1", "-PF2"), c.getPatient(), c.getClinician(), c.getFormCStatus(),
                    c.getUpdatedAt(), "/periodontics-view-faculty/" + c.getId()));
            String recall = c.getPf3CurrentRecall() > 0 ? " · Recall " + c.getPf3CurrentRecall() : "";
            out.add(new DashRow("Periodontics – Gingivitis Reevaluation (PF3)" + recall, no.replace("-PF1", "-PF3"), c.getPatient(), c.getClinician(), c.getPf3Status(),
                    c.getUpdatedAt(), "/periodontics-pf3/" + c.getId() + "/view"));
        }
        for (var c : operativeChartService.findByFacultyOrderByCreatedAtDesc(faculty)) {
            String no = c.getChartNo() != null ? c.getChartNo() : "";
            out.add(new DashRow("Operative – Assessment Form (RESTO10)", no, c.getPatient(), c.getClinician(), c.getStatus(),
                    c.getUpdatedAt(), "/operative-view-faculty/" + c.getId()));
            out.add(new DashRow("Operative – Caries Risk Assessment (RESTO6)", no.replace("-RESTO10", "-RESTO6"), c.getPatient(), c.getClinician(), c.getForm2Status(),
                    c.getUpdatedAt(), "/operative2-view-faculty/" + c.getId()));
        }
        for (var c : prosthodonticsChartService.findByFacultyOrderByCreatedAtDesc(faculty)) {
            out.add(new DashRow("Prosthodontics (" + (c.getCaseType() != null ? c.getCaseType() : "—") + ") – Case Booklet", c.getChartNo(), c.getPatient(),
                    c.getClinician(), c.getStatus(), c.getUpdatedAt(), "/prosthodontics-faculty/" + c.getId()));
        }
        return out;
    }

    @GetMapping("/faculty-patientlist")
    /** All patients of the faculty's section, not only the ones assigned to them. */
    public String patientList(Model model, Authentication authentication) {
        User faculty = getCurrentUser(authentication);
        model.addAttribute("currentUser", faculty);

        String section = faculty != null && faculty.getSection() != null
                ? faculty.getSection().toUpperCase() : "";

        model.addAttribute("osCharts", "OS".equals(section) && faculty != null
                ? oralSurgeryChartService.findAllOrderByCreatedAtDesc()
                : List.of());
        model.addAttribute("endoCharts", "ENDO".equals(section) && faculty != null
                ? endodonticsChartService.findAllOrderByCreatedAtDesc()
                : List.of());
        model.addAttribute("perioCharts", "PERIO".equals(section) && faculty != null
                ? periodonticChartService.findAllOrderByCreatedAtDesc()
                : List.of());
        model.addAttribute("operativeCharts", "RESTO".equals(section) && faculty != null
                ? operativeChartService.findAllOrderByCreatedAtDesc()
                : List.of());
        model.addAttribute("prosthoCharts", "PROSTHO".equals(section) && faculty != null
                ? prosthodonticsChartService.findAllOrderByCreatedAtDesc()
                : List.of());
        model.addAttribute("clinicianPatientCounts", patientService.getUniquePatientCountByClinician());

        // One row per patient (all of the section's patients), with "mine" = has a chart assigned to this faculty
        @SuppressWarnings("unchecked")
        List<java.util.Map<String, Object>> rows = com.dentis.DENTIS.service.PatientRowBuilder.build(
                (List<com.dentis.DENTIS.model.OralSurgeryChart>) model.getAttribute("osCharts"),
                (List<com.dentis.DENTIS.model.EndodonticsChart>) model.getAttribute("endoCharts"),
                (List<com.dentis.DENTIS.model.PeriodonticChart>) model.getAttribute("perioCharts"),
                (List<com.dentis.DENTIS.model.OperativeChart>) model.getAttribute("operativeCharts"),
                (List<com.dentis.DENTIS.model.ProsthodonticsChart>) model.getAttribute("prosthoCharts"));
        for (var r : rows) {
            @SuppressWarnings("unchecked") java.util.Set<Long> ids = (java.util.Set<Long>) r.get("facultyIds");
            r.put("mine", faculty != null && ids.contains(faculty.getId()));
        }
        model.addAttribute("patientRows", rows);
        return "patientlist-faculty";
    }

    @GetMapping("/faculty-advisees")
    public String advisees(Model model, Authentication authentication) {
        User faculty = getCurrentUser(authentication);
        model.addAttribute("currentUser", faculty);
        String section = faculty != null && faculty.getSection() != null
                ? faculty.getSection() : "";
        List<User> advisees = faculty != null
                ? new java.util.ArrayList<>(assignmentService.getAdviseesForFaculty(faculty, section))
                : new java.util.ArrayList<>();

        // Each advisee's patients in this section, one row per patient with chart status tags,
        // plus counts of forms awaiting approval / for revision / in progress
        Map<Long, List<Map<String, Object>>> rowsMap = new LinkedHashMap<>();
        Map<Long, Map<String, Integer>> countsMap = new LinkedHashMap<>();
        String sec = section.toUpperCase();
        for (User c : advisees) {
            List<Map<String, Object>> rows = com.dentis.DENTIS.service.PatientRowBuilder.build(
                    "OS".equals(sec) ? oralSurgeryChartService.findByClinicianOrderByCreatedAtDesc(c) : List.of(),
                    "ENDO".equals(sec) ? endodonticsChartService.findByClinicianOrderByCreatedAtDesc(c) : List.of(),
                    "PERIO".equals(sec) ? periodonticChartService.findByClinicianOrderByCreatedAtDesc(c) : List.of(),
                    "RESTO".equals(sec) ? operativeChartService.findByClinicianOrderByCreatedAtDesc(c) : List.of(),
                    "PROSTHO".equals(sec) ? prosthodonticsChartService.findByClinicianOrderByCreatedAtDesc(c) : List.of());
            Map<String, Integer> n = new java.util.HashMap<>(Map.of("awaiting", 0, "revise", 0, "progress", 0, "done", 0, "patients", rows.size()));
            for (Map<String, Object> r : rows) {
                @SuppressWarnings("unchecked") List<String> sts = (List<String>) r.get("statuses");
                for (String st : sts) {
                    switch (st) {
                        case "SUBMITTED" -> n.merge("awaiting", 1, Integer::sum);
                        case "REVISE" -> n.merge("revise", 1, Integer::sum);
                        case "CREATED", "IN_PROGRESS" -> n.merge("progress", 1, Integer::sum);
                        default -> { }
                    }
                }
                if (Boolean.TRUE.equals(r.get("done"))) n.merge("done", 1, Integer::sum);
            }
            rowsMap.put(c.getId(), rows);
            countsMap.put(c.getId(), n);
        }
        // advisees with charts waiting for review first, then revisions, then by name
        advisees.sort(java.util.Comparator
                .comparing((User u) -> -countsMap.get(u.getId()).get("awaiting"))
                .thenComparing(u -> -countsMap.get(u.getId()).get("revise"))
                .thenComparing(u -> u.getFullName() == null ? "" : u.getFullName()));

        model.addAttribute("advisees", advisees);
        model.addAttribute("rowsMap", rowsMap);
        model.addAttribute("countsMap", countsMap);
        model.addAttribute("section", section);
        return "faculty-advisees";
    }

    @GetMapping("/faculty-assign")
    public String assignPatients(Model model, Authentication authentication,
                                 @RequestParam(value = "assigned", required = false) Long assignedId) {
        User faculty = getCurrentUser(authentication);
        model.addAttribute("currentUser", faculty);
        // waiting longest first
        List<Patient> waiting = faculty != null ? new java.util.ArrayList<>(patientService.getUnassignedPatientsForFaculty(faculty)) : new java.util.ArrayList<>();
        waiting.sort(java.util.Comparator.comparing(Patient::getCreatedAt, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())));
        model.addAttribute("patients", waiting);
        model.addAttribute("advisees", adviseeOptions(faculty));
        // assigned in the last 7 days, newest first
        java.time.LocalDateTime since = java.time.LocalDateTime.now().minusDays(7);
        model.addAttribute("recentlyAssigned", faculty == null ? List.of()
                : patientService.getAssignedPatientsForFaculty(faculty).stream()
                    .filter(p -> p.getClinicianAssignedAt() != null && p.getClinicianAssignedAt().isAfter(since))
                    .sorted(java.util.Comparator.comparing(Patient::getClinicianAssignedAt).reversed())
                    .limit(10).toList());
        if (assignedId != null) model.addAttribute("justAssigned", patientService.getPatientById(assignedId));
        return "assign-patients-faculty";
    }

    /** Assign Patients picker: this faculty's advisees in their section, with how many active patients each has there. */
    public static class AdviseeOption {
        private final User user; private final int active;
        AdviseeOption(User user, int active) { this.user = user; this.active = active; }
        public User getUser() { return user; }
        public int getActive() { return active; }
        public String getLabel() {
            return user.getFullName() + (user.getYearLevel() != null && !user.getYearLevel().isBlank() ? " · " + user.getYearLevel() : "")
                    + " · " + active + (active == 1 ? " active patient" : " active patients");
        }
    }

    private List<AdviseeOption> adviseeOptions(User faculty) {
        if (faculty == null || faculty.getSection() == null) return List.of();
        String sec = faculty.getSection().toUpperCase();
        List<AdviseeOption> out = new java.util.ArrayList<>();
        for (User c : assignmentService.getAdviseesForFaculty(faculty, faculty.getSection())) {
            List<Map<String, Object>> rows = com.dentis.DENTIS.service.PatientRowBuilder.build(
                    "OS".equals(sec) ? oralSurgeryChartService.findByClinicianOrderByCreatedAtDesc(c) : List.of(),
                    "ENDO".equals(sec) ? endodonticsChartService.findByClinicianOrderByCreatedAtDesc(c) : List.of(),
                    "PERIO".equals(sec) ? periodonticChartService.findByClinicianOrderByCreatedAtDesc(c) : List.of(),
                    "RESTO".equals(sec) ? operativeChartService.findByClinicianOrderByCreatedAtDesc(c) : List.of(),
                    "PROSTHO".equals(sec) ? prosthodonticsChartService.findByClinicianOrderByCreatedAtDesc(c) : List.of());
            int active = (int) rows.stream().filter(r -> !Boolean.TRUE.equals(r.get("done"))).count();
            out.add(new AdviseeOption(c, active));
        }
        out.sort(java.util.Comparator.comparingInt(AdviseeOption::getActive).thenComparing(o -> o.getUser().getFullName()));
        return out;
    }

    @GetMapping("/chartsview-faculty/{id}")
    public String chartsView(@PathVariable Long id, Model model, Authentication authentication) {
        User faculty = getCurrentUser(authentication);
        Patient patient = patientService.getPatientById(id);

        String section = faculty != null && faculty.getSection() != null
                ? faculty.getSection().toUpperCase() : "";

        model.addAttribute("currentUser", faculty);
        model.addAttribute("patient", patient);
        model.addAttribute("osCharts", "OS".equals(section)
                ? oralSurgeryChartService.findAllByPatient(patient)
                : List.of());
        model.addAttribute("endoCharts", "ENDO".equals(section)
                ? endodonticsChartService.findAllByPatient(patient)
                : List.of());
        model.addAttribute("perioCharts", "PERIO".equals(section)
                ? periodonticChartService.findAllByPatient(patient)
                : List.of());
        model.addAttribute("operativeCharts", "RESTO".equals(section)
                ? operativeChartService.findAllByPatient(patient)
                : List.of());
        model.addAttribute("prosthoCharts", "PROSTHO".equals(section)
                ? prosthodonticsChartService.findAllByPatient(patient)
                : List.of());
        model.addAttribute("clinicianPatientCounts", patientService.getUniquePatientCountByClinician());
        return "chartsview-faculty";
    }

    @GetMapping("/admitting-view-faculty/{id}")
    public String admittingView(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("patient", patientService.getPatientById(id));
        return "admitting-view-faculty";
    }

    @PostMapping("/oralsurgery-faculty/{id}/approve")
    public String approveChart(@PathVariable Long id, Authentication authentication) {
        OralSurgeryChart owned = oralSurgeryChartService.getById(id);   // only the faculty assigned to this chart can approve / revise it
        if (!isAssignedFaculty(owned.getFaculty(), authentication))
            return "redirect:/chartsview-faculty/" + owned.getPatient().getId() + "?notAssigned";
        OralSurgeryChart chart = oralSurgeryChartService.approve(id);
        return "redirect:/chartsview-faculty/" + chart.getPatient().getId();
    }

    @PostMapping("/oralsurgery-faculty/{id}/revise")
    public String reviseChart(@PathVariable Long id, @RequestParam String revisionReason, Authentication authentication) {
        OralSurgeryChart owned = oralSurgeryChartService.getById(id);   // only the faculty assigned to this chart can approve / revise it
        if (!isAssignedFaculty(owned.getFaculty(), authentication))
            return "redirect:/chartsview-faculty/" + owned.getPatient().getId() + "?notAssigned";
        OralSurgeryChart chart = oralSurgeryChartService.revise(id, revisionReason);
        return "redirect:/chartsview-faculty/" + chart.getPatient().getId();
    }

    @PostMapping("/faculty-dashboard/{id}/assign-clinician")
    public String assignClinician(@PathVariable Long id,
                                  @RequestParam Long clinicianId,
                                  @RequestParam(required = false, defaultValue = "faculty-assign") String returnTo,
                                  Authentication authentication) {
        User faculty = getCurrentUser(authentication);
        patientService.assignClinician(id, clinicianId, faculty);
        String back = "faculty-dashboard".equals(returnTo) ? "faculty-dashboard" : "faculty-assign";   // only our own pages
        return "redirect:/" + back + "?assigned=" + id;
    }

    /** True when the logged-in user is the faculty assigned to the chart. */
    private boolean isAssignedFaculty(User chartFaculty, Authentication authentication) {
        User me = getCurrentUser(authentication);
        return me != null && chartFaculty != null && chartFaculty.getId().equals(me.getId());
    }
}
