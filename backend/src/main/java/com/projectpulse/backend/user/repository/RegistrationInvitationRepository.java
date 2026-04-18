package com.projectpulse.backend.user.repository;

import com.projectpulse.backend.user.domain.RegistrationInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegistrationInvitationRepository extends JpaRepository<RegistrationInvitation, Long> {
    Optional<RegistrationInvitation> findByToken(String token);
}
