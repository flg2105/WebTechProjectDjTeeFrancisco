package team.projectpulse.user.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.projectpulse.peereval.domain.PeerEvaluationEntry;
import team.projectpulse.peereval.domain.PeerEvaluationSubmission;
import team.projectpulse.peereval.repository.PeerEvaluationSubmissionRepository;
import team.projectpulse.section.domain.ActiveWeek;
import team.projectpulse.section.domain.Section;
import team.projectpulse.section.repository.ActiveWeekRepository;
import team.projectpulse.team.repository.TeamMembershipRepository;
import team.projectpulse.team.domain.Team;
import team.projectpulse.team.domain.TeamInstructorAssignment;
import team.projectpulse.team.domain.TeamMembership;
import team.projectpulse.team.repository.TeamInstructorAssignmentRepository;
import team.projectpulse.team.repository.TeamRepository;
import team.projectpulse.section.repository.SectionRepository;
import team.projectpulse.system.ApiException;
import team.projectpulse.system.StatusCode;
import team.projectpulse.user.domain.Invitation;
import team.projectpulse.user.domain.ProjectUser;
import team.projectpulse.user.domain.UserRole;
import team.projectpulse.user.domain.UserStatus;
import team.projectpulse.user.dto.DeactivateInstructorRequest;
import team.projectpulse.user.dto.EditAccountRequest;
import team.projectpulse.user.dto.InstructorDetailsResponse;
import team.projectpulse.user.dto.InstructorSearchResultResponse;
import team.projectpulse.user.dto.InstructorSupervisedTeamResponse;
import team.projectpulse.user.dto.InvitationRequest;
import team.projectpulse.user.dto.InvitationResponse;
import team.projectpulse.user.dto.SetupAccountRequest;
import team.projectpulse.user.dto.StudentDetailsResponse;
import team.projectpulse.user.dto.StudentPeerEvaluationSummaryResponse;
import team.projectpulse.user.dto.StudentSearchResultResponse;
import team.projectpulse.user.dto.StudentWarSummaryResponse;
import team.projectpulse.user.dto.UserResponse;
import team.projectpulse.user.repository.InvitationRepository;
import team.projectpulse.user.repository.UserRepository;
import team.projectpulse.war.domain.WarEntry;
import team.projectpulse.war.repository.WarEntryRepository;

