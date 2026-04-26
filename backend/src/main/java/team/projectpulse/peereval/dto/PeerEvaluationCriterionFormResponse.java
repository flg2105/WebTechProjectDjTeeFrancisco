package team.projectpulse.peereval.dto;

import java.math.BigDecimal;

public record PeerEvaluationCriterionFormResponse(
    Long rubricCriterionId,
    String name,
    String description,
    BigDecimal maxScore,
    Integer position) {
}
