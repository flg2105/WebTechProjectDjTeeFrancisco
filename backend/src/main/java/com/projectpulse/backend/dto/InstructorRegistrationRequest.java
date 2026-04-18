package com.projectpulse.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record InstructorRegistrationRequest(
        @NotBlank(message = "Invitation token is required.")
        String token,

        @NotBlank(message = "First name is required.")
        @Size(max = 100, message = "First name must be at most 100 characters.")
        String firstName,

        @Size(max = 1, message = "Middle initial must be a single character.")
        @Pattern(regexp = "^$|^[A-Za-z]$", message = "Middle initial must be a single letter.")
        String middleInitial,

        @NotBlank(message = "Last name is required.")
        @Size(max = 100, message = "Last name must be at most 100 characters.")
        String lastName,

        @NotBlank(message = "Password is required.")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters.")
        String password,

        @NotBlank(message = "Please reenter the password.")
        String confirmPassword
) {
}
