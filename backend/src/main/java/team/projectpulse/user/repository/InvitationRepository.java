package team.projectpulse.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.projectpulse.user.domain.Invitation;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {}
