package team.projectpulse.user.dto;

public record InstructorSupervisedTeamResponse(
    Long sectionId,
    String sectionName,
    Long teamId,
    String teamName) {}
