package com.projectpulse.backend.team.repository;

import com.projectpulse.backend.team.domain.TeamMembership;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMembershipRepository extends JpaRepository<TeamMembership, Long> {
}
