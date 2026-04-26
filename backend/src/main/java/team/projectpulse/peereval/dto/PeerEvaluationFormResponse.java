package team.projectpulse.peereval.dto;

import java.time.LocalDate;
import java.util.List;

public record PeerEvaluationFormResponse(
    Long evaluatorStudentUserId,
    String evaluatorDisplayName,
    Long sectionId,
    Long teamId,
    LocalDate weekStartDate,
    boolean alreadySubmitted,
    List<PeerEvaluationCriterionFormResponse> criteria,
    List<PeerEvaluationTeammateResponse> teammates) {
}
