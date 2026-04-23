package team.projectpulse.user.dto;

import java.util.List;
import team.projectpulse.user.domain.UserRole;

public record InvitationResponse(
    UserRole role,
    Long sectionId,
    List<UserResponse> users) {}
