package team.projectpulse.team.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.projectpulse.section.repository.SectionRepository;
import team.projectpulse.section.repository.SectionInstructorAssignmentRepository;
import team.projectpulse.system.ApiException;
import team.projectpulse.system.StatusCode;
import team.projectpulse.team.domain.Team;
import team.projectpulse.team.domain.TeamInstructorAssignment;
import team.projectpulse.team.domain.TeamMembership;
import team.projectpulse.team.dto.AssignInstructorsRequest;
import team.projectpulse.team.dto.AssignStudentRequest;
import team.projectpulse.team.dto.TeamRequest;
import team.projectpulse.team.dto.TeamResponse;
import team.projectpulse.team.repository.TeamInstructorAssignmentRepository;
import team.projectpulse.team.repository.TeamMembershipRepository;
import team.projectpulse.team.repository.TeamRepository;
import team.projectpulse.user.domain.UserRole;
import team.projectpulse.user.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class TeamService {
  private final TeamRepository teamRepository;
  private final TeamMembershipRepository teamMembershipRepository;
  private final TeamInstructorAssignmentRepository teamInstructorAssignmentRepository;
  private final SectionRepository sectionRepository;
  private final SectionInstructorAssignmentRepository sectionInstructorAssignmentRepository;
  private final UserRepository userRepository;

  public TeamService(
      TeamRepository teamRepository,
      TeamMembershipRepository teamMembershipRepository,
      TeamInstructorAssignmentRepository teamInstructorAssignmentRepository,
      SectionRepository sectionRepository,
      SectionInstructorAssignmentRepository sectionInstructorAssignmentRepository,
      UserRepository userRepository) {
    this.teamRepository = teamRepository;
    this.teamMembershipRepository = teamMembershipRepository;
    this.teamInstructorAssignmentRepository = teamInstructorAssignmentRepository;
    this.sectionRepository = sectionRepository;
    this.sectionInstructorAssignmentRepository = sectionInstructorAssignmentRepository;
    this.userRepository = userRepository;
  }

  public List<TeamResponse> findAll(Long sectionId) {
    List<Team> teams = sectionId == null
        ? teamRepository.findAllByOrderByNameAsc()
        : teamRepository.findBySectionIdOrderByNameAsc(sectionId);
    return teams.stream().map(this::toResponse).toList();
  }

  public TeamResponse findById(Long id) {
    return toResponse(getTeam(id));
  }

  @Transactional
  public TeamResponse create(TeamRequest request) {
    validateSection(request.sectionId());
    String name = request.name().trim();
    if (teamRepository.existsBySectionIdAndNameIgnoreCase(request.sectionId(), name)) {
      throw new ApiException(StatusCode.CONFLICT, "Team name already exists in this section");
    }

    Team team = new Team();
    team.setSectionId(request.sectionId());
    team.setName(name);
    return toResponse(teamRepository.save(team));
  }

  @Transactional
  public TeamResponse update(Long id, TeamRequest request) {
    validateSection(request.sectionId());
    Team team = getTeam(id);
    team.setSectionId(request.sectionId());
    team.setName(request.name().trim());
    team.touch();
    return toResponse(teamRepository.save(team));
  }

  @Transactional
  public void delete(Long id) {
    Team team = getTeam(id);
    if (teamMembershipRepository.existsByTeamId(id)) {
      throw new ApiException(StatusCode.CONFLICT, "Remove students before deleting this team");
    }
    teamRepository.delete(team);
  }

  @Transactional
  public TeamResponse assignStudent(Long teamId, AssignStudentRequest request) {
    Team team = getTeam(teamId);
    validateStudent(request.studentUserId());
    if (!teamMembershipRepository.existsByTeamIdAndStudentUserId(teamId, request.studentUserId())) {
      TeamMembership membership = new TeamMembership();
      membership.setTeamId(teamId);
      membership.setStudentUserId(request.studentUserId());
      teamMembershipRepository.save(membership);
    }
    team.touch();
    return toResponse(teamRepository.save(team));
  }

  @Transactional
  public TeamResponse removeStudent(Long teamId, Long studentUserId) {
    Team team = getTeam(teamId);
    teamMembershipRepository.deleteByTeamIdAndStudentUserId(teamId, studentUserId);
    team.touch();
    return toResponse(teamRepository.save(team));
  }

  @Transactional
  public TeamResponse assignInstructors(Long teamId, AssignInstructorsRequest request) {
    Team team = getTeam(teamId);
    for (Long instructorUserId : request.instructorUserIds()) {
      validateInstructor(instructorUserId);
      validateInstructorInSection(instructorUserId, team.getSectionId());
      if (!teamInstructorAssignmentRepository.existsByTeamIdAndInstructorUserId(teamId, instructorUserId)) {
        TeamInstructorAssignment assignment = new TeamInstructorAssignment();
        assignment.setTeamId(teamId);
        assignment.setInstructorUserId(instructorUserId);
        teamInstructorAssignmentRepository.save(assignment);
      }
    }
    team.touch();
    return toResponse(teamRepository.save(team));
  }

  @Transactional
  public TeamResponse removeInstructor(Long teamId, Long instructorUserId) {
    Team team = getTeam(teamId);
    teamInstructorAssignmentRepository.deleteByTeamIdAndInstructorUserId(teamId, instructorUserId);
    team.touch();
    return toResponse(teamRepository.save(team));
  }

  private Team getTeam(Long id) {
    return teamRepository.findById(id)
        .orElseThrow(() -> new ApiException(StatusCode.NOT_FOUND, "Team not found with id " + id));
  }

  private void validateSection(Long sectionId) {
    if (!sectionRepository.existsById(sectionId)) {
      throw new ApiException(StatusCode.NOT_FOUND, "Section not found with id " + sectionId);
    }
  }

  private void validateStudent(Long studentUserId) {
    boolean validStudent = userRepository.findById(studentUserId)
        .map(user -> user.getRole() == UserRole.STUDENT)
        .orElse(false);
    if (!validStudent) {
      throw new ApiException(StatusCode.NOT_FOUND, "Student not found with id " + studentUserId);
    }
  }

  private void validateInstructor(Long instructorUserId) {
    boolean validInstructor = userRepository.findById(instructorUserId)
        .map(user -> user.getRole() == UserRole.INSTRUCTOR)
        .orElse(false);
    if (!validInstructor) {
      throw new ApiException(StatusCode.NOT_FOUND, "Instructor not found with id " + instructorUserId);
    }
  }

  private void validateInstructorInSection(Long instructorUserId, Long sectionId) {
    if (!sectionInstructorAssignmentRepository.existsBySectionIdAndInstructorUserId(sectionId, instructorUserId)) {
      throw new ApiException(
          StatusCode.INVALID_ARGUMENT,
          "Instructor %d is not assigned to section %d".formatted(instructorUserId, sectionId));
    }
  }

  private TeamResponse toResponse(Team team) {
    List<Long> studentUserIds = teamMembershipRepository.findByTeamIdOrderByStudentUserIdAsc(team.getId())
        .stream()
        .map(TeamMembership::getStudentUserId)
        .toList();
    List<Long> instructorUserIds = teamInstructorAssignmentRepository.findByTeamIdOrderByInstructorUserIdAsc(team.getId())
        .stream()
        .map(TeamInstructorAssignment::getInstructorUserId)
        .toList();
    return new TeamResponse(
        team.getId(),
        team.getSectionId(),
        team.getName(),
        team.getCreatedAt(),
        team.getUpdatedAt(),
        studentUserIds,
        instructorUserIds);
  }
}
