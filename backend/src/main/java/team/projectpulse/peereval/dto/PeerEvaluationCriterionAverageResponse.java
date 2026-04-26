package team.projectpulse.peereval.dto;

import java.math.BigDecimal;

public record PeerEvaluationCriterionAverageResponse(
    Long rubricCriterionId,
    String name,
    String description,
    BigDecimal maxScore,
    Integer position,
    BigDecimal averageScore) {
}
