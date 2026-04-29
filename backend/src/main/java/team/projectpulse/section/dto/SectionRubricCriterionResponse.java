package team.projectpulse.section.dto;

import java.math.BigDecimal;

public record SectionRubricCriterionResponse(
    Long id,
    String name,
    String description,
    BigDecimal maxScore,
    Integer position) {}
