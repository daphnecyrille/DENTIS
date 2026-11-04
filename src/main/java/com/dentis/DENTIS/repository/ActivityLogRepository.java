package com.dentis.DENTIS.repository;

import com.dentis.DENTIS.model.ActivityLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByAtAfterOrderByAtDesc(LocalDateTime since);
    List<ActivityLog> findAllByOrderByAtDesc(Pageable page);
}
