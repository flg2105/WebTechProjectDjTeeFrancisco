package team.projectpulse.team.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AssignInstructorsRequest(@NotNull @NotEmpty List<Long> instructorUserIds) {}

