package team.projectpulse.war.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import team.projectpulse.war.domain.WarActivityCategory;
import team.projectpulse.war.domain.WarActivityStatus;
import java.math.BigDecimal;

public record WarActivityRequest(
    @NotNull Long studentUserId,
    @NotNull Long activeWeekId,
    @NotNull WarActivityCategory category,
    @NotBlank String activity,
    @NotBlank String description,
    @NotNull @DecimalMin("0.0") BigDecimal hoursPlanned,
    @NotNull @DecimalMin("0.0") BigDecimal hoursActual,
    @NotNull WarActivityStatus status) {}
