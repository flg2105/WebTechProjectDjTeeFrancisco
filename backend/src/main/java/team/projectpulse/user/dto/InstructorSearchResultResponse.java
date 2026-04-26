package team.projectpulse.user.dto;

import team.projectpulse.user.domain.UserStatus;

public record InstructorSearchResultResponse(
    Long id,
    String email,
    String displayName,
    UserStatus status,
    Long teamId,
    String teamName) {}

