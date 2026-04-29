package team.projectpulse.section.dto;

import java.util.List;

public record SectionRubricResponse(
    Long id,
    String name,
    List<SectionRubricCriterionResponse> criteria) {}
