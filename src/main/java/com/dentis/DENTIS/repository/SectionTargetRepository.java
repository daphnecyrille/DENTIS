package com.dentis.DENTIS.repository;

import com.dentis.DENTIS.model.SectionTarget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SectionTargetRepository extends JpaRepository<SectionTarget, Long> {
    Optional<SectionTarget> findBySectionIgnoreCase(String section);
}
