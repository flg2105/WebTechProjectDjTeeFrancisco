package team.projectpulse.team.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import team.projectpulse.team.domain.TeamInstructorAssignment;

public interface TeamInstructorAssignmentRepository extends JpaRepository<TeamInstructorAssignment, Long> {
  boolean existsByTeamIdAndInstructorUserId(Long teamId, Long instructorUserId);

  boolean existsByTeamId(Long teamId);

  List<TeamInstructorAssignment> findByInstructorUserIdOrderByTeamIdAsc(Long instructorUserId);

  List<TeamInstructorAssignment> findByTeamIdOrderByInstructorUserIdAsc(Long teamId);

  void deleteByTeamId(Long teamId);

  void deleteByTeamIdAndInstructorUserId(Long teamId, Long instructorUserId);
}
