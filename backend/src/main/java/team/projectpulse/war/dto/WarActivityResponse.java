package team.projectpulse.war.dto;

import java.math.BigDecimal;
import team.projectpulse.war.domain.WarActivityCategory;
import team.projectpulse.war.domain.WarActivityStatus;

public record WarActivityResponse(
    Long id,
    WarActivityCategory category,
    String activity,
    String description,
    BigDecimal hoursPlanned,
    BigDecimal hoursActual,
    WarActivityStatus status) {}
