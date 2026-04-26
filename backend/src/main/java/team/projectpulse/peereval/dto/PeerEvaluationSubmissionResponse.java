package team.projectpulse.peereval.dto;

import java.time.Instant;
import java.time.LocalDate;

public record PeerEvaluationSubmissionResponse(
    Long id,
    Long evaluatorStudentUserId,
    Long teamId,
    Long sectionId,
    LocalDate weekStartDate,
    int evaluationsSubmitted,
    Instant submittedAt) {
}
