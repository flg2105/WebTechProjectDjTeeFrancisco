package team.projectpulse.auth.dto;

import team.projectpulse.user.domain.UserRole;
import team.projectpulse.user.domain.UserStatus;

public record CurrentUserResponse(
    Long userId,
    String email,
    String displayName,
    UserRole role,
    UserStatus status) {}
