package team.projectpulse.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeactivateInstructorRequest(
    @NotBlank @Size(max = 500) String reason) {}
