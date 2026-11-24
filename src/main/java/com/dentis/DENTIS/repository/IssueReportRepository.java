package com.dentis.DENTIS.repository;

import com.dentis.DENTIS.model.IssueReport;
import com.dentis.DENTIS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueReportRepository extends JpaRepository<IssueReport, Long> {
    List<IssueReport> findAllByOrderByCreatedAtDesc();
    List<IssueReport> findTop5ByReporterOrderByCreatedAtDesc(User reporter);
    long countByStatus(IssueReport.Status status);
}
