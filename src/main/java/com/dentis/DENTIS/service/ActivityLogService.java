package com.dentis.DENTIS.service;

import com.dentis.DENTIS.model.ActivityLog;
import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.repository.ActivityLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/** Writes and reads the activity log. Logging never breaks the action itself. */
@Service
public class ActivityLogService {

    private static final Logger log = LoggerFactory.getLogger(ActivityLogService.class);
    private final ActivityLogRepository repo;

    public ActivityLogService(ActivityLogRepository repo) { this.repo = repo; }

    public void record(User actor, String category, String action, String target) {
        try {
            ActivityLog e = new ActivityLog();
            if (actor != null) {
                e.setActorId(actor.getId());
                e.setActorName(actor.getFullName());
                e.setActorRole(actor.getRole() != null ? actor.getRole().getDisplayName() : null);
            }
            e.setCategory(category);
            e.setAction(action);
            e.setTarget(target);
            repo.save(e);
        } catch (Exception ex) {
            log.warn("Could not write activity log: {}", ex.getMessage());
        }
    }

    /** Entries of the last n days, newest first. */
    public List<ActivityLog> since(int days) {
        return repo.findByAtAfterOrderByAtDesc(LocalDateTime.now().minusDays(days));
    }

    public List<ActivityLog> latest(int n) {
        return repo.findAllByOrderByAtDesc(PageRequest.of(0, n));
    }
}
