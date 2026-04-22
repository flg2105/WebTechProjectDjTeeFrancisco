package team.projectpulse.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TeamRequest(
    @NotNull Long sectionId,
    @NotBlank String name) {}
