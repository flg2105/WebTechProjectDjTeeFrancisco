package team.projectpulse.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EditAccountRequest(
    @Email @NotBlank String email,
    @NotBlank String displayName) {}

