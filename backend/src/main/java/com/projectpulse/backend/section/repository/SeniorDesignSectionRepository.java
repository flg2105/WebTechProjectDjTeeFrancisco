package com.projectpulse.backend.section.repository;

import com.projectpulse.backend.section.domain.SeniorDesignSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeniorDesignSectionRepository extends JpaRepository<SeniorDesignSection, Long> {
    Optional<SeniorDesignSection> findBySectionCode(String sectionCode);
}
