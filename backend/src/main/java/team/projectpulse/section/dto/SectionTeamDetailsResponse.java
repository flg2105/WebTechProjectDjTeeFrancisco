package team.projectpulse.section.dto;

import java.util.List;

public record SectionTeamDetailsResponse(
    Long id,
    String name,
    List<SectionUserSummaryResponse> members,
    List<SectionUserSummaryResponse> instructors) {}
