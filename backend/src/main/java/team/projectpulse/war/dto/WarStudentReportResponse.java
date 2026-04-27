package team.projectpulse.war.dto;

import java.time.LocalDate;
import java.util.List;

public record WarStudentReportResponse(
    Long studentUserId,
    Long sectionId,
    Long startActiveWeekId,
    Long endActiveWeekId,
    LocalDate startWeekStartDate,
    LocalDate endWeekStartDate,
    List<WarEntryResponse> entries) {}

