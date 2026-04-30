package team.projectpulse.team.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import team.projectpulse.team.domain.TeamMembership;

public interface TeamMembershipRepository extends JpaRepository<TeamMembership, Long> {
  boolean existsByTeamIdAndStudentUserId(Long teamId, Long studentUserId);

  boolean existsByTeamId(Long teamId);

  boolean existsByStudentUserId(Long studentUserId);

  List<TeamMembership> findByStudentUserIdOrderByTeamIdAsc(Long studentUserId);

  List<TeamMembership> findByTeamIdOrderByStudentUserIdAsc(Long teamId);

  void deleteByTeamId(Long teamId);

  void deleteByTeamIdAndStudentUserId(Long teamId, Long studentUserId);

  void deleteByStudentUserId(Long studentUserId);
}
