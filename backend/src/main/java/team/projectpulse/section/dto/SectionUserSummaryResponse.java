package team.projectpulse.section.dto;

import team.projectpulse.user.domain.UserStatus;

public record SectionUserSummaryResponse(
    Long id,
    String displayName,
    String email,
    UserStatus status) {}
