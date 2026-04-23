package team.projectpulse.section.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ActiveWeekRequest(
    @NotNull LocalDate weekStartDate,
    boolean active) {}
