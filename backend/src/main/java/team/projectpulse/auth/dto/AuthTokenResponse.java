package team.projectpulse.auth.dto;

import java.time.Instant;
import team.projectpulse.user.domain.UserRole;
import team.projectpulse.user.domain.UserStatus;

public record AuthTokenResponse(
    String tokenType,
    String accessToken,
    Instant expiresAt,
    Long userId,
    String email,
    String displayName,
    UserRole role,
    UserStatus status) {}
