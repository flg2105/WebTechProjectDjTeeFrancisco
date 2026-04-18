package com.projectpulse.backend.section.repository;

import com.projectpulse.backend.section.domain.ActiveWeek;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActiveWeekRepository extends JpaRepository<ActiveWeek, Long> {
}
