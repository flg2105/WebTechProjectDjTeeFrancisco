package team.projectpulse.user.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StudentPeerEvaluationSummaryResponse(
    Long entryId,
    Long submissionId,
    LocalDate weekStartDate,
    Long evaluatorStudentUserId,
    String evaluatorDisplayName,
    Long teamId,
    String teamName,
    BigDecimal averageScore,
    String publicComment,
    String privateComment) {}
