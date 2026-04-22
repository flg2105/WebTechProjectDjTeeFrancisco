package team.projectpulse.user.dto;

import java.time.Instant;
import team.projectpulse.user.domain.UserRole;
import team.projectpulse.user.domain.UserStatus;

public record UserResponse(
    Long id,
    String email,
    String displayName,
    UserRole role,
    UserStatus status,
    Instant createdAt,
    Instant updatedAt) {}
