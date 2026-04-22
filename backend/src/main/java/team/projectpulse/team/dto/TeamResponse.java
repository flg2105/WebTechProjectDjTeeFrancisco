package team.projectpulse.team.dto;

import java.time.Instant;
import java.util.List;

public record TeamResponse(
    Long id,
    Long sectionId,
    String name,
    Instant createdAt,
    Instant updatedAt,
    List<Long> studentUserIds) {}
