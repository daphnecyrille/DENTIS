package com.dentis.DENTIS.controller;

import com.dentis.DENTIS.model.ChartRequest;
import com.dentis.DENTIS.model.Patient;
import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.repository.UserRepository;
import com.dentis.DENTIS.service.ChartRequestService;
import com.dentis.DENTIS.service.EndodonticsChartService;
import com.dentis.DENTIS.service.OperativeChartService;
import com.dentis.DENTIS.service.OralSurgeryChartService;
import com.dentis.DENTIS.service.PatientService;
import com.dentis.DENTIS.service.PeriodonticChartService;
import com.dentis.DENTIS.service.ProsthodonticsChartService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ChartRequestController {

    private final ChartRequestService chartRequestService;
    private final PatientService patientService;
    private final OralSurgeryChartService oralSurgeryChartService;
    private final EndodonticsChartService endodonticsChartService;
    private final OperativeChartService operativeChartService;
    private final PeriodonticChartService periodonticChartService;
    private final UserRepository userRepository;
    private final ProsthodonticsChartService prosthodonticsChartService;

    public ChartRequestController(ChartRequestService chartRequestService,
                                   OperativeChartService operativeChartService,
                                  PatientService patientService,
                                  OralSurgeryChartService oralSurgeryChartService,
                                  EndodonticsChartService endodonticsChartService,
                                  PeriodonticChartService periodonticChartService,
                                  UserRepository userRepository,
                                  ProsthodonticsChartService prosthodonticsChartService) {
        this.chartRequestService = chartRequestService;
        this.patientService = patientService;
        this.oralSurgeryChartService = oralSurgeryChartService;
        this.endodonticsChartService = endodonticsChartService;
        this.operativeChartService = operativeChartService;
        this.periodonticChartService = periodonticChartService;
        this.userRepository = userRepository;
        this.prosthodonticsChartService = prosthodonticsChartService;
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            String email = oAuth2User.getAttribute("email");
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }

    @GetMapping("/clinician-dashboard")
    public String clinicianDashboard(Model model, Authentication authentication) {
        User clinician = getCurrentUser(authentication);
        model.addAttribute("currentUser", clinician);
        if (clinician != null) {
            processApprovedRequests(clinician);
            for (Patient p : patientService.getPatientsForClinician(clinician)) {
                if ("Resto".equalsIgnoreCase(p.getServiceCode())
                        && operativeChartService.findAllByPatient(p).isEmpty()
                        && p.getAssignedFaculty() != null) {
                    operativeChartService.createForPatient(p, clinician, p.getAssignedFaculty());
                }
            }
        }
        model.addAttribute("operativeCharts", clinician != null
                ? operativeChartService.findByClinicianOrderByCreatedAtDesc(clinician).stream().limit(5).toList()
                : List.of());
        model.addAttribute("operativeActionNeededCharts", clinician != null
                ? operativeChartService.findForm1ActionNeededByClinician(clinician)
                : List.of());
        model.addAttribute("operative6ActionNeededCharts", clinician != null
                ? operativeChartService.findForm2ActionNeededByClinician(clinician)
                : List.of());
        model.addAttribute("actionNeededCharts", clinician != null
                ? oralSurgeryChartService.findActionNeededByClinician(clinician)
                : List.of());
        model.addAttribute("endoActionNeededCharts", clinician != null
                ? endodonticsChartService.findActionNeededByClinician(clinician)
                : List.of());
        model.addAttribute("endoForm2ActionNeededCharts", clinician != null
                ? endodonticsChartService.findForm2ActionNeededByClinician(clinician)
                : List.of());
        model.addAttribute("perioActionNeededCharts", clinician != null
                ? periodonticChartService.findFormABActionNeededByClinician(clinician)
                : List.of());
        model.addAttribute("periocActionNeededCharts", clinician != null
                ? periodonticChartService.findFormCActionNeededByClinician(clinician)
                : List.of());
        model.addAttribute("prosthoCharts", clinician != null
                ? prosthodonticsChartService.findByClinicianOrderByCreatedAtDesc(clinician).stream().limit(5).toList()
                : List.of());
        model.addAttribute("prosthoActionNeededCharts", clinician != null
                ? prosthodonticsChartService.findActionNeededByClinician(clinician)
                : List.of());
        model.addAttribute("requests", clinician != null
                ? chartRequestService.getClinicianRequests(clinician).stream().limit(5).toList()
                : List.of());
        model.addAttribute("recentPatients", clinician != null
                ? patientService.getPatientsForClinician(clinician).stream().limit(5).toList()
                : List.of());
        return "dashboard-clinician";
    }

    private void processApprovedRequests(User clinician) {
        for (ChartRequest req : chartRequestService.getApprovedRequestsForClinician(clinician)) {
            Patient patient = req.getPatient();
            if (patient == null) {
                patient = chartRequestService.resolvePatient(req.getChartNo());
            }
            if (patient == null) continue;
            User faculty = patient.getAssignedFaculty();
            String section = req.getSection() != null ? req.getSection().toUpperCase() : "";
            switch (section) {
                case "ENDO" -> {
                    if (endodonticsChartService.findAllByPatient(patient).isEmpty())
                        endodonticsChartService.createForPatient(patient, clinician, faculty);
                }
                case "OS" -> {
                    if (oralSurgeryChartService.findAllByPatient(patient).isEmpty())
                        oralSurgeryChartService.createForPatient(patient, clinician, faculty,
                                com.dentis.DENTIS.model.OralSurgeryChartType.OSF1);
                }
                case "PERIO" -> {
                    if (periodonticChartService.findAllByPatient(patient).isEmpty())
                        periodonticChartService.createForPatient(patient, clinician, faculty);
                }
                case "RESTO" -> {
                    if (operativeChartService.findAllByPatient(patient).isEmpty())
                        operativeChartService.createForPatient(patient, clinician, faculty);
                }
                case "CD", "RPD", "FPD" -> {
                    if (prosthodonticsChartService.findAllByPatient(patient).isEmpty())
                        prosthodonticsChartService.createForPatient(patient, clinician, faculty);
                }
                default -> {}
            }
        }
    }

    private List<com.dentis.DENTIS.model.Patient> getUniquePatientsForClinician(User clinician) {
        return oralSurgeryChartService.findByClinicianOrderByCreatedAtDesc(clinician)
                .stream()
                .map(c -> c.getPatient())
                .distinct()
                .toList();
    }

    @GetMapping("/chartrequest-clinician")
    public String showForm(Model model, Authentication authentication) {
        User clinician = getCurrentUser(authentication);
        model.addAttribute("chartRequest", new ChartRequest());
        model.addAttribute("currentUser", clinician);
        model.addAttribute("requests", clinician != null
                ? chartRequestService.getClinicianRequests(clinician)
                : List.of());
        return "chartrequest-clinician";
    }

    @PostMapping("/chartrequest-clinician")
    public String submitRequest(@ModelAttribute ChartRequest chartRequest,
                                Authentication authentication) {
        User clinician = getCurrentUser(authentication);
        chartRequestService.submit(chartRequest, clinician != null ? clinician.getId() : null);
        return "redirect:/chartrequest-clinician";
    }

    @GetMapping("/requestlist-clinicmanager")
    public String requestList(Model model) {
        model.addAttribute("requests", chartRequestService.getPendingRequests());
        return "requestlist-clinicmanager";
    }

    @PostMapping("/requestlist-clinicmanager/{id}/approve")
    public String approve(@PathVariable Long id) {
        chartRequestService.approve(id);
        com.dentis.DENTIS.model.ChartRequest req = chartRequestService.getRequestById(id);
        com.dentis.DENTIS.model.Patient patient = req.getPatient();
        com.dentis.DENTIS.model.User clinician = req.getClinician();
        if (patient != null && clinician != null) {
            com.dentis.DENTIS.model.User faculty = patient.getAssignedFaculty();
            String section = req.getSection() != null ? req.getSection().toUpperCase() : "";
            switch (section) {
                case "ENDO" -> {
                    if (endodonticsChartService.findAllByPatient(patient).isEmpty())
                        endodonticsChartService.createForPatient(patient, clinician, faculty);
                }
                case "OS" -> {
                    if (oralSurgeryChartService.findAllByPatient(patient).isEmpty())
                        oralSurgeryChartService.createForPatient(patient, clinician, faculty,
                                com.dentis.DENTIS.model.OralSurgeryChartType.OSF1);
                }
                case "PERIO" -> {
                    if (periodonticChartService.findAllByPatient(patient).isEmpty())
                        periodonticChartService.createForPatient(patient, clinician, faculty);
                }
                case "RESTO" -> {
                    if (operativeChartService.findAllByPatient(patient).isEmpty())
                        operativeChartService.createForPatient(patient, clinician, faculty);
                }
                case "CD", "RPD", "FPD" -> {
                    if (prosthodonticsChartService.findAllByPatient(patient).isEmpty())
                        prosthodonticsChartService.createForPatient(patient, clinician, faculty);
                }
                default -> {}
            }
        }
        return "redirect:/requestlist-clinicmanager";
    }

    @PostMapping("/requestlist-clinicmanager/{id}/deny")
    public String deny(@PathVariable Long id) {
        chartRequestService.deny(id);
        return "redirect:/requestlist-clinicmanager";
    }

    @GetMapping("/patientlist-clinician")
    public String patientList(Model model, Authentication authentication) {
        User clinician = getCurrentUser(authentication);
        model.addAttribute("currentUser", clinician);
        if (clinician != null) {
            processApprovedRequests(clinician);
            for (Patient p : patientService.getPatientsForClinician(clinician)) {
                if ("Resto".equalsIgnoreCase(p.getServiceCode())
                        && operativeChartService.findAllByPatient(p).isEmpty()
                        && p.getAssignedFaculty() != null) {
                    operativeChartService.createForPatient(p, clinician, p.getAssignedFaculty());
                }
            }
        }
        java.util.List<com.dentis.DENTIS.model.OralSurgeryChart> osCharts = clinician != null
                ? new java.util.ArrayList<>(oralSurgeryChartService.findByClinicianOrderByCreatedAtDesc(clinician))
                : new java.util.ArrayList<>();
        java.util.List<com.dentis.DENTIS.model.EndodonticsChart> endoCharts = clinician != null
                ? new java.util.ArrayList<>(endodonticsChartService.findByClinicianOrderByCreatedAtDesc(clinician))
                : new java.util.ArrayList<>();
        java.util.List<com.dentis.DENTIS.model.PeriodonticChart> perioCharts = clinician != null
                ? new java.util.ArrayList<>(periodonticChartService.findByClinicianOrderByCreatedAtDesc(clinician))
                : new java.util.ArrayList<>();
        java.util.List<com.dentis.DENTIS.model.OperativeChart> operativeCharts = clinician != null
                ? new java.util.ArrayList<>(operativeChartService.findByClinicianOrderByCreatedAtDesc(clinician))
                : new java.util.ArrayList<>();
        java.util.List<com.dentis.DENTIS.model.ProsthodonticsChart> prosthoCharts = clinician != null
                ? new java.util.ArrayList<>(prosthodonticsChartService.findByClinicianOrderByCreatedAtDesc(clinician))
                : new java.util.ArrayList<>();

        if (clinician != null) {
            for (ChartRequest req : chartRequestService.getApprovedRequestsForClinician(clinician)) {
                Patient rp = req.getPatient();
                if (rp == null) rp = chartRequestService.resolvePatient(req.getChartNo());
                if (rp == null) continue;
                final Patient reqPatient = rp;
                String section = req.getSection() != null ? req.getSection().toUpperCase() : "";
                switch (section) {
                    case "ENDO" -> endodonticsChartService.findAllByPatient(reqPatient).stream()
                            .filter(c -> endoCharts.stream().noneMatch(e -> e.getPatient().equals(reqPatient)))
                            .findFirst().ifPresent(endoCharts::add);
                    case "OS" -> oralSurgeryChartService.findAllByPatient(reqPatient).stream()
                            .filter(c -> osCharts.stream().noneMatch(e -> e.getPatient().equals(reqPatient)))
                            .findFirst().ifPresent(osCharts::add);
                    case "PERIO" -> periodonticChartService.findAllByPatient(reqPatient).stream()
                            .filter(c -> perioCharts.stream().noneMatch(e -> e.getPatient().equals(reqPatient)))
                            .findFirst().ifPresent(perioCharts::add);
                    case "RESTO" -> operativeChartService.findAllByPatient(reqPatient).stream()
                            .filter(c -> operativeCharts.stream().noneMatch(e -> e.getPatient().equals(reqPatient)))
                            .findFirst().ifPresent(operativeCharts::add);
                    case "CD", "RPD", "FPD" -> prosthodonticsChartService.findAllByPatient(reqPatient).stream()
                            .filter(c -> prosthoCharts.stream().noneMatch(e -> e.getPatient().equals(reqPatient)))
                            .findFirst().ifPresent(prosthoCharts::add);
                    default -> {}
                }
            }
        }

        model.addAttribute("osCharts", osCharts);
        model.addAttribute("endoCharts", endoCharts);
        model.addAttribute("perioCharts", perioCharts);
        model.addAttribute("operativeCharts", operativeCharts);
        model.addAttribute("prosthoCharts", prosthoCharts);
        return "patientlist-clinician";
    }

    @GetMapping("/admitting-view-clinician/{id}")
    public String admittingViewClinician(@PathVariable Long id, Model model) {
        model.addAttribute("patient", chartRequestService.getPatientById(id));
        return "admitting-view-clinician";
    }

    @GetMapping("/chartsview-clinician/{id}")
    public String chartsViewClinician(@PathVariable Long id, Model model, Authentication authentication) {
        User clinician = getCurrentUser(authentication);
        Patient patient = patientService.getPatientById(id);
        model.addAttribute("currentUser", clinician);
        model.addAttribute("patient", patient);
        model.addAttribute("approvedCharts", clinician != null
                ? chartRequestService.getApprovedRequestsForClinicianAndPatient(clinician, id)
                : List.of());
        model.addAttribute("osCharts", oralSurgeryChartService.findAllByPatient(patient));
        model.addAttribute("endoCharts", endodonticsChartService.findAllByPatient(patient));
        model.addAttribute("perioCharts", periodonticChartService.findAllByPatient(patient));
        if ("Resto".equalsIgnoreCase(patient.getServiceCode())
                && operativeChartService.findAllByPatient(patient).isEmpty()
                && patient.getAssignedClinician() != null
                && patient.getAssignedFaculty() != null) {
            operativeChartService.createForPatient(patient, patient.getAssignedClinician(), patient.getAssignedFaculty());
        }
        model.addAttribute("operativeCharts", operativeChartService.findAllByPatient(patient));
        if (("CD".equalsIgnoreCase(patient.getServiceCode()) || "RPD".equalsIgnoreCase(patient.getServiceCode()))
                && prosthodonticsChartService.findAllByPatient(patient).isEmpty()
                && patient.getAssignedClinician() != null
                && patient.getAssignedFaculty() != null) {
            prosthodonticsChartService.createForPatient(patient, patient.getAssignedClinician(), patient.getAssignedFaculty());
        }
        model.addAttribute("prosthoCharts", prosthodonticsChartService.findAllByPatient(patient));
        return "chartsview-clinician";
    }
}
