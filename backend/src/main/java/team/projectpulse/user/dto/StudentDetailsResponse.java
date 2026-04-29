package team.projectpulse.user.dto;

import java.util.List;

public record StudentDetailsResponse(
    Long id,
    String email,
    String displayName,
    String firstName,
    String lastName,
    Long sectionId,
    Long teamId,
    String sectionName,
    String teamName,
    List<StudentWarSummaryResponse> wars,
    List<StudentPeerEvaluationSummaryResponse> peerEvaluations) {}
