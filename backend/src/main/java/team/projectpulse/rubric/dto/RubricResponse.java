package team.projectpulse.rubric.dto;

import java.time.Instant;
import java.util.List;

public record RubricResponse(
    Long id,
    String name,
    Instant createdAt,
    List<RubricCriterionResponse> criteria
) {}
