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

    public FacultyController(PatientService patientService,
                             OralSurgeryChartService oralSurgeryChartService,
                             EndodonticsChartService endodonticsChartService,
                             OperativeChartService operativeChartService,
                             PeriodonticChartService periodonticChartService,
                             UserRepository userRepository,
                             ClinicianFacultyAssignmentService assignmentService) {
        this.patientService = patientService;
        this.oralSurgeryChartService = oralSurgeryChartService;
        this.endodonticsChartService = endodonticsChartService;
        this.operativeChartService = operativeChartService;
        this.periodonticChartService = periodonticChartService;
        this.userRepository = userRepository;
        this.assignmentService = assignmentService;
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            String email = oAuth2User.getAttribute("email");
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }

    @GetMapping("/faculty-dashboard")
    public String dashboard(Model model, Authentication authentication) {
        User faculty = getCurrentUser(authentication);
        model.addAttribute("currentUser", faculty);

        String section = faculty != null && faculty.getSection() != null
                ? faculty.getSection().toUpperCase() : "";

        model.addAttribute("osCharts", "OS".equals(section) && faculty != null
                ? oralSurgeryChartService.findByFacultyOrderByCreatedAtDesc(faculty).stream().limit(5).toList()
                : List.of());
        model.addAttribute("endoCharts", "ENDO".equals(section) && faculty != null
                ? endodonticsChartService.findByFacultyOrderByCreatedAtDesc(faculty).stream().limit(5).toList()
                : List.of());
        model.addAttribute("perioCharts", "PERIO".equals(section) && faculty != null
                ? periodonticChartService.findByFacultyOrderByCreatedAtDesc(faculty).stream().limit(5).toList()
                : List.of());
        model.addAttribute("operativeCharts", "RESTO".equals(section) && faculty != null
                ? operativeChartService.findByFacultyOrderByCreatedAtDesc(faculty).stream().limit(5).toList()
                : List.of());
        model.addAttribute("awaitingApprovalCharts", "OS".equals(section) && faculty != null
                ? oralSurgeryChartService.findAwaitingApprovalByFaculty(faculty)
                : List.of());
        model.addAttribute("awaitingApprovalEndoCharts", "ENDO".equals(section) && faculty != null
                ? endodonticsChartService.findAwaitingApprovalByFaculty(faculty)
                : List.of());
        model.addAttribute("awaitingApprovalEndo2Charts", "ENDO".equals(section) && faculty != null
                ? endodonticsChartService.findAwaitingApproval2ByFaculty(faculty)
                : List.of());
        model.addAttribute("awaitingApprovalPerioCharts", "PERIO".equals(section) && faculty != null
                ? periodonticChartService.findFormABAwaitingApprovalByFaculty(faculty)
                : List.of());
        model.addAttribute("awaitingApprovalPerio2Charts", "PERIO".equals(section) && faculty != null
                ? periodonticChartService.findFormCAwaitingApprovalByFaculty(faculty)
                : List.of());
        model.addAttribute("awaitingApprovalOperative10Charts", "RESTO".equals(section) && faculty != null
                ? operativeChartService.findForm1AwaitingApprovalByFaculty(faculty)
                : List.of());
        model.addAttribute("awaitingApprovalOperative6Charts", "RESTO".equals(section) && faculty != null
                ? operativeChartService.findForm2AwaitingApprovalByFaculty(faculty)
                : List.of());
        model.addAttribute("unassignedPatients", faculty != null
                ? patientService.getUnassignedPatientsForFaculty(faculty).stream().limit(5).toList()
                : List.of());
        model.addAttribute("clinicians", patientService.getAllClinicians());
        return "dashboard-faculty";
    }

    @GetMapping("/faculty-patientlist")
    public String patientList(Model model, Authentication authentication) {
        User faculty = getCurrentUser(authentication);
        model.addAttribute("currentUser", faculty);

        String section = faculty != null && faculty.getSection() != null
                ? faculty.getSection().toUpperCase() : "";

        model.addAttribute("osCharts", "OS".equals(section) && faculty != null
                ? oralSurgeryChartService.findByFacultyOrderByCreatedAtDesc(faculty)
                : List.of());
        model.addAttribute("endoCharts", "ENDO".equals(section) && faculty != null
                ? endodonticsChartService.findByFacultyOrderByCreatedAtDesc(faculty)
                : List.of());
        model.addAttribute("perioCharts", "PERIO".equals(section) && faculty != null
                ? periodonticChartService.findByFacultyOrderByCreatedAtDesc(faculty)
                : List.of());
        model.addAttribute("operativeCharts", "RESTO".equals(section) && faculty != null
                ? operativeChartService.findByFacultyOrderByCreatedAtDesc(faculty)
                : List.of());
        return "patientlist-faculty";
    }

    @GetMapping("/faculty-advisees")
    public String advisees(Model model, Authentication authentication) {
        User faculty = getCurrentUser(authentication);
        model.addAttribute("currentUser", faculty);
        String section = faculty != null && faculty.getSection() != null
                ? faculty.getSection() : "";
        List<User> advisees = faculty != null
                ? assignmentService.getAdviseesForFaculty(faculty, section)
                : List.of();
        Map<Long, List<Patient>> patientsMap = new LinkedHashMap<>();
        for (User advisee : advisees) {
            patientsMap.put(advisee.getId(), patientService.getPatientsForClinician(advisee));
        }
        model.addAttribute("advisees", advisees);
        model.addAttribute("patientsMap", patientsMap);
        model.addAttribute("section", section);
        return "faculty-advisees";
    }

    @GetMapping("/faculty-assign")
    public String assignPatients(Model model, Authentication authentication) {
        User faculty = getCurrentUser(authentication);
        model.addAttribute("currentUser", faculty);
        model.addAttribute("patients", faculty != null
                ? patientService.getUnassignedPatientsForFaculty(faculty)
                : List.of());
        model.addAttribute("clinicians", patientService.getAllClinicians());
        return "assign-patients-faculty";
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
        return "chartsview-faculty";
    }

    @GetMapping("/admitting-view-faculty/{id}")
    public String admittingView(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("patient", patientService.getPatientById(id));
        return "admitting-view-faculty";
    }

    @PostMapping("/oralsurgery-faculty/{id}/approve")
    public String approveChart(@PathVariable Long id) {
        OralSurgeryChart chart = oralSurgeryChartService.approve(id);
        return "redirect:/chartsview-faculty/" + chart.getPatient().getId();
    }

    @PostMapping("/oralsurgery-faculty/{id}/revise")
    public String reviseChart(@PathVariable Long id) {
        OralSurgeryChart chart = oralSurgeryChartService.revise(id);
        return "redirect:/chartsview-faculty/" + chart.getPatient().getId();
    }

    @PostMapping("/faculty-dashboard/{id}/assign-clinician")
    public String assignClinician(@PathVariable Long id,
                                  @RequestParam Long clinicianId,
                                  @RequestParam(required = false, defaultValue = "faculty-assign") String returnTo,
                                  Authentication authentication) {
        User faculty = getCurrentUser(authentication);
        patientService.assignClinician(id, clinicianId, faculty);
        return "redirect:/" + returnTo;
    }
}
