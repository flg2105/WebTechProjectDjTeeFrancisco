package team.projectpulse.peereval.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PeerEvaluationStudentWeeklyReportResponse(
    LocalDate weekStartDate,
    BigDecimal averageTotalScore,
    int receivedEvaluations,
    List<PeerEvaluationStudentEvaluationDetailResponse> evaluations) {}

