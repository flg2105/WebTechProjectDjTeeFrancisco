package team.projectpulse.section.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AssignSectionInstructorsRequest(@NotNull @NotEmpty List<Long> instructorUserIds) {}

