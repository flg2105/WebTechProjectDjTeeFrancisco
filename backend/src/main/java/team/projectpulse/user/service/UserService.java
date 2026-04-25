package team.projectpulse.user.service;

import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.projectpulse.section.repository.SectionRepository;
import team.projectpulse.system.ApiException;
import team.projectpulse.system.StatusCode;
import team.projectpulse.user.domain.Invitation;
import team.projectpulse.user.domain.ProjectUser;
import team.projectpulse.user.domain.UserRole;
import team.projectpulse.user.domain.UserStatus;
import team.projectpulse.user.dto.EditAccountRequest;
import team.projectpulse.user.dto.InstructorSearchResultResponse;
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

  public UserService(
      UserRepository userRepository,
      InvitationRepository invitationRepository,
      SectionRepository sectionRepository) {
    this.userRepository = userRepository;
    this.invitationRepository = invitationRepository;
    this.sectionRepository = sectionRepository;
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
}
