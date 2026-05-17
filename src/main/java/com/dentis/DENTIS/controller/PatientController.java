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
    public String dashboard(Model model) {
        model.addAttribute("patients", patientService.getAllPatients().stream().limit(5).toList());
        model.addAttribute("requests", chartRequestService.getPendingRequests().stream().limit(5).toList());
        model.addAttribute("statTotalPatients", patientService.countAll());
        model.addAttribute("statOS", patientService.countBySection("OS"));
        model.addAttribute("statENDO", patientService.countBySection("ENDO"));
        model.addAttribute("statPERIO", patientService.countBySection("PERIO"));
        model.addAttribute("statRESTO", patientService.countBySection("Resto"));
        model.addAttribute("statPROSTHO", patientService.countBySection("CD") + patientService.countBySection("RPD"));
        model.addAttribute("statPendingRequests", chartRequestService.getPendingRequests().size());
        model.addAttribute("statClinicians", patientService.getAllClinicians().size());
        model.addAttribute("clinicianPatientCounts", patientService.getUniquePatientCountByClinician());
        return "dashboard-clinicmanager";
    }

    @GetMapping("/assign-clinicmanager")
    public String assignClinicians(Model model) {
        model.addAttribute("assignmentRows", assignmentService.getAssignmentRows());
        model.addAttribute("osFaculty", patientService.getFacultyBySection("OS"));
        model.addAttribute("endoFaculty", patientService.getFacultyBySection("ENDO"));
        model.addAttribute("perioFaculty", patientService.getFacultyBySection("PERIO"));
        model.addAttribute("restoFaculty", patientService.getFacultyBySection("Resto"));
        model.addAttribute("prosthoFaculty", patientService.getFacultyBySection("CD"));
        return "assign-clinicmanager";
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
                .filter(p -> "CD".equalsIgnoreCase(p.getServiceCode()) || "RPD".equalsIgnoreCase(p.getServiceCode())).toList());
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
        model.addAttribute("patients", patientService.getAllPatients());
        model.addAttribute("clinicianPatientCounts", patientService.getUniquePatientCountByClinician());
        return "patientlist-clinicmanager";
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
    public String assignFaculty(@PathVariable Long id, @RequestParam Long facultyId) {
        patientService.assignFaculty(id, facultyId);
        return "redirect:/chartsview-clinicmanager/" + id;
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
