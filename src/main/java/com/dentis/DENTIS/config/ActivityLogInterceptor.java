package com.dentis.DENTIS.config;

import com.dentis.DENTIS.model.Patient;
import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.repository.UserRepository;
import com.dentis.DENTIS.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Writes the activity log after important actions succeed (approve, revise, submit, assign, requests, accounts).
 * One table of rules instead of logging code in every controller. An action that was refused
 * (redirect with notAssigned / signRequired / gradeRequired / err=) is not logged.
 */
@Component
public class ActivityLogInterceptor implements HandlerInterceptor {

    private record Rule(Pattern path, String category, String action, BiFunction<Long, HttpServletRequest, String> target) { }

    private final ActivityLogService activity;
    private final UserRepository users;
    private final PeriodonticChartService perio;
    private final EndodonticsChartService endo;
    private final OperativeChartService operative;
    private final OralSurgeryChartService os;
    private final ProsthodonticsChartService prostho;
    private final PatientService patients;
    private final ChartRequestService requests;
    private final PerioEvaluationService evals;
    private final List<Rule> rules;

    public ActivityLogInterceptor(ActivityLogService activity, UserRepository users, PeriodonticChartService perio,
                                  EndodonticsChartService endo, OperativeChartService operative, OralSurgeryChartService os,
                                  ProsthodonticsChartService prostho, PatientService patients, ChartRequestService requests,
                                  PerioEvaluationService evals) {
        this.activity = activity; this.users = users; this.perio = perio; this.endo = endo; this.operative = operative;
        this.os = os; this.prostho = prostho; this.patients = patients; this.requests = requests; this.evals = evals;
        this.rules = List.of(
            // faculty: approve / revise
            rule("/periodontics-faculty/(\\d+)/approve", "APPROVE", "Approved PF1 (signed)", (id, r) -> perioT(id, "-PF1")),
            rule("/periodontics-faculty/(\\d+)/revise", "REVISE", "Sent PF1 back for revision", (id, r) -> perioT(id, "-PF1")),
            rule("/periodontics2-faculty/(\\d+)/approve", "APPROVE", "Approved PF2", (id, r) -> perioT(id, "-PF2")),
            rule("/periodontics2-faculty/(\\d+)/revise", "REVISE", "Sent PF2 back for revision", (id, r) -> perioT(id, "-PF2")),
            rule("/periodontics3-faculty/(\\d+)/approve", "APPROVE", "Approved a PF3 recall (signed)", (id, r) -> perioT(id, "-PF3")),
            rule("/periodontics3-faculty/(\\d+)/revise", "REVISE", "Sent PF3 back for revision", (id, r) -> perioT(id, "-PF3")),
            rule("/endodontics-faculty/(\\d+)/approve", "APPROVE", "Approved Endo Patient Workup", (id, r) -> endoT(id, false)),
            rule("/endodontics-faculty/(\\d+)/revise", "REVISE", "Sent Endo Patient Workup back for revision", (id, r) -> endoT(id, false)),
            rule("/endodontics2-faculty/(\\d+)/approve", "APPROVE", "Approved Endo Evaluation & Treatment Planning", (id, r) -> endoT(id, true)),
            rule("/endodontics2-faculty/(\\d+)/revise", "REVISE", "Sent Endo Evaluation back for revision", (id, r) -> endoT(id, true)),
            rule("/operative-faculty/(\\d+)/approve", "APPROVE", "Approved RESTO10", (id, r) -> opT(id, false)),
            rule("/operative-faculty/(\\d+)/revise", "REVISE", "Sent RESTO10 back for revision", (id, r) -> opT(id, false)),
            rule("/operative2-faculty/(\\d+)/approve", "APPROVE", "Approved RESTO6", (id, r) -> opT(id, true)),
            rule("/operative2-faculty/(\\d+)/revise", "REVISE", "Sent RESTO6 back for revision", (id, r) -> opT(id, true)),
            rule("/oralsurgery-faculty/(\\d+)/approve", "APPROVE", "Approved Oral Surgery chart", (id, r) -> osT(id)),
            rule("/oralsurgery-faculty/(\\d+)/revise", "REVISE", "Sent Oral Surgery chart back for revision", (id, r) -> osT(id)),
            rule("/prosthodontics-faculty/(\\d+)/approve", "APPROVE", "Cleared Prostho case (signed)", (id, r) -> prosthoT(id) + gradeOf(id)),
            rule("/prosthodontics-faculty/(\\d+)/revise", "REVISE", "Sent Prostho booklet back for revision", (id, r) -> prosthoT(id)),
            // clinician: submit
            rule("/periodontics-ab/(\\d+)/submit", "SUBMIT", "Submitted PF1", (id, r) -> perioT(id, "-PF1")),
            rule("/periodontics-c/(\\d+)/submit", "SUBMIT", "Submitted PF2", (id, r) -> perioT(id, "-PF2")),
            rule("/periodontics-pf3/(\\d+)/submit", "SUBMIT", "Submitted PF3", (id, r) -> perioT(id, "-PF3")),
            rule("/endodontics-clinician/(\\d+)/submit", "SUBMIT", "Submitted Endo Patient Workup", (id, r) -> endoT(id, false)),
            rule("/endodontics2-clinician/(\\d+)/submit", "SUBMIT", "Submitted Endo Evaluation", (id, r) -> endoT(id, true)),
            rule("/operative-c/(\\d+)/submit", "SUBMIT", "Submitted RESTO10", (id, r) -> opT(id, false)),
            rule("/operative-a/(\\d+)/submit", "SUBMIT", "Submitted RESTO6", (id, r) -> opT(id, true)),
            rule("/oralsurgery-clinician/(\\d+)/submit", "SUBMIT", "Submitted Oral Surgery chart", (id, r) -> osT(id)),
            rule("/prosthodontics-clinician/(\\d+)/submit", "SUBMIT", "Submitted Prostho booklet for clearance", (id, r) -> prosthoT(id)),
            // evaluations
            rule("/perio-eval-faculty/(\\d+)", "EVALUATION", null, (id, r) -> evalT(id)),
            // assignments and requests
            rule("/chartsview-clinicmanager/(\\d+)/assign-faculty", "ASSIGN", "Assigned faculty", (id, r) -> patientT(id) + " → " + userName(r.getParameter("facultyId"))),
            rule("/faculty-dashboard/(\\d+)/assign-clinician", "ASSIGN", "Assigned clinician", (id, r) -> patientT(id) + " → " + userName(r.getParameter("clinicianId"))),
            rule("/assign-clinicmanager/save-all", "ASSIGN", "Updated faculty advisers", (id, r) -> null),
            rule("/requestlist-clinicmanager/(\\d+)/approve", "REQUEST", "Accepted a chart request", (id, r) -> requestT(id)),
            rule("/requestlist-clinicmanager/(\\d+)/deny", "REQUEST", "Denied a chart request", (id, r) -> requestT(id)),
            rule("/chartrequest-clinician", "REQUEST", "Requested a chart", (id, r) -> r.getParameter("chartNo") + " (" + r.getParameter("section") + ")"),
            // patients
            rule("/admitting-clinicmanager", "PATIENT", "Admitted a patient", (id, r) -> r.getParameter("surname") + ", " + r.getParameter("firstName") + " (" + r.getParameter("serviceCode") + ")"),
            rule("/readmit-clinicmanager/(\\d+)", "PATIENT", "Readmitted a patient", (id, r) -> patientT(id)),
            // accounts
            rule("/create-user-sysadmin", "ACCOUNT", "Created an account", (id, r) -> r.getParameter("email") + " (" + r.getParameter("role") + ")"),
            rule("/edit-user-sysadmin", "ACCOUNT", "Edited an account", (id, r) -> userName(r.getParameter("userId"))),
            rule("/toggle-status-sysadmin", "ACCOUNT", null, (id, r) -> userName(r.getParameter("userId")))
        );
    }

