package team.projectpulse.peereval.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PeerEvaluationEntryRequest(
    @NotNull Long evaluateeStudentUserId,
    @Size(max = 2000) String publicComment,
    @Size(max = 2000) String privateComment,
    @NotEmpty List<@Valid PeerEvaluationCriterionScoreRequest> scores) {
}
