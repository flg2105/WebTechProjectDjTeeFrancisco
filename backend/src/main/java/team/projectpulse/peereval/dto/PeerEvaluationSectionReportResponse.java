package team.projectpulse.peereval.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PeerEvaluationSectionReportResponse(
    Long sectionId,
    String sectionName,
    LocalDate weekStartDate,
    BigDecimal maxTotalScore,
    List<PeerEvaluationSectionStudentReportResponse> students,
    List<PeerEvaluationSectionMissingSubmissionResponse> missingSubmitters) {
}
