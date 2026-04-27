package team.projectpulse.peereval.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PeerEvaluationStudentReportResponse(
    Long studentUserId,
    String studentDisplayName,
    Long sectionId,
    Long teamId,
    String teamName,
    Long startActiveWeekId,
    Long endActiveWeekId,
    LocalDate startWeekStartDate,
    LocalDate endWeekStartDate,
    BigDecimal maxTotalScore,
    List<PeerEvaluationStudentWeeklyReportResponse> weeks) {}