    private static Rule rule(String regex, String category, String action, BiFunction<Long, HttpServletRequest, String> target) {
        return new Rule(Pattern.compile("^" + regex + "$"), category, action, target);
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex) {
        if (ex != null || !"POST".equalsIgnoreCase(req.getMethod())) return;
        if (res.getStatus() >= 400) return;
        String loc = res.getHeader("Location");
        if (loc != null && (loc.contains("notAssigned") || loc.contains("signRequired") || loc.contains("gradeRequired") || loc.contains("err="))) return;
        String path = req.getRequestURI().substring(req.getContextPath().length());
        for (Rule rule : rules) {
            Matcher m = rule.path().matcher(path);
            if (!m.matches()) continue;
            Long id = m.groupCount() > 0 ? Long.valueOf(m.group(1)) : null;
            String action = rule.action();
            if (action == null) {
                if ("ACCOUNT".equals(rule.category())) action = "activate".equals(req.getParameter("action")) ? "Activated an account" : "Deactivated an account";
                else if ("send".equals(req.getParameter("action"))) action = "Sent an evaluation form";
                else return;   // saving an evaluation draft is not logged
            }
            String target;
            try { target = rule.target().apply(id, req); } catch (Exception e) { target = id != null ? "#" + id : null; }
            activity.record(currentUser(), rule.category(), action, target);
            return;
        }
    }

    private User currentUser() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a != null && a.getPrincipal() instanceof OAuth2User u) return users.findByEmail(u.getAttribute("email")).orElse(null);
        return null;
    }

    // ── target descriptions ──
    private static String name(Patient p) {
        return p == null ? "" : p.getSurname() + ", " + p.getFirstName();
    }
    private String perioT(Long id, String suffix) {
        var c = perio.getById(id);
        return c.getChartNo().replace("-PF1", suffix) + " · " + name(c.getPatient());
    }
    private String endoT(Long id, boolean form2) {
        var c = endo.getById(id);
        return (form2 ? c.getChartNo().replace("-ENDO1", "-ENDO2") : c.getChartNo()) + " · " + name(c.getPatient());
    }
    private String opT(Long id, boolean resto6) {
        var c = operative.getById(id);
        return (resto6 ? c.getChartNo().replace("-RESTO10", "-RESTO6") : c.getChartNo()) + " · " + name(c.getPatient());
    }
    private String osT(Long id) { var c = os.getById(id); return c.getChartNo() + " · " + name(c.getPatient()); }
    private String prosthoT(Long id) { var c = prostho.getById(id); return c.getChartNo() + " · " + name(c.getPatient()); }
    private String gradeOf(Long id) { var c = prostho.getById(id); return c.getGrade() != null ? " · Grade " + c.getGrade() : ""; }
    private String patientT(Long id) { var p = patients.getPatientById(id); return p.getChartNo() + " · " + name(p); }
    private String requestT(Long id) {
        var q = requests.getRequestById(id);
        return (q.getClinician() != null ? q.getClinician().getFullName() + " · " : "") + q.getChartNo()
                + (q.getSection() != null ? " (" + q.getSection() + ")" : "");
    }
    private String evalT(Long id) {
        var e = evals.getById(id);
        return e.getFormType().name() + " · " + e.getPerioChart().getChartNo().replace("-PF1", "") + " · " + name(e.getPerioChart().getPatient());
    }
    private String userName(String id) {
        try { return users.findById(Long.valueOf(id)).map(User::getFullName).orElse("—"); } catch (Exception e) { return "—"; }
    }
}
