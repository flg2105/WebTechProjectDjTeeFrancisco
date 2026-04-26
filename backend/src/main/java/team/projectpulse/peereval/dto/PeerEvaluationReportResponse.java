package team.projectpulse.peereval.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PeerEvaluationReportResponse(
    Long studentUserId,
    String studentDisplayName,
    Long sectionId,
    Long teamId,
    LocalDate weekStartDate,
    BigDecimal averageTotalScore,
    int receivedEvaluations,
    List<PeerEvaluationCriterionAverageResponse> criterionAverages,
    List<String> publicComments) {
}
