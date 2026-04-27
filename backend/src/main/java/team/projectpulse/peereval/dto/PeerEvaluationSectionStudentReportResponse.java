package team.projectpulse.peereval.dto;

import java.math.BigDecimal;
import java.util.List;

public record PeerEvaluationSectionStudentReportResponse(
    Long studentUserId,
    String studentDisplayName,
    Long teamId,
    String teamName,
    BigDecimal averageTotalScore,
    int receivedEvaluations,
    List<PeerEvaluationSectionEvaluationDetailResponse> evaluations) {
}
