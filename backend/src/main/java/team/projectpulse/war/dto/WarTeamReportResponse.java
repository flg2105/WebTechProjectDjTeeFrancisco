package team.projectpulse.war.dto;

import java.time.LocalDate;
import java.util.List;

public record WarTeamReportResponse(
    Long teamId,
    String teamName,
    Long sectionId,
    Long activeWeekId,
    LocalDate weekStartDate,
    List<WarTeamMemberReportResponse> memberReports,
    List<WarTeamMemberReportResponse> missingSubmissions) {}

