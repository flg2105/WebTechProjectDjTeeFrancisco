package team.projectpulse.peereval.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import team.projectpulse.peereval.domain.PeerEvaluationSubmission;

public interface PeerEvaluationSubmissionRepository extends JpaRepository<PeerEvaluationSubmission, Long> {
  boolean existsByEvaluatorStudentUserIdAndWeekStartDate(Long evaluatorStudentUserId, LocalDate weekStartDate);

  List<PeerEvaluationSubmission> findByTeamIdAndWeekStartDate(Long teamId, LocalDate weekStartDate);
}
