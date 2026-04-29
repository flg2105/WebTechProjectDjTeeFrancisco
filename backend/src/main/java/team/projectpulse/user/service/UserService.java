package team.projectpulse.user.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import team.projectpulse.section.domain.Section;
import team.projectpulse.team.repository.TeamMembershipRepository;
import team.projectpulse.team.domain.Team;
import team.projectpulse.team.domain.TeamInstructorAssignment;
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
import team.projectpulse.user.dto.StudentSearchResultResponse;
import team.projectpulse.user.dto.UserResponse;
import team.projectpulse.user.repository.InvitationRepository;
import team.projectpulse.user.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class UserService {
  private final UserRepository userRepository;
  private final InvitationRepository invitationRepository;
  private final SectionRepository sectionRepository;
  private final TeamMembershipRepository teamMembershipRepository;
  private final TeamRepository teamRepository;
  private final TeamInstructorAssignmentRepository teamInstructorAssignmentRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(
      UserRepository userRepository,
      InvitationRepository invitationRepository,
      SectionRepository sectionRepository,
      TeamMembershipRepository teamMembershipRepository,
      TeamRepository teamRepository,
      TeamInstructorAssignmentRepository teamInstructorAssignmentRepository,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.invitationRepository = invitationRepository;
    this.sectionRepository = sectionRepository;
    this.teamMembershipRepository = teamMembershipRepository;
    this.teamRepository = teamRepository;
    this.teamInstructorAssignmentRepository = teamInstructorAssignmentRepository;
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

  public List<InstructorSearchResultResponse> findInstructors(String q) {
    String query = normalizeQuery(q);
    return userRepository.searchByRole(UserRole.INSTRUCTOR, query).stream()
        .map(this::toInstructorSearchResult)
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

    teamMembershipRepository.deleteByStudentUserId(id);
    invitationRepository.deleteByEmailIgnoreCaseAndRole(student.getEmail(), UserRole.STUDENT);
    try {
      userRepository.delete(student);
      userRepository.flush();
      return true;
    } catch (DataIntegrityViolationException ex) {
      student.setStatus(UserStatus.INACTIVE);
      student.touch();
      userRepository.save(student);
      return false;
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
    return new StudentSearchResultResponse(
        user.getId(),
        user.getEmail(),
        user.getDisplayName(),
        null,
        null,
        null,
        null);
  }

  private StudentDetailsResponse toStudentDetails(ProjectUser user) {
    return new StudentDetailsResponse(
        user.getId(),
        user.getEmail(),
        user.getDisplayName(),
        null,
        null,
        null,
        null,
        List.of(),
        List.of());
  }

  private InstructorSearchResultResponse toInstructorSearchResult(ProjectUser user) {
    return new InstructorSearchResultResponse(
        user.getId(),
        user.getEmail(),
        user.getDisplayName(),
        user.getStatus(),
        null,
        null);
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
}
