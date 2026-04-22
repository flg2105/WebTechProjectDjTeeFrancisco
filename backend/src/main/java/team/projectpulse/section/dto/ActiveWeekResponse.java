package team.projectpulse.section.dto;

import java.time.LocalDate;

public record ActiveWeekResponse(
    Long id,
    LocalDate weekStartDate,
    boolean active) {}
