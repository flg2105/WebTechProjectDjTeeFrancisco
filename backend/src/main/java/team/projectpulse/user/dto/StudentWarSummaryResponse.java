package team.projectpulse.user.dto;

import java.time.Instant;
import java.time.LocalDate;

public record StudentWarSummaryResponse(
    Long id,
    Long activeWeekId,
    LocalDate weekStartDate,
    Long teamId,
    String teamName,
    Instant submittedAt,
    int activityCount) {}
