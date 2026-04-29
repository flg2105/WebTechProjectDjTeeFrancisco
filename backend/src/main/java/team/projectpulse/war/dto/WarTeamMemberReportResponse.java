package team.projectpulse.war.dto;

public record WarTeamMemberReportResponse(
    Long studentUserId,
    String studentDisplayName,
    boolean submitted,
    WarEntryResponse entry) {}

