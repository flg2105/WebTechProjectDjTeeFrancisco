package team.projectpulse.user.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.projectpulse.user.domain.ProjectUser;
import team.projectpulse.user.domain.UserRole;

public interface UserRepository extends JpaRepository<ProjectUser, Long> {
  boolean existsByEmailIgnoreCase(String email);

  Optional<ProjectUser> findByEmailIgnoreCase(String email);

  List<ProjectUser> findByRoleOrderByDisplayNameAsc(UserRole role);

  List<ProjectUser> findAllByOrderByDisplayNameAsc();

  @Query("""
      select u from ProjectUser u
      where u.role = :role
        and (:q is null
          or lower(u.displayName) like lower(concat('%', :q, '%'))
          or lower(u.email) like lower(concat('%', :q, '%')))
      order by u.displayName asc
      """)
  List<ProjectUser> searchByRole(@Param("role") UserRole role, @Param("q") String q);
}
