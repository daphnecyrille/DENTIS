package com.dentis.DENTIS.config;

import com.dentis.DENTIS.service.IssueReportService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Adds the number of unresolved issue reports to the system admin pages (shown on the sidebar). */
@ControllerAdvice
public class AdminModelAdvice {

    private final IssueReportService issues;

    public AdminModelAdvice(IssueReportService issues) { this.issues = issues; }

    @ModelAttribute("openIssueCount")
    public Long openIssueCount(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path != null && path.endsWith("-sysadmin") ? issues.unresolvedCount() : null;
    }
}
