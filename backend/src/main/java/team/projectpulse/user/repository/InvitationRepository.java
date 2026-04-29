package team.projectpulse.user.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import team.projectpulse.user.domain.Invitation;
import team.projectpulse.user.domain.UserRole;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
  boolean existsByEmailIgnoreCaseAndRole(String email, UserRole role);

  List<Invitation> findBySectionIdAndRole(Long sectionId, UserRole role);

  void deleteByEmailIgnoreCaseAndRole(String email, UserRole role);
}
