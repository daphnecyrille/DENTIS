package com.dentis.DENTIS.service;

import com.dentis.DENTIS.model.SectionTarget;
import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.repository.SectionTargetRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SectionTargetService {

    private final SectionTargetRepository repo;

    public SectionTargetService(SectionTargetRepository repo) { this.repo = repo; }

    public Optional<SectionTarget> get(String section) {
        if (section == null || section.isBlank()) return Optional.empty();
        return repo.findBySectionIgnoreCase(section.trim());
    }

    public SectionTarget set(String section, int target, User by) {
        SectionTarget t = get(section).orElseGet(SectionTarget::new);
        t.setSection(section.trim().toUpperCase());
        t.setTarget(Math.max(0, Math.min(target, 99)));
        t.setUpdatedBy(by);
        t.setUpdatedAt(LocalDateTime.now());
        return repo.save(t);
    }
}
