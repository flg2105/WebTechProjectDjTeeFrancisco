package team.projectpulse.peereval.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record SubmitPeerEvaluationRequest(
    @NotNull Long evaluatorStudentUserId,
    @NotNull LocalDate weekStartDate,
    @NotEmpty List<@Valid PeerEvaluationEntryRequest> evaluations) {
}
