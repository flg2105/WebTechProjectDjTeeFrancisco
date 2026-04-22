package team.projectpulse.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record InvitationRequest(
    Long sectionId,
    @NotEmpty List<@Email String> emails) {}
