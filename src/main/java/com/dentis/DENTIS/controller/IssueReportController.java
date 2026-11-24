package com.dentis.DENTIS.controller;

import com.dentis.DENTIS.model.IssueReport;
import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.repository.UserRepository;
import com.dentis.DENTIS.service.IssueReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/** Users report technical problems; the system administrator reviews and resolves them (objective 4c). */
@Controller
public class IssueReportController {

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private final IssueReportService issues;
    private final UserRepository userRepository;

    public IssueReportController(IssueReportService issues, UserRepository userRepository) {
        this.issues = issues;
        this.userRepository = userRepository;
    }

    private User currentUser(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof OAuth2User u)
            return userRepository.findByEmail(u.getAttribute("email")).orElse(null);
        return null;
    }

    // ── Any signed-in user ──

    @PostMapping("/report-issue")
    @ResponseBody
    public ResponseEntity<?> report(@RequestParam String subject, @RequestParam(required = false) String description,
                                    @RequestParam(required = false) String page, Authentication auth) {
        User me = currentUser(auth);
        if (me == null) return ResponseEntity.status(403).body(Map.of("error", "Please sign in again."));
        if (subject == null || subject.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "Please enter a subject."));
        issues.report(me, subject, description, page);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /** The signed-in user's last 5 reports and their status. */
    @GetMapping("/report-issue/mine")
    @ResponseBody
    public List<Map<String, String>> mine(Authentication auth) {
        User me = currentUser(auth);
        if (me == null) return List.of();
        return issues.recentFor(me).stream().map(r -> Map.of(
                "subject", r.getSubject(),
                "status", r.getStatus().name(),
                "label", r.getStatus().getLabel(),
                "date", r.getCreatedAt() != null ? r.getCreatedAt().format(WHEN) : "",
                "note", r.getAdminNote() != null ? r.getAdminNote() : "")).toList();
    }

    // ── System administrator ──

    @GetMapping("/issues-sysadmin")
    public String inbox(Model model, Authentication auth) {
        model.addAttribute("currentUser", currentUser(auth));
        model.addAttribute("adminPage", "issues");
        model.addAttribute("issues", issues.all());
        return "issues-sysadmin";
    }

    @PostMapping("/issues-sysadmin/{id}")
    public String update(@PathVariable Long id, @RequestParam IssueReport.Status status,
                         @RequestParam(required = false) String adminNote, Authentication auth) {
        issues.update(id, status, adminNote, currentUser(auth));
        return "redirect:/issues-sysadmin?saved=" + id + "#issue-" + id;
    }
}
