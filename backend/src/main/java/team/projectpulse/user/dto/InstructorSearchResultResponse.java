package team.projectpulse.user.dto;

import java.util.List;
import team.projectpulse.user.domain.UserStatus;

public record InstructorSearchResultResponse(
    Long id,
    String email,
    String displayName,
    String firstName,
    String lastName,
    UserStatus status,
    List<InstructorSupervisedTeamResponse> supervisedTeams) {}
