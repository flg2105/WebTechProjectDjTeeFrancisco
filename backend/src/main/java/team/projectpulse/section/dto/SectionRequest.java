package team.projectpulse.section.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record SectionRequest(
    @NotBlank String name,
    @NotBlank String academicYear,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    @NotNull Long rubricId) {}
