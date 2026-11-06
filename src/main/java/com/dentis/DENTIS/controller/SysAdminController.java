package com.dentis.DENTIS.controller;

import com.dentis.DENTIS.model.AccountStatus;
import com.dentis.DENTIS.model.Role;
import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;

@Controller
public class SysAdminController {

    private final UserRepository userRepository;
    private final com.dentis.DENTIS.service.ActivityLogService activity;
    private final com.dentis.DENTIS.service.PatientService patientService;
    private final com.dentis.DENTIS.service.ChartRequestService chartRequestService;
    private final com.dentis.DENTIS.service.OralSurgeryChartService osService;
    private final com.dentis.DENTIS.service.EndodonticsChartService endoService;
    private final com.dentis.DENTIS.service.PeriodonticChartService perioService;
    private final com.dentis.DENTIS.service.OperativeChartService operativeService;
    private final com.dentis.DENTIS.service.ProsthodonticsChartService prosthoService;

    public SysAdminController(UserRepository userRepository,
                              com.dentis.DENTIS.service.ActivityLogService activity,
                              com.dentis.DENTIS.service.PatientService patientService,
                              com.dentis.DENTIS.service.ChartRequestService chartRequestService,
                              com.dentis.DENTIS.service.OralSurgeryChartService osService,
                              com.dentis.DENTIS.service.EndodonticsChartService endoService,
                              com.dentis.DENTIS.service.PeriodonticChartService perioService,
                              com.dentis.DENTIS.service.OperativeChartService operativeService,
                              com.dentis.DENTIS.service.ProsthodonticsChartService prosthoService) {
        this.userRepository = userRepository;
        this.activity = activity;
        this.patientService = patientService;
        this.chartRequestService = chartRequestService;
        this.osService = osService;
        this.endoService = endoService;
        this.perioService = perioService;
        this.operativeService = operativeService;
        this.prosthoService = prosthoService;
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null) return null;
        String email = ((OAuth2User) authentication.getPrincipal()).getAttribute("email");
        return userRepository.findByEmail(email).orElse(null);
    }

    @GetMapping("/dashboard-sysadmin")
    public String dashboard(Model model, Authentication authentication,
                            @RequestParam(value = "msg", required = false) String msg,
                            @RequestParam(value = "err", required = false) String err) {
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("msg", msg);   // confirmation after create / edit / activate / deactivate
        model.addAttribute("err", err);

        List<User> users = userRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(User::getLastName))
                .toList();

        long totalUsers    = users.size();
        long activeUsers   = users.stream().filter(u -> u.getStatus() == AccountStatus.ACTIVE).count();
        long clinicians    = users.stream().filter(u -> u.getRole() == Role.CLINICIAN).count();
        long faculty       = users.stream().filter(u -> u.getRole() == Role.FACULTY).count();
        long managers      = users.stream().filter(u -> u.getRole() == Role.CLINIC_MANAGER).count();

        model.addAttribute("users",          users);
        model.addAttribute("statTotalUsers", totalUsers);
        model.addAttribute("statActiveUsers",activeUsers);
        model.addAttribute("statClinicians", clinicians);
        model.addAttribute("statFaculty",    faculty);
        model.addAttribute("statManagers",   managers);
        model.addAttribute("statInactive",   totalUsers - activeUsers);
        model.addAttribute("statAdmins",     users.stream().filter(u -> u.getRole() == Role.ADMIN).count());
        model.addAttribute("statNeverSignedIn", users.stream().filter(u -> u.getLastLoginAt() == null && u.getStatus() == AccountStatus.ACTIVE).count());
        // everyone below 6th year moves up one; 6th years (graduating or delayed) stay
        model.addAttribute("movingUp", users.stream().filter(u -> u.getRole() == Role.CLINICIAN && u.getStatus() == AccountStatus.ACTIVE && nextYear(u.getYearLevel()) != null)
                .sorted(Comparator.comparing((User u) -> u.getYearLevel()).thenComparing(User::getLastName)).toList());
        model.addAttribute("graduating", users.stream().filter(u -> u.getRole() == Role.CLINICIAN && u.getStatus() == AccountStatus.ACTIVE && FINAL_YEAR.equals(u.getYearLevel())).toList());
        // accounts missing something the system needs (faculty section, clinician student no. / year level, name)
        model.addAttribute("statNeedsSetup", users.stream().filter(u -> !setupIssues(u).isEmpty()).count());
        java.util.Map<Long, String> issues = new java.util.HashMap<>();
        for (User u : users) issues.put(u.getId(), String.join(", ", setupIssues(u)));
        model.addAttribute("setupIssues", issues);

        return "dashboard-sysadmin";
    }

    /** What an account still needs before it works fully (empty = complete). */
    static List<String> setupIssues(User u) {
        List<String> out = new java.util.ArrayList<>();
        if (u.getFirstName() == null || u.getFirstName().isBlank()) out.add("no name");
        if (u.getRole() == Role.FACULTY && (u.getSection() == null || u.getSection().isBlank())) out.add("no section");
        if (u.getRole() == Role.CLINICIAN) {
            if (u.getStudentNumber() == null || u.getStudentNumber().isBlank()) out.add("no student no.");
            if (u.getYearLevel() == null || u.getYearLevel().isBlank()) out.add("no year level");
        }
        return out;
    }

    private static String q(String s) { return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8); }

    @PostMapping("/create-user-sysadmin")
    public String createUser(@RequestParam String email,
                             @RequestParam String role,
                             @RequestParam(required = false) String firstName,
                             @RequestParam(required = false) String lastName,
                             @RequestParam(required = false) String section) {
        String mail = email.trim().toLowerCase();
        if (userRepository.findByEmail(mail).isPresent()) return "redirect:/dashboard-sysadmin?err=" + q("An account with " + mail + " already exists.");
        if ("FACULTY".equals(role) && (section == null || section.isBlank())) return "redirect:/dashboard-sysadmin?err=" + q("Pick a section for the faculty account.");
        User user = new User();
        user.setFirstName(firstName != null && !firstName.isBlank() ? firstName.trim() : "");
        user.setLastName(lastName != null && !lastName.isBlank() ? lastName.trim() : "");
        user.setEmail(email.trim().toLowerCase());
        user.setRole(Role.valueOf(role));
        user.setStatus(AccountStatus.ACTIVE);
        if (section != null && !section.isBlank()) user.setSection(section.trim());
        userRepository.save(user);
        return "redirect:/dashboard-sysadmin?msg=" + q("Account created for " + mail + ".");
    }

    @PostMapping("/edit-user-sysadmin")
    public String editUser(Authentication authentication,
                           @RequestParam Long userId,
                           @RequestParam String firstName,
                           @RequestParam String lastName,
                           @RequestParam String email,
                           @RequestParam String role,
                           @RequestParam(required = false) String section,
                           @RequestParam(required = false) String yearLevel,
                           @RequestParam(required = false) String studentNumber) {
        User user = userRepository.findById(userId).orElseThrow();
        String mail = email.trim().toLowerCase();
        var other = userRepository.findByEmail(mail);
        if (other.isPresent() && !other.get().getId().equals(userId)) return "redirect:/dashboard-sysadmin?err=" + q("Another account already uses " + mail + ".");
        if ("FACULTY".equals(role) && (section == null || section.isBlank())) return "redirect:/dashboard-sysadmin?err=" + q("Pick a section for the faculty account.");
        User me = getCurrentUser(authentication);
        if (me != null && me.getId().equals(userId) && !"ADMIN".equals(role))
            return "redirect:/dashboard-sysadmin?err=" + q("You can't remove the Admin role from your own account.");
        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setRole(Role.valueOf(role));
        user.setSection((section != null && !section.isBlank()) ? section.trim() : null);
        user.setYearLevel((yearLevel != null && !yearLevel.isBlank()) ? yearLevel.trim() : null);
        user.setStudentNumber((studentNumber != null && !studentNumber.isBlank()) ? studentNumber.trim() : null);
        userRepository.save(user);
        return "redirect:/dashboard-sysadmin?msg=" + q("Changes saved for " + user.getFullName() + ".");
    }

    @PostMapping("/toggle-status-sysadmin")
    public String toggleStatus(Authentication authentication,
                               @RequestParam Long userId,
                               @RequestParam String action) {
        User user = userRepository.findById(userId).orElseThrow();
        User me = getCurrentUser(authentication);
        if (!"activate".equals(action) && me != null && me.getId().equals(userId))
            return "redirect:/dashboard-sysadmin?err=" + q("You can't deactivate your own account.");
        user.setStatus("activate".equals(action) ? AccountStatus.ACTIVE : AccountStatus.INACTIVE);
        userRepository.save(user);
        return "redirect:/dashboard-sysadmin?msg=" + q(user.getFullName() + ("activate".equals(action) ? " can sign in again." : " was deactivated and can no longer sign in."));
    }

    // ── Activity Log ─────────────────────────────────────────────────────────

    @GetMapping("/activity-sysadmin")
    public String activityLog(Model model, Authentication authentication,
                              @RequestParam(value = "days", defaultValue = "7") int days) {
        model.addAttribute("currentUser", getCurrentUser(authentication));
        int d = java.util.List.of(1, 7, 30, 90, 365).contains(days) ? days : 7;
        model.addAttribute("days", d);
        model.addAttribute("adminPage", "activity");
        model.addAttribute("entries", activity.since(d));
        return "activity-sysadmin";
    }

    // ── Bulk import (CSV is read in the browser; the rows arrive here as JSON) ──

    @PostMapping("/import-users-sysadmin")
    public String importUsers(@RequestParam("rows") String rowsJson, Authentication authentication) {
        List<java.util.Map<String, String>> rows;
        try {
            rows = new com.fasterxml.jackson.databind.ObjectMapper().readValue(rowsJson,
                    new com.fasterxml.jackson.core.type.TypeReference<List<java.util.Map<String, String>>>() { });
        } catch (Exception e) {
            return "redirect:/dashboard-sysadmin?err=" + q("The file could not be read. Please use the template.");
        }
        int created = 0, skipped = 0;
        for (var r : rows) {
            String email = trim(r.get("email")).toLowerCase();
            String role = trim(r.get("role")).toUpperCase().replace(' ', '_');
            if (email.isEmpty() || !email.contains("@") || userRepository.findByEmail(email).isPresent()) { skipped++; continue; }
            if (!java.util.Set.of("CLINICIAN", "FACULTY", "CLINIC_MANAGER", "ADMIN").contains(role)) { skipped++; continue; }
            String section = trim(r.get("section"));
            if ("FACULTY".equals(role) && section.isEmpty()) { skipped++; continue; }
            User u = new User();
            u.setEmail(email);
            u.setFirstName(trim(r.get("firstName")));
            u.setLastName(trim(r.get("lastName")));
            u.setRole(Role.valueOf(role));
            u.setStatus(AccountStatus.ACTIVE);
            if (!section.isEmpty()) u.setSection(section);
            if ("CLINICIAN".equals(role)) {
                String y = trim(r.get("yearLevel"));
                if (!y.isEmpty()) u.setYearLevel(y);
                String sn = trim(r.get("studentNumber"));
                if (!sn.isEmpty()) u.setStudentNumber(sn);
            }
            userRepository.save(u);
            created++;
        }
        activity.record(getCurrentUser(authentication), "ACCOUNT", "Imported accounts from a file",
                created + " created" + (skipped > 0 ? ", " + skipped + " skipped" : ""));
        return "redirect:/dashboard-sysadmin?msg=" + q(created + (created == 1 ? " account" : " accounts") + " created"
                + (skipped > 0 ? ", " + skipped + " skipped (already exist or missing details)." : "."));
    }

    private static String trim(String s) { return s == null ? "" : s.trim(); }

    // ── End of semester ──────────────────────────────────────────────────────

    static final List<String> YEARS = List.of("1st-Year", "2nd-Year", "3rd-Year", "4th-Year", "5th-Year", "6th-Year");
    static final String FINAL_YEAR = "6th-Year";

    /** The year after this one, or null for 6th year / unknown. */
    static String nextYear(String y) {
        int i = YEARS.indexOf(y);
        return (i < 0 || i == YEARS.size() - 1) ? null : YEARS.get(i + 1);
    }

    /** Moves the ticked clinicians up one year level (1st to 2nd ... 5th to 6th). */
    @PostMapping("/semester-promote-sysadmin")
    public String promote(@RequestParam(value = "userIds", required = false) List<Long> userIds,
                          Authentication authentication) {
        int n = 0;
        if (userIds != null) for (Long id : userIds) {
            User u = userRepository.findById(id).orElse(null);
            if (u == null || u.getRole() != Role.CLINICIAN || u.getStatus() != AccountStatus.ACTIVE) continue;
            String next = nextYear(u.getYearLevel());
            if (next == null) continue;
            u.setYearLevel(next);
            userRepository.save(u);
            n++;
        }
        activity.record(getCurrentUser(authentication), "ACCOUNT", "Moved clinicians up a year", n + " clinicians moved up one year level");
        return "redirect:/dashboard-sysadmin?msg=" + q(n + (n == 1 ? " clinician" : " clinicians") + " moved up one year level.");
    }

    /** Deactivates the chosen (graduating) clinicians; their charts stay in the records. */
    @PostMapping("/semester-deactivate-sysadmin")
    public String deactivateGraduates(@RequestParam(value = "userIds", required = false) List<Long> userIds,
                                      Authentication authentication) {
        User me = getCurrentUser(authentication);
        int n = 0;
        if (userIds != null) for (Long id : userIds) {
            User u = userRepository.findById(id).orElse(null);
            if (u == null || u.getRole() != Role.CLINICIAN || (me != null && me.getId().equals(id))) continue;
            u.setStatus(AccountStatus.INACTIVE);
            userRepository.save(u);
            n++;
        }
        activity.record(me, "ACCOUNT", "Deactivated graduating clinicians", n + " clinicians");
        return "redirect:/dashboard-sysadmin?msg=" + q(n + (n == 1 ? " clinician" : " clinicians") + " deactivated. Their charts stay in the records.");
    }

    // ── Clinic Overview ──────────────────────────────────────────────────────

    /** One chart form, for the overview. */
    public record Form(String label, String chartNo, String patient, String clinician, String faculty, Long facultyId,
                       String status, java.time.LocalDateTime when) { }

    @GetMapping("/overview-sysadmin")
    public String overview(Model model, Authentication authentication) {
        model.addAttribute("currentUser", getCurrentUser(authentication));
        model.addAttribute("adminPage", "overview");
        List<Form> forms = allForms();
        List<Form> waiting = forms.stream().filter(f -> "SUBMITTED".equals(f.status()))
                .sorted(Comparator.comparing(Form::when, Comparator.nullsLast(Comparator.naturalOrder()))).toList();
        model.addAttribute("waiting", waiting.stream().limit(10).toList());
        model.addAttribute("waitingCount", waiting.size());
        model.addAttribute("reviseCount", forms.stream().filter(f -> "REVISE".equals(f.status())).count());
        model.addAttribute("progressCount", forms.stream().filter(f -> "CREATED".equals(f.status()) || "IN_PROGRESS".equals(f.status())).count());
        model.addAttribute("approvedCount", forms.stream().filter(f -> "APPROVED".equals(f.status())).count());

        // waiting per faculty, most first, with the oldest wait
        java.util.Map<String, List<Form>> byFac = new java.util.TreeMap<>();
        for (Form f : waiting) byFac.computeIfAbsent(f.faculty() != null ? f.faculty() : "No faculty", k -> new java.util.ArrayList<>()).add(f);
        List<java.util.Map<String, Object>> facRows = new java.util.ArrayList<>();
        byFac.forEach((name, list) -> facRows.add(java.util.Map.of("name", name, "count", list.size(),
                "oldest", list.get(0).when() != null ? list.get(0).when() : java.time.LocalDateTime.now())));
        facRows.sort((a, b) -> Integer.compare((Integer) b.get("count"), (Integer) a.get("count")));
        model.addAttribute("facultyRows", facRows);

        // patients per section (admissions) and clinicians per year
        java.util.Map<String, Long> sections = new java.util.LinkedHashMap<>();
        for (String s : List.of("OS", "Endo", "Perio", "Resto", "Prostho")) sections.put(s, 0L);
        for (var p : patientService.getAllPatients()) {
            String sec = sectionOf(p.getServiceCode());
            if (sections.containsKey(sec)) sections.merge(sec, 1L, Long::sum);
        }
        model.addAttribute("sections", sections);
        long maxSec = sections.values().stream().mapToLong(Long::longValue).max().orElse(0);
        model.addAttribute("maxSection", Math.max(maxSec, 1));
        java.util.Map<String, Long> years = new java.util.TreeMap<>();
        for (User u : userRepository.findAll())
            if (u.getRole() == Role.CLINICIAN && u.getStatus() == AccountStatus.ACTIVE)
                years.merge(u.getYearLevel() != null ? u.getYearLevel().replace('-', ' ') : "No year level", 1L, Long::sum);
        model.addAttribute("years", years);
        model.addAttribute("pendingRequests", chartRequestService.getPendingRequests().size());
        model.addAttribute("needsFaculty", patientService.getAllPatients().stream().filter(p -> p.getAssignedFaculty() == null).count());
        model.addAttribute("recent", activity.latest(8));
        return "overview-sysadmin";
    }

    private static String sectionOf(String code) {
        if (code == null) return "";
        return switch (code.toUpperCase()) {
            case "CD", "RPD", "FPD" -> "Prostho";
            case "RESTO" -> "Resto";
            case "ENDO" -> "Endo";
            case "PERIO" -> "Perio";
            case "OS" -> "OS";
            default -> code;
        };
    }

    private List<Form> allForms() {
        List<Form> out = new java.util.ArrayList<>();
        java.util.function.Function<com.dentis.DENTIS.model.Patient, String> nm = p -> p == null ? "—" : p.getSurname() + ", " + p.getFirstName();
        java.util.function.Function<User, String> un = u -> u == null ? null : u.getFullName();
        java.util.function.Function<User, Long> ui = u -> u == null ? null : u.getId();
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
}
