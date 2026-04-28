package team.projectpulse.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SetupAccountRequest(
    @Email @NotBlank String email,
    @NotBlank String displayName,
    @NotBlank @Size(min = 8, max = 72) String password) {}