@Service
@Transactional(readOnly = true)
public class UserService {
  private final UserRepository userRepository;
  private final InvitationRepository invitationRepository;
  private final SectionRepository sectionRepository;
  private final ActiveWeekRepository activeWeekRepository;
  private final TeamMembershipRepository teamMembershipRepository;
  private final TeamRepository teamRepository;
  private final TeamInstructorAssignmentRepository teamInstructorAssignmentRepository;
  private final WarEntryRepository warEntryRepository;
  private final PeerEvaluationSubmissionRepository peerEvaluationSubmissionRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(
      UserRepository userRepository,
      InvitationRepository invitationRepository,
      SectionRepository sectionRepository,
      ActiveWeekRepository activeWeekRepository,
      TeamMembershipRepository teamMembershipRepository,
      TeamRepository teamRepository,
      TeamInstructorAssignmentRepository teamInstructorAssignmentRepository,
      WarEntryRepository warEntryRepository,
      PeerEvaluationSubmissionRepository peerEvaluationSubmissionRepository,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.invitationRepository = invitationRepository;
    this.sectionRepository = sectionRepository;
    this.activeWeekRepository = activeWeekRepository;
    this.teamMembershipRepository = teamMembershipRepository;
    this.teamRepository = teamRepository;
    this.teamInstructorAssignmentRepository = teamInstructorAssignmentRepository;
    this.warEntryRepository = warEntryRepository;
    this.peerEvaluationSubmissionRepository = peerEvaluationSubmissionRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public List<UserResponse> findAll(UserRole role) {
    List<ProjectUser> users = role == null
        ? userRepository.findAllByOrderByDisplayNameAsc()
        : userRepository.findByRoleOrderByDisplayNameAsc(role);
    return users.stream().map(this::toResponse).toList();
  }

  @Transactional
  public UserResponse editAccount(Long id, EditAccountRequest request) {
    if (id == null || id <= 0) {
      throw new ApiException(StatusCode.INVALID_ARGUMENT, "id must be positive");
    }
    ProjectUser user = userRepository.findById(id)
        .orElseThrow(() -> new ApiException(StatusCode.NOT_FOUND, "User not found with id " + id));

    String email = request.email().trim().toLowerCase();
    if (!Objects.equals(user.getEmail(), email) && userRepository.existsByEmailIgnoreCase(email)) {
      throw new ApiException(StatusCode.CONFLICT, "User already exists with email " + email);
    }

    user.setEmail(email);
    user.setDisplayName(request.displayName().trim());
    user.touch();
    return toResponse(userRepository.save(user));
  }

  @Transactional
  public InvitationResponse inviteStudents(InvitationRequest request) {
    if (request.sectionId() == null) {
      throw new ApiException(StatusCode.INVALID_ARGUMENT, "sectionId is required");
    }
    if (request.sectionId() <= 0) {
      throw new ApiException(StatusCode.INVALID_ARGUMENT, "sectionId must be positive");
    }
    if (!sectionRepository.existsById(request.sectionId())) {
      throw new ApiException(StatusCode.NOT_FOUND, "Section not found with id " + request.sectionId());
    }
    return invite(request, UserRole.STUDENT);
  }

  @Transactional
  public InvitationResponse inviteInstructors(InvitationRequest request) {
    return invite(new InvitationRequest(null, request.emails()), UserRole.INSTRUCTOR);
  }

  @Transactional
  public UserResponse setupStudent(SetupAccountRequest request) {
    return setupAccount(request, UserRole.STUDENT);
  }

  @Transactional
  public UserResponse setupInstructor(SetupAccountRequest request) {
    return setupAccount(request, UserRole.INSTRUCTOR);
  }

  @Transactional
  public UserResponse createStudent(SetupAccountRequest request) {
    return setupAccount(request, UserRole.STUDENT);
  }

  @Transactional
  public UserResponse createInstructor(SetupAccountRequest request) {
    return setupAccount(request, UserRole.INSTRUCTOR);
  }

  public List<StudentSearchResultResponse> findStudents(String q) {
    String query = normalizeQuery(q);
    return userRepository.searchByRole(UserRole.STUDENT, query).stream()
        .map(this::toStudentSearchResult)
        .toList();
  }

  public StudentDetailsResponse viewStudent(Long id) {
    if (id == null || id <= 0) {
      throw new ApiException(StatusCode.INVALID_ARGUMENT, "id must be positive");
    }
    ProjectUser user = userRepository.findById(id)
        .filter(found -> found.getRole() == UserRole.STUDENT)
        .orElseThrow(() -> new ApiException(StatusCode.NOT_FOUND, "Student not found with id " + id));
    return toStudentDetails(user);
  }

  public List<InstructorSearchResultResponse> findInstructors(
      String firstName,
      String lastName,
      String teamName,
      UserStatus status) {
    String normalizedFirstName = normalizeQuery(firstName);
    String normalizedLastName = normalizeQuery(lastName);
    String normalizedTeamName = normalizeQuery(teamName);

    return userRepository.findByRoleOrderByDisplayNameAsc(UserRole.INSTRUCTOR).stream()
        .map(this::toInstructorSearchCandidate)
        .filter(candidate -> matchesInstructorSearch(
            candidate,
            normalizedFirstName,
            normalizedLastName,
            normalizedTeamName,
            status))
        .sorted(Comparator
            .comparingInt(InstructorSearchCandidate::latestAcademicYearStart)
            .reversed()
            .thenComparing(candidate -> sortText(candidate.nameParts().lastName()))
            .thenComparing(candidate -> sortText(candidate.nameParts().firstName()))
            .thenComparing(candidate -> sortText(candidate.user().getDisplayName())))
        .map(InstructorSearchCandidate::response)
        .toList();
  }

  public InstructorDetailsResponse viewInstructor(Long id) {
    return toInstructorDetails(requireInstructor(id));
  }

  @Transactional
  public UserResponse deactivateInstructor(Long id, DeactivateInstructorRequest request) {
    ProjectUser instructor = requireInstructor(id);
    String reason = request.reason().trim();
    if (reason.isEmpty()) {
      throw new ApiException(StatusCode.INVALID_ARGUMENT, "reason is required");
    }
    if (instructor.getStatus() != UserStatus.ACTIVE) {
      throw new ApiException(StatusCode.CONFLICT, "Only active instructors can be deactivated");
    }

    instructor.setStatus(UserStatus.INACTIVE);
    instructor.touch();
    return toResponse(userRepository.save(instructor));
  }

  @Transactional
  public UserResponse reactivateInstructor(Long id) {
    ProjectUser instructor = requireInstructor(id);
    if (instructor.getStatus() != UserStatus.INACTIVE) {
      throw new ApiException(StatusCode.CONFLICT, "Only inactive instructors can be reactivated");
    }

    instructor.setStatus(UserStatus.ACTIVE);
    instructor.touch();
    return toResponse(userRepository.save(instructor));
  }

  @Transactional
  public boolean deleteStudent(Long id) {
    if (id == null || id <= 0) {
      throw new ApiException(StatusCode.INVALID_ARGUMENT, "id must be positive");
    }
    ProjectUser student = userRepository.findById(id)
        .filter(found -> found.getRole() == UserRole.STUDENT)
        .orElseThrow(() -> new ApiException(StatusCode.NOT_FOUND, "Student not found with id " + id));

    deletePeerEvaluationsForStudent(id);
    warEntryRepository.deleteByStudentUserId(id);
    teamMembershipRepository.deleteByStudentUserId(id);
    invitationRepository.deleteByEmailIgnoreCaseAndRole(student.getEmail(), UserRole.STUDENT);
    userRepository.delete(student);
    return true;
  }

  private void deletePeerEvaluationsForStudent(Long studentUserId) {
    peerEvaluationSubmissionRepository.deleteAll(peerEvaluationSubmissionRepository
        .findByEvaluatorStudentUserIdOrderByWeekStartDateDesc(studentUserId));

    List<PeerEvaluationSubmission> submissions = peerEvaluationSubmissionRepository
        .findDistinctByEntriesEvaluateeStudentUserIdOrderByWeekStartDateDesc(studentUserId);
    for (PeerEvaluationSubmission submission : submissions) {
      List<PeerEvaluationEntry> entriesToRemove = new ArrayList<>(submission.getEntries().stream()
          .filter(entry -> Objects.equals(entry.getEvaluateeStudentUserId(), studentUserId))
          .toList());
      entriesToRemove.forEach(submission::removeEntry);
      if (submission.getEntries().isEmpty()) {
        peerEvaluationSubmissionRepository.delete(submission);
      }
    }
  }

  private InvitationResponse invite(InvitationRequest request, UserRole role) {
    List<UserResponse> users = request.emails().stream()
        .map(email -> inviteOne(email, role, request.sectionId()))
        .toList();
    return new InvitationResponse(role, request.sectionId(), users);
  }

  private UserResponse inviteOne(String rawEmail, UserRole role, Long sectionId) {
    String email = rawEmail.trim().toLowerCase();
    ProjectUser user = userRepository.findByEmailIgnoreCase(email).orElseGet(ProjectUser::new);
    user.setEmail(email);
    user.setDisplayName(displayNameFromEmail(email));
    user.setRole(role);
    if (user.getStatus() == null || user.getStatus() != UserStatus.ACTIVE) {
      user.setStatus(UserStatus.INVITED);
    }
    user.touch();
    ProjectUser saved = userRepository.save(user);

    Invitation invitation = new Invitation();
    invitation.setEmail(email);
    invitation.setRole(role);
    invitation.setSectionId(sectionId);
    invitationRepository.save(invitation);

    return toResponse(saved);
  }

  private UserResponse setupAccount(SetupAccountRequest request, UserRole role) {
    String email = request.email().trim().toLowerCase();
    ProjectUser user = userRepository.findByEmailIgnoreCase(email).orElseGet(ProjectUser::new);
    if (user.getRole() != null && user.getRole() != role) {
      throw new ApiException(StatusCode.CONFLICT, "User already exists with email " + email);
    }
    user.setEmail(email);
    user.setDisplayName(request.displayName().trim());
    user.setPasswordHash(passwordEncoder.encode(request.password()));
    user.setRole(role);
    user.setStatus(UserStatus.ACTIVE);
    user.touch();
    return toResponse(userRepository.save(user));
  }

  private String normalizeQuery(String q) {
    if (q == null) {
      return null;
    }
    String trimmed = q.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String displayNameFromEmail(String email) {
    int at = email.indexOf('@');
    return at > 0 ? email.substring(0, at) : email;
  }

  private ProjectUser requireInstructor(Long id) {
    if (id == null || id <= 0) {
      throw new ApiException(StatusCode.INVALID_ARGUMENT, "id must be positive");
    }
    return userRepository.findById(id)
        .filter(found -> found.getRole() == UserRole.INSTRUCTOR)
        .orElseThrow(() -> new ApiException(StatusCode.NOT_FOUND, "Instructor not found with id " + id));
  }

  private StudentSearchResultResponse toStudentSearchResult(ProjectUser user) {
    StudentPlacement placement = resolveStudentPlacement(user);
    return new StudentSearchResultResponse(
        user.getId(),
        user.getEmail(),
        user.getDisplayName(),
        placement.sectionId(),
        placement.teamId(),
        placement.sectionName(),
        placement.teamName());
  }

  private StudentDetailsResponse toStudentDetails(ProjectUser user) {
    NameParts nameParts = splitName(user.getDisplayName());
    StudentPlacement placement = resolveStudentPlacement(user);

    List<WarEntry> warEntries = warEntryRepository.findByStudentUserIdOrderByActiveWeekIdDesc(user.getId());
    Map<Long, ActiveWeek> activeWeeksById = activeWeekRepository.findAllById(
        warEntries.stream().map(WarEntry::getActiveWeekId).distinct().toList()).stream()
        .collect(Collectors.toMap(ActiveWeek::getId, Function.identity()));
    Map<Long, Team> warTeamsById = teamRepository.findAllById(
        warEntries.stream().map(WarEntry::getTeamId).distinct().toList()).stream()
        .collect(Collectors.toMap(Team::getId, Function.identity()));

    List<StudentWarSummaryResponse> wars = warEntries.stream()
        .map(entry -> {
          ActiveWeek activeWeek = activeWeeksById.get(entry.getActiveWeekId());
          Team team = warTeamsById.get(entry.getTeamId());
          return new StudentWarSummaryResponse(
              entry.getId(),
              entry.getActiveWeekId(),
              activeWeek != null ? activeWeek.getWeekStartDate() : null,
              entry.getTeamId(),
              team != null ? team.getName() : "Team " + entry.getTeamId(),
              entry.getSubmittedAt(),
              entry.getActivities().size());
        })
        .toList();

    List<PeerEvaluationSubmission> receivedSubmissions = peerEvaluationSubmissionRepository
        .findDistinctByEntriesEvaluateeStudentUserIdOrderByWeekStartDateDesc(user.getId());
    Map<Long, ProjectUser> evaluatorsById = userRepository.findAllById(receivedSubmissions.stream()
        .map(PeerEvaluationSubmission::getEvaluatorStudentUserId)
        .distinct()
        .toList()).stream()
        .collect(Collectors.toMap(ProjectUser::getId, Function.identity()));
    Map<Long, Team> peerTeamsById = teamRepository.findAllById(receivedSubmissions.stream()
        .map(PeerEvaluationSubmission::getTeamId)
        .distinct()
        .toList()).stream()
        .collect(Collectors.toMap(Team::getId, Function.identity()));

    List<StudentPeerEvaluationSummaryResponse> peerEvaluations = receivedSubmissions.stream()
        .flatMap(submission -> submission.getEntries().stream()
            .filter(entry -> Objects.equals(entry.getEvaluateeStudentUserId(), user.getId()))
            .map(entry -> toPeerEvaluationSummary(entry, submission, evaluatorsById, peerTeamsById)))
        .sorted(Comparator
            .comparing(StudentPeerEvaluationSummaryResponse::weekStartDate, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(response -> sortText(response.evaluatorDisplayName())))
        .toList();

    return new StudentDetailsResponse(
        user.getId(),
        user.getEmail(),
        user.getDisplayName(),
        nameParts.firstName(),
        nameParts.lastName(),
        placement.sectionId(),
        placement.teamId(),
        placement.sectionName(),
        placement.teamName(),
        wars,
        peerEvaluations);
  }

  private InstructorDetailsResponse toInstructorDetails(ProjectUser user) {
    NameParts nameParts = splitName(user.getDisplayName());

    List<Long> teamIds = teamInstructorAssignmentRepository.findByInstructorUserIdOrderByTeamIdAsc(user.getId())
        .stream()
        .map(TeamInstructorAssignment::getTeamId)
        .distinct()
        .toList();

    Map<Long, Team> teamsById = teamRepository.findAllById(teamIds).stream()
        .collect(Collectors.toMap(Team::getId, team -> team));
    List<Long> sectionIds = teamsById.values().stream()
        .map(Team::getSectionId)
        .distinct()
        .toList();
    Map<Long, Section> sectionsById = sectionRepository.findAllById(sectionIds).stream()
        .collect(Collectors.toMap(Section::getId, section -> section));

    List<InstructorSupervisedTeamResponse> supervisedTeams = teamIds.stream()
        .map(teamsById::get)
        .filter(Objects::nonNull)
        .map(team -> {
          Section section = sectionsById.get(team.getSectionId());
          return new InstructorSupervisedTeamResponse(
              team.getSectionId(),
              section != null ? section.getName() : "Section " + team.getSectionId(),
              team.getId(),
              team.getName());
        })
        .sorted(Comparator
            .comparing((InstructorSupervisedTeamResponse response) -> sortText(response.sectionName()))
            .thenComparing(response -> sortText(response.teamName())))
        .toList();

    return new InstructorDetailsResponse(
        user.getId(),
        user.getEmail(),
        user.getDisplayName(),
        nameParts.firstName(),
        nameParts.lastName(),
        user.getStatus(),
        supervisedTeams);
  }

  private NameParts splitName(String displayName) {
    String normalized = displayName == null ? "" : displayName.trim();
    if (normalized.isEmpty()) {
      return new NameParts("", "");
    }
    int lastSpace = normalized.lastIndexOf(' ');
    if (lastSpace < 0) {
      return new NameParts(normalized, "");
    }
    return new NameParts(
        normalized.substring(0, lastSpace).trim(),
        normalized.substring(lastSpace + 1).trim());
  }

  private String sortText(String value) {
    return value == null ? "" : value.trim().toLowerCase();
  }

  private InstructorSearchCandidate toInstructorSearchCandidate(ProjectUser user) {
    NameParts nameParts = splitName(user.getDisplayName());

    List<Long> teamIds = teamInstructorAssignmentRepository.findByInstructorUserIdOrderByTeamIdAsc(user.getId())
        .stream()
        .map(TeamInstructorAssignment::getTeamId)
        .distinct()
        .toList();

    Map<Long, Team> teamsById = teamRepository.findAllById(teamIds).stream()
        .collect(Collectors.toMap(Team::getId, Function.identity()));
    List<Long> sectionIds = teamsById.values().stream()
        .map(Team::getSectionId)
        .distinct()
        .toList();
    Map<Long, Section> sectionsById = sectionRepository.findAllById(sectionIds).stream()
        .collect(Collectors.toMap(Section::getId, Function.identity()));

    List<InstructorSupervisedTeamResponse> supervisedTeams = teamIds.stream()
        .map(teamsById::get)
        .filter(Objects::nonNull)
        .map(team -> {
          Section section = sectionsById.get(team.getSectionId());
          return new InstructorSupervisedTeamResponse(
              team.getSectionId(),
              section != null ? section.getName() : "Section " + team.getSectionId(),
              team.getId(),
              team.getName());
        })
        .sorted(Comparator
            .comparing((InstructorSupervisedTeamResponse response) -> sortSectionAcademicYear(response.sectionId(), sectionsById))
            .reversed()
            .thenComparing(response -> sortText(response.sectionName()))
            .thenComparing(response -> sortText(response.teamName())))
        .toList();

    int latestAcademicYearStart = supervisedTeams.stream()
        .map(InstructorSupervisedTeamResponse::sectionId)
        .map(sectionsById::get)
        .filter(Objects::nonNull)
        .map(Section::getAcademicYear)
        .mapToInt(this::academicYearStart)
        .max()
        .orElse(-1);

    InstructorSearchResultResponse response = new InstructorSearchResultResponse(
        user.getId(),
        user.getEmail(),
        user.getDisplayName(),
        nameParts.firstName(),
        nameParts.lastName(),
        user.getStatus(),
        supervisedTeams);

    return new InstructorSearchCandidate(user, nameParts, supervisedTeams, latestAcademicYearStart, response);
  }

  private boolean matchesInstructorSearch(
      InstructorSearchCandidate candidate,
      String firstName,
      String lastName,
      String teamName,
      UserStatus status) {
    if (status != null && candidate.user().getStatus() != status) {
      return false;
    }
    if (firstName != null && !sortText(candidate.nameParts().firstName()).contains(sortText(firstName))) {
      return false;
    }
    if (lastName != null && !sortText(candidate.nameParts().lastName()).contains(sortText(lastName))) {
      return false;
    }
    if (teamName != null && candidate.supervisedTeams().stream()
        .noneMatch(team -> sortText(team.teamName()).contains(sortText(teamName)))) {
      return false;
    }
    return true;
  }

  private int academicYearStart(String academicYear) {
    if (academicYear == null) {
      return -1;
    }
    String trimmed = academicYear.trim();
    if (trimmed.length() < 4) {
      return -1;
    }
    try {
      return Integer.parseInt(trimmed.substring(0, 4));
    } catch (NumberFormatException ex) {
      return -1;
    }
  }

  private int sortSectionAcademicYear(Long sectionId, Map<Long, Section> sectionsById) {
    Section section = sectionsById.get(sectionId);
    return section == null ? -1 : academicYearStart(section.getAcademicYear());
  }

  private StudentPlacement resolveStudentPlacement(ProjectUser user) {
    List<TeamMembership> memberships = teamMembershipRepository.findByStudentUserIdOrderByTeamIdAsc(user.getId());
    if (!memberships.isEmpty()) {
      Long teamId = memberships.get(0).getTeamId();
      Team team = teamRepository.findById(teamId).orElse(null);
      if (team != null) {
        Section section = sectionRepository.findById(team.getSectionId()).orElse(null);
        return new StudentPlacement(
            team.getSectionId(),
            section != null ? section.getName() : "Section " + team.getSectionId(),
            team.getId(),
            team.getName());
      }
    }

    return invitationRepository.findFirstByEmailIgnoreCaseAndRole(user.getEmail(), UserRole.STUDENT)
        .filter(invitation -> invitation.getSectionId() != null)
        .map(invitation -> {
          Section section = sectionRepository.findById(invitation.getSectionId()).orElse(null);
          return new StudentPlacement(
              invitation.getSectionId(),
              section != null ? section.getName() : "Section " + invitation.getSectionId(),
              null,
              null);
        })
        .orElseGet(() -> new StudentPlacement(null, null, null, null));
  }

  private StudentPeerEvaluationSummaryResponse toPeerEvaluationSummary(
      PeerEvaluationEntry entry,
      PeerEvaluationSubmission submission,
      Map<Long, ProjectUser> evaluatorsById,
      Map<Long, Team> teamsById) {
    ProjectUser evaluator = evaluatorsById.get(submission.getEvaluatorStudentUserId());
    Team team = teamsById.get(submission.getTeamId());
    return new StudentPeerEvaluationSummaryResponse(
        entry.getId(),
        submission.getId(),
        submission.getWeekStartDate(),
        submission.getEvaluatorStudentUserId(),
        evaluator != null ? evaluator.getDisplayName() : "Student " + submission.getEvaluatorStudentUserId(),
        submission.getTeamId(),
        team != null ? team.getName() : "Team " + submission.getTeamId(),
        averageScore(entry),
        entry.getPublicComment(),
        entry.getPrivateComment());
  }

  private BigDecimal averageScore(PeerEvaluationEntry entry) {
    if (entry.getScores().isEmpty()) {
      return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
    BigDecimal total = entry.getScores().stream()
        .map(score -> score.getScore())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    return total.divide(BigDecimal.valueOf(entry.getScores().size()), 2, RoundingMode.HALF_UP);
  }

  private UserResponse toResponse(ProjectUser user) {
    return new UserResponse(
        user.getId(),
        user.getEmail(),
        user.getDisplayName(),
        user.getRole(),
        user.getStatus(),
        user.getCreatedAt(),
        user.getUpdatedAt());
  }

  private record NameParts(String firstName, String lastName) {}

  private record StudentPlacement(
      Long sectionId,
      String sectionName,
      Long teamId,
      String teamName) {}

  private record InstructorSearchCandidate(
      ProjectUser user,
      NameParts nameParts,
      List<InstructorSupervisedTeamResponse> supervisedTeams,
      int latestAcademicYearStart,
      InstructorSearchResultResponse response) {}
}
