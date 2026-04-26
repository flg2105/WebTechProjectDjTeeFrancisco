package team.projectpulse.user.dto;

public record StudentSearchResultResponse(
    Long id,
    String email,
    String displayName,
    Long sectionId,
    Long teamId,
    String sectionName,
    String teamName) {}

