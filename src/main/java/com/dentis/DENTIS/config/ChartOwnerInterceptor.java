package com.dentis.DENTIS.config;

import com.dentis.DENTIS.model.Patient;
import com.dentis.DENTIS.model.Role;
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

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Only the clinician a chart belongs to can edit it. Another clinician who can see the patient
 * (through an accepted chart request) gets the read-only page: editing pages redirect to their
 * "/view" page, and saves / submits are refused.
 */
@Component
public class ChartOwnerInterceptor implements HandlerInterceptor {

    private static final Pattern FORM = Pattern.compile(
            "^/(oralsurgery-clinician|oralsurgery2-clinician|endodontics-clinician|endodontics4-clinician|prosthodontics-clinician"
            + "|periodontics-a|periodontics-b|periodontics-c|periodontics-pf3|periodontics-ab"
            + "|operative-a|operative-b|operative-c)/(\\d+)(/.*)?$");

    /** owner (clinician) and patient of a chart */
    private record Owner(User clinician, Patient patient) { }

    private final UserRepository users;
    private final OralSurgeryChartService os;
    private final EndodonticsChartService endo;
    private final PeriodonticChartService perio;
    private final OperativeChartService operative;
    private final ProsthodonticsChartService prostho;

    public ChartOwnerInterceptor(UserRepository users, OralSurgeryChartService os, EndodonticsChartService endo,
                                 PeriodonticChartService perio, OperativeChartService operative,
                                 ProsthodonticsChartService prostho) {
        this.users = users; this.os = os; this.endo = endo; this.perio = perio; this.operative = operative; this.prostho = prostho;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        Matcher m = FORM.matcher(req.getRequestURI());
        if (!m.matches()) return true;
        User me = currentUser();
        if (me == null || me.getRole() != Role.CLINICIAN) return true;

        String form = m.group(1), rest = m.group(3) == null ? "" : m.group(3);
        Long id = Long.valueOf(m.group(2));
        Owner owner;
        try { owner = ownerOf(form).apply(id); } catch (Exception e) { return true; }   // let the controller handle a missing chart
        if (owner.clinician() != null && owner.clinician().getId().equals(me.getId())) return true;   // their own chart

        boolean get = "GET".equalsIgnoreCase(req.getMethod());
        if (get && rest.startsWith("/view")) return true;                       // read-only page: allowed
        if (get && rest.isEmpty()) {                                             // editing page -> read-only page
            res.sendRedirect("/" + form + "/" + id + "/view");
            return false;
        }
        // saving / submitting / signing someone else's chart: refused
        res.sendRedirect(owner.patient() != null ? "/chartsview-clinician/" + owner.patient().getId() + "?viewOnly" : "/patientlist-clinician");
        return false;
    }

    private Function<Long, Owner> ownerOf(String form) {
        if (form.startsWith("oralsurgery")) return id -> { var c = os.getById(id); return new Owner(c.getClinician(), c.getPatient()); };
        if (form.startsWith("endodontics")) return id -> { var c = endo.getById(id); return new Owner(c.getClinician(), c.getPatient()); };
        if (form.startsWith("periodontics")) return id -> { var c = perio.getById(id); return new Owner(c.getClinician(), c.getPatient()); };
        if (form.startsWith("operative")) return id -> { var c = operative.getById(id); return new Owner(c.getClinician(), c.getPatient()); };
        return id -> { var c = prostho.getById(id); return new Owner(c.getClinician(), c.getPatient()); };
    }

    private User currentUser() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a != null && a.getPrincipal() instanceof OAuth2User u) return users.findByEmail(u.getAttribute("email")).orElse(null);
        return null;
    }
}
