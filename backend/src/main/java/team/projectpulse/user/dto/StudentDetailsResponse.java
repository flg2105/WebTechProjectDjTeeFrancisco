package team.projectpulse.user.dto;

import java.util.List;

public record StudentDetailsResponse(
    Long id,
    String email,
    String displayName,
    Long sectionId,
    Long teamId,
    String sectionName,
    String teamName,
    List<Long> warEntryIds,
    List<Long> peerEvaluationIds) {}

