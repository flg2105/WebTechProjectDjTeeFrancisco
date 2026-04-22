package team.projectpulse.rubric.dto;

import java.math.BigDecimal;

public record RubricCriterionResponse(
    Long id,
    String name,
    String description,
    BigDecimal maxScore,
    Integer position
) {}
