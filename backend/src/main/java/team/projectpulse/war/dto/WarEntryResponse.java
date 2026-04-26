package team.projectpulse.war.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record WarEntryResponse(
    Long id,
    Long activeWeekId,
    LocalDate weekStartDate,
    Long teamId,
    Long studentUserId,
    Instant submittedAt,
    List<WarActivityResponse> activities) {}
