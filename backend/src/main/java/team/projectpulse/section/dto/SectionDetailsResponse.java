package team.projectpulse.section.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record SectionDetailsResponse(
    Long id,
    String name,
    String academicYear,
    LocalDate startDate,
    LocalDate endDate,
    Long rubricId,
    Instant createdAt,
    Instant updatedAt,
    List<ActiveWeekResponse> activeWeeks,
    List<Long> instructorUserIds,
    List<SectionTeamDetailsResponse> teams,
    List<SectionUserSummaryResponse> unassignedInstructors,
    List<SectionUserSummaryResponse> unassignedStudents,
    SectionRubricResponse rubricUsed) {}
