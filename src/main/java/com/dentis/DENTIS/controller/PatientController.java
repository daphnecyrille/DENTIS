package com.dentis.DENTIS.controller;

import com.dentis.DENTIS.model.EndodonticsChart;
import com.dentis.DENTIS.model.OperativeChart;
import com.dentis.DENTIS.model.OralSurgeryChart;
import com.dentis.DENTIS.model.Patient;
import com.dentis.DENTIS.model.PeriodonticChart;
import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.service.ChartRequestService;
import com.dentis.DENTIS.service.ClinicianFacultyAssignmentService;
import com.dentis.DENTIS.service.EndodonticsChartService;
import com.dentis.DENTIS.service.OperativeChartService;
import com.dentis.DENTIS.service.OralSurgeryChartService;
import com.dentis.DENTIS.service.PatientService;
import com.dentis.DENTIS.model.ProsthodonticsChart;
import com.dentis.DENTIS.service.PeriodonticChartService;
import com.dentis.DENTIS.service.ProsthodonticsChartService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PatientController {

    private final PatientService patientService;
    private final ChartRequestService chartRequestService;
    private final ClinicianFacultyAssignmentService assignmentService;
    private final OralSurgeryChartService oralSurgeryChartService;
    private final EndodonticsChartService endodonticsChartService;
    private final PeriodonticChartService periodonticChartService;
    private final OperativeChartService operativeChartService;
    private final ProsthodonticsChartService prosthodonticsChartService;

    public PatientController(PatientService patientService, ChartRequestService chartRequestService,
                              ClinicianFacultyAssignmentService assignmentService,
                              OralSurgeryChartService oralSurgeryChartService,
                              EndodonticsChartService endodonticsChartService,
                              PeriodonticChartService periodonticChartService,
                              OperativeChartService operativeChartService,
                              ProsthodonticsChartService prosthodonticsChartService) {
        this.patientService = patientService;
        this.chartRequestService = chartRequestService;
        this.assignmentService = assignmentService;
        this.oralSurgeryChartService = oralSurgeryChartService;
        this.endodonticsChartService = endodonticsChartService;
        this.periodonticChartService = periodonticChartService;
        this.operativeChartService = operativeChartService;
        this.prosthodonticsChartService = prosthodonticsChartService;
    }

    @GetMapping("/dashboard-clinicmanager")
    public String dashboard(Model model,
                            @RequestParam(value = "facultyAssigned", required = false) Long facultyAssignedId) {
        List<Patient> all = patientService.getAllPatients();   // every admission (originals and readmissions), newest first
        // waiting for a faculty: oldest first, each with the faculty of its section
        List<Patient> needsFaculty = new ArrayList<>(all.stream().filter(p -> p.getAssignedFaculty() == null).toList());
        java.util.Collections.reverse(needsFaculty);
        java.util.Map<Long, List<com.dentis.DENTIS.model.User>> facultyByAdmission = new java.util.HashMap<>();
        for (Patient p : needsFaculty) facultyByAdmission.put(p.getId(), patientService.getFacultyBySection(p.getServiceCode()));
        // requests: oldest first
        List<com.dentis.DENTIS.model.ChartRequest> pending = new ArrayList<>(chartRequestService.getPendingRequests());
        java.util.Collections.reverse(pending);
        java.time.LocalDate today = java.time.LocalDate.now();
        model.addAttribute("requests", pending);
        model.addAttribute("needsFaculty", needsFaculty);
        model.addAttribute("facultyByAdmission", facultyByAdmission);
        model.addAttribute("recentAdmissions", all.stream().limit(10).toList());
        model.addAttribute("statAdmittedToday", all.stream().filter(p -> today.equals(p.getRegistrationDate())
                || (p.getCreatedAt() != null && today.equals(p.getCreatedAt().toLocalDate()))).count());
        if (facultyAssignedId != null) model.addAttribute("justAssigned", patientService.getPatientById(facultyAssignedId));
        model.addAttribute("statTotalPatients", patientService.countAll());
        model.addAttribute("statOS", patientService.countBySection("OS"));
        model.addAttribute("statENDO", patientService.countBySection("ENDO"));
        model.addAttribute("statPERIO", patientService.countBySection("PERIO"));
        model.addAttribute("statRESTO", patientService.countBySection("Resto"));
        model.addAttribute("statPROSTHO", patientService.countBySection("CD") + patientService.countBySection("RPD")
                + patientService.countBySection("FPD"));
        model.addAttribute("statPendingRequests", chartRequestService.getPendingRequests().size());
        model.addAttribute("statClinicians", patientService.getAllClinicians().size());
        model.addAttribute("clinicianPatientCounts", patientService.getUniquePatientCountByClinician());
        return "dashboard-clinicmanager";
    }

    @GetMapping("/assign-clinicmanager")
    public String assignClinicians(Model model, @RequestParam(value = "saved", required = false) Integer saved) {
        model.addAttribute("assignmentRows", assignmentService.getAssignmentRows());
        String[][] secs = { {"OS", "OS", "osFaculty"}, {"ENDO", "ENDO", "endoFaculty"}, {"PERIO", "PERIO", "perioFaculty"},
                            {"RESTO", "Resto", "restoFaculty"}, {"PROSTHO", "CD", "prosthoFaculty"} };
        // how many advisees each faculty already has in their section (shown in the dropdowns to balance the load)
        java.util.Map<Long, Integer> adviseeCounts = new java.util.HashMap<>();
        java.util.Map<String, List<java.util.Map<String, Object>>> bulkFaculty = new java.util.HashMap<>();   // for the bulk picker
        for (String[] sec : secs) {
            List<User> fac = patientService.getFacultyBySection(sec[1]);
            model.addAttribute(sec[2], fac);
            for (User f : fac) adviseeCounts.put(f.getId(), assignmentService.getAdviseesForFaculty(f, sec[0]).size());
            bulkFaculty.put(sec[0].toLowerCase(), fac.stream()
                    .map(f -> java.util.Map.<String, Object>of("id", f.getId(), "name", f.getFullName())).toList());
        }
        model.addAttribute("bulkFaculty", bulkFaculty);
        model.addAttribute("adviseeCounts", adviseeCounts);
        model.addAttribute("saved", saved);
        return "assign-clinicmanager";
    }

    /** Save all changed rows at once: fields "os_<clinicianId>", "endo_…", "perio_…", "resto_…", "prostho_…" (empty = none). */
    @PostMapping("/assign-clinicmanager/save-all")
    public String saveAllAssignments(@RequestParam java.util.Map<String, String> form) {
        java.util.Set<Long> ids = new java.util.LinkedHashSet<>();
        for (String k : form.keySet()) {
            int i = k.indexOf('_');
            if (i > 0) try { ids.add(Long.parseLong(k.substring(i + 1))); } catch (NumberFormatException ignored) { }
        }
        for (Long id : ids) {
            assignmentService.saveAssignment(id, idOf(form.get("os_" + id)), idOf(form.get("endo_" + id)),
                    idOf(form.get("perio_" + id)), idOf(form.get("resto_" + id)), idOf(form.get("prostho_" + id)));
        }
        return "redirect:/assign-clinicmanager?saved=" + ids.size();
    }

    private static Long idOf(String v) {
        try { return v == null || v.isBlank() ? null : Long.parseLong(v); } catch (NumberFormatException e) { return null; }
    }

    @PostMapping("/assign-clinicmanager/{clinicianId}")
    public String saveAssignment(@PathVariable Long clinicianId,
                                  @RequestParam(required = false) Long osFacultyId,
                                  @RequestParam(required = false) Long endoFacultyId,
                                  @RequestParam(required = false) Long perioFacultyId,
                                  @RequestParam(required = false) Long restoFacultyId,
                                  @RequestParam(required = false) Long prosthoFacultyId) {
        assignmentService.saveAssignment(clinicianId, osFacultyId, endoFacultyId, perioFacultyId, restoFacultyId, prosthoFacultyId);
        return "redirect:/assign-clinicmanager";
    }

    private com.dentis.DENTIS.model.User getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            String email = oAuth2User.getAttribute("email");
            return patientService.getUserByEmail(email);
        }
        return null;
    }

    @GetMapping("/clinician-profile/{id}")
    public String clinicianProfile(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("currentUser", getCurrentUser(authentication));
        User clinician = patientService.getUserById(id);
        List<Patient> allPatients = patientService.getPatientsForClinician(clinician)
                .stream().filter(p -> p.getOriginalPatient() == null).toList();
        model.addAttribute("clinician", clinician);
        model.addAttribute("assignmentRow", assignmentService.getAssignmentRowForClinician(clinician));
        model.addAttribute("osPatients", allPatients.stream()
                .filter(p -> "OS".equalsIgnoreCase(p.getServiceCode())).toList());
        model.addAttribute("endoPatients", allPatients.stream()
                .filter(p -> "ENDO".equalsIgnoreCase(p.getServiceCode())).toList());
        model.addAttribute("perioPatients", allPatients.stream()
                .filter(p -> "PERIO".equalsIgnoreCase(p.getServiceCode())).toList());
        model.addAttribute("restoPatients", allPatients.stream()
                .filter(p -> "Resto".equalsIgnoreCase(p.getServiceCode())).toList());
        model.addAttribute("prosthoPatients", allPatients.stream()
                .filter(p -> "CD".equalsIgnoreCase(p.getServiceCode()) || "RPD".equalsIgnoreCase(p.getServiceCode())
                        || "FPD".equalsIgnoreCase(p.getServiceCode())).toList());
        model.addAttribute("allPatients", allPatients);
        return "clinician-profile-clinicmanager";
    }

    @GetMapping("/admitting-clinicmanager")
    public String showAdmittingForm(Model model) {
        model.addAttribute("patient", new Patient());
        return "admitting-clinicmanager";
    }

    @PostMapping("/admitting-clinicmanager")
    public String submitAdmittingForm(@ModelAttribute Patient patient) {
        patientService.save(patient);
        return "redirect:/patientlist-clinicmanager";
    }

    @GetMapping("/patientlist-clinicmanager")
    public String patientList(Model model) {
        model.addAttribute("clinicianPatientCounts", patientService.getUniquePatientCountByClinician());
        // chart status tags of every admission, from all charts
        java.util.Map<Long, java.util.Map<String, Object>> chartRows = new java.util.HashMap<>();
        for (var r : com.dentis.DENTIS.service.PatientRowBuilder.build(
                oralSurgeryChartService.findAllOrderByCreatedAtDesc(), endodonticsChartService.findAllOrderByCreatedAtDesc(),
                periodonticChartService.findAllOrderByCreatedAtDesc(), operativeChartService.findAllOrderByCreatedAtDesc(),
                prosthodonticsChartService.findAllOrderByCreatedAtDesc())) {
            chartRows.put(((Patient) r.get("patient")).getId(), r);
        }
        // one row per admission (original or readmission), newest first
        List<java.util.Map<String, Object>> rows = new ArrayList<>();
        for (Patient p : patientService.getAllPatients()) {
            java.util.Map<String, Object> c = chartRows.get(p.getId());
            java.util.Map<String, Object> r = new java.util.HashMap<>();
            r.put("patient", p);
            r.put("section", sectionLabel(p.getServiceCode()));
            r.put("tags", c != null ? c.get("tags") : List.of());
            r.put("lastUpdated", c != null && c.get("lastUpdated") != null ? c.get("lastUpdated") : p.getCreatedAt());
            boolean needsFaculty = p.getAssignedFaculty() == null;
            boolean needsClinician = !needsFaculty && p.getAssignedClinician() == null;
            boolean done = c != null && Boolean.TRUE.equals(c.get("done"));
            r.put("needsFaculty", needsFaculty);
            r.put("needsClinician", needsClinician);
            r.put("done", done);
            r.put("inProgress", !needsFaculty && !needsClinician && !done);
            r.put("readmission", p.getOriginalPatient() != null);
            rows.add(r);
        }
        model.addAttribute("patientRows", rows);
        return "patientlist-clinicmanager";
    }

    /** Section of a service code, as used by the filters ("CD", "RPD", "FPD" are Prostho). */
    private static String sectionLabel(String code) {
        if (code == null) return "—";
        return switch (code.toUpperCase()) {
            case "CD", "RPD", "FPD" -> "Prostho";
            case "RESTO" -> "Resto";
            case "ENDO" -> "Endo";
            case "PERIO" -> "Perio";
            case "OS" -> "OS";
            case "PEDO" -> "Pedo";
            case "ORTHO" -> "Ortho";
            default -> code;
        };
    }

    @GetMapping("/chartsview-clinicmanager/{id}")
    public String chartsView(@PathVariable Long id, Model model) {
        Patient patient = patientService.getPatientById(id);
        model.addAttribute("patient", patient);
        model.addAttribute("facultyOptions", patientService.getFacultyBySection(patient.getServiceCode()));

        // Resolve the root patient (original visit)
        Patient root = patient.getOriginalPatient() != null ? patient.getOriginalPatient() : patient;

        // All recalls of the root patient, excluding the current patient
        List<Patient> siblingRecalls = patientService.getRecallsByOriginal(root)
                .stream().filter(r -> !r.getId().equals(patient.getId())).collect(Collectors.toList());
        model.addAttribute("rootPatient", root);
        model.addAttribute("isRecall", patient.getOriginalPatient() != null);
        model.addAttribute("siblingRecalls", siblingRecalls);

        // Faculty choices for each admission row (each admission can be for a different section)
        java.util.Map<Long, List<com.dentis.DENTIS.model.User>> facultyByAdmission = new java.util.HashMap<>();
        List<Patient> admissions = new ArrayList<>(siblingRecalls);
        admissions.add(patient);
        if (patient.getOriginalPatient() != null) admissions.add(root);
        for (Patient a : admissions) facultyByAdmission.put(a.getId(), patientService.getFacultyBySection(a.getServiceCode()));
        model.addAttribute("facultyByAdmission", facultyByAdmission);

        // Merge sub-charts from the current patient + root + all sibling recalls
        List<OralSurgeryChart> osCharts = new ArrayList<>(oralSurgeryChartService.findAllByPatient(patient));
        List<EndodonticsChart> endoCharts = new ArrayList<>(endodonticsChartService.findAllByPatient(patient));
        List<PeriodonticChart> perioCharts = new ArrayList<>(periodonticChartService.findAllByPatient(patient));
        List<OperativeChart> operativeCharts = new ArrayList<>(operativeChartService.findAllByPatient(patient));
        List<ProsthodonticsChart> prosthoCharts = new ArrayList<>(prosthodonticsChartService.findAllByPatient(patient));
        if (patient.getOriginalPatient() != null) {
            osCharts.addAll(oralSurgeryChartService.findAllByPatient(root));
            endoCharts.addAll(endodonticsChartService.findAllByPatient(root));
            perioCharts.addAll(periodonticChartService.findAllByPatient(root));
            operativeCharts.addAll(operativeChartService.findAllByPatient(root));
            prosthoCharts.addAll(prosthodonticsChartService.findAllByPatient(root));
        }
        for (Patient sibling : siblingRecalls) {
            osCharts.addAll(oralSurgeryChartService.findAllByPatient(sibling));
            endoCharts.addAll(endodonticsChartService.findAllByPatient(sibling));
            perioCharts.addAll(periodonticChartService.findAllByPatient(sibling));
            operativeCharts.addAll(operativeChartService.findAllByPatient(sibling));
            prosthoCharts.addAll(prosthodonticsChartService.findAllByPatient(sibling));
        }
        model.addAttribute("osCharts", osCharts);
        model.addAttribute("endoCharts", endoCharts);
        model.addAttribute("perioCharts", perioCharts);
        model.addAttribute("operativeCharts", operativeCharts);
        model.addAttribute("prosthoCharts", prosthoCharts);
        return "chartsview-clinicmanager";
    }

    @PostMapping("/chartsview-clinicmanager/{id}/assign-faculty")
    public String assignFaculty(@PathVariable Long id, @RequestParam Long facultyId,
                                @RequestParam(required = false) Long returnTo,
                                @RequestParam(required = false) String from) {
        patientService.assignFaculty(id, facultyId);
        if ("dashboard".equals(from)) return "redirect:/dashboard-clinicmanager?facultyAssigned=" + id;
        // stay on the charts view the clinic manager was on (it may list several admissions)
        return "redirect:/chartsview-clinicmanager/" + (returnTo != null ? returnTo : id);
    }

    @GetMapping("/readmit-clinicmanager/{id}")
    public String readmitForm(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("patient", patientService.getPatientById(id));
        model.addAttribute("today", java.time.LocalDate.now().toString());
        return "readmit-clinicmanager";
    }

    @PostMapping("/readmit-clinicmanager/{id}")
    public String readmitSubmit(@PathVariable Long id, @ModelAttribute Patient patient) {
        Patient recall = patientService.readmit(id, patient);
        return "redirect:/chartsview-clinicmanager/" + recall.getId();
    }

    @GetMapping("/admitting-view-clinicmanager/{id}")
    public String admittingView(@PathVariable Long id, Model model) {
        model.addAttribute("patient", patientService.getPatientById(id));
        return "admitting-view-clinicmanager";
    }

    @GetMapping("/admitting-edit-clinician/{id}")
    public String admittingEditForm(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("patient", patientService.getPatientById(id));
        return "admitting-edit-clinician";
    }

    @PostMapping("/admitting-edit-clinician/{id}")
    public String admittingEditSave(@PathVariable Long id, @ModelAttribute Patient patient) {
        patientService.update(id, patient);
        return "redirect:/chartsview-clinician/" + id;
    }
}
