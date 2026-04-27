package team.projectpulse.peereval.dto;

public record PeerEvaluationSectionMissingSubmissionResponse(
    Long studentUserId,
    String studentDisplayName,
    Long teamId,
    String teamName) {
}
