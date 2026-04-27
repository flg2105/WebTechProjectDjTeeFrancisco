package team.projectpulse.peereval.dto;

import java.math.BigDecimal;

public record PeerEvaluationSectionEvaluationDetailResponse(
    Long evaluatorStudentUserId,
    String evaluatorDisplayName,
    BigDecimal totalScore,
    String publicComment,
    String privateComment) {
}
