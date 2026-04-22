package team.projectpulse.user.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import team.projectpulse.user.domain.ProjectUser;
import team.projectpulse.user.domain.UserRole;

public interface UserRepository extends JpaRepository<ProjectUser, Long> {
  boolean existsByEmailIgnoreCase(String email);

  Optional<ProjectUser> findByEmailIgnoreCase(String email);

  List<ProjectUser> findByRoleOrderByDisplayNameAsc(UserRole role);

  List<ProjectUser> findAllByOrderByDisplayNameAsc();
}
