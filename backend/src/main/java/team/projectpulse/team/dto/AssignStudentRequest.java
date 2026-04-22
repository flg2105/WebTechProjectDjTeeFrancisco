package team.projectpulse.team.dto;

import jakarta.validation.constraints.NotNull;

public record AssignStudentRequest(@NotNull Long studentUserId) {}
