package team.projectpulse.peereval.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PeerEvaluationCriterionScoreRequest(
    @NotNull Long rubricCriterionId,
    @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal score) {
}
