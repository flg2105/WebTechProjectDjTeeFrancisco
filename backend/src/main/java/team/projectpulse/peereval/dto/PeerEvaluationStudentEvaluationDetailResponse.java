package team.projectpulse.peereval.dto;

import java.math.BigDecimal;

public record PeerEvaluationStudentEvaluationDetailResponse(
    Long evaluatorStudentUserId,
    String evaluatorDisplayName,
    BigDecimal totalScore,
    String publicComment,
    String privateComment) {}

