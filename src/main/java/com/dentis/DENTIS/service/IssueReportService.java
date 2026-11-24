package com.dentis.DENTIS.service;

import com.dentis.DENTIS.model.IssueReport;
import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.repository.IssueReportRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IssueReportService {

    private final IssueReportRepository repo;

    public IssueReportService(IssueReportRepository repo) { this.repo = repo; }

    public IssueReport report(User reporter, String subject, String description, String page) {
        IssueReport r = new IssueReport();
        r.setReporter(reporter);
        r.setSubject(cut(subject, 150));
        r.setDescription(cut(description, 4000));
        r.setPage(cut(page, 300));
        return repo.save(r);
    }

    public List<IssueReport> all() { return repo.findAllByOrderByCreatedAtDesc(); }

    public List<IssueReport> recentFor(User reporter) { return repo.findTop5ByReporterOrderByCreatedAtDesc(reporter); }

    /** Open plus in-progress reports (shown as the count on the admin sidebar). */
    public long unresolvedCount() {
        return repo.countByStatus(IssueReport.Status.OPEN) + repo.countByStatus(IssueReport.Status.IN_PROGRESS);
    }

    public void update(Long id, IssueReport.Status status, String note, User admin) {
        IssueReport r = repo.findById(id).orElseThrow();
        r.setStatus(status);
        r.setAdminNote(note == null || note.isBlank() ? null : cut(note.trim(), 4000));
        r.setHandledBy(admin);
        r.setUpdatedAt(LocalDateTime.now());
        repo.save(r);
    }

    private static String cut(String s, int max) {
        if (s == null) return null;
        s = s.trim();
        return s.length() > max ? s.substring(0, max) : s;
    }
}
