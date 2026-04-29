package team.projectpulse.section.service;

import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.projectpulse.rubric.domain.Rubric;
import team.projectpulse.rubric.domain.RubricCriterion;
import team.projectpulse.rubric.repository.RubricRepository;
import team.projectpulse.section.domain.ActiveWeek;
import team.projectpulse.section.domain.Section;
import team.projectpulse.section.domain.SectionInstructorAssignment;
import team.projectpulse.section.dto.ActiveWeekRequest;
import team.projectpulse.section.dto.ActiveWeekResponse;
import team.projectpulse.section.dto.AssignSectionInstructorsRequest;
import team.projectpulse.section.dto.SectionDetailsResponse;
import team.projectpulse.section.dto.SectionRequest;
import team.projectpulse.section.dto.SectionRubricCriterionResponse;
import team.projectpulse.section.dto.SectionRubricResponse;
import team.projectpulse.section.dto.SectionResponse;
import team.projectpulse.section.dto.SectionTeamDetailsResponse;
import team.projectpulse.section.dto.SectionUserSummaryResponse;
import team.projectpulse.section.repository.ActiveWeekRepository;
import team.projectpulse.section.repository.SectionInstructorAssignmentRepository;
import team.projectpulse.section.repository.SectionRepository;
import team.projectpulse.system.ApiException;
import team.projectpulse.team.domain.Team;
import team.projectpulse.team.domain.TeamInstructorAssignment;
import team.projectpulse.team.domain.TeamMembership;
import team.projectpulse.team.repository.TeamInstructorAssignmentRepository;
import team.projectpulse.team.repository.TeamMembershipRepository;
import team.projectpulse.team.repository.TeamRepository;
import team.projectpulse.system.StatusCode;
import team.projectpulse.user.domain.Invitation;
import team.projectpulse.user.domain.ProjectUser;
import team.projectpulse.user.domain.UserRole;
import team.projectpulse.user.domain.UserStatus;
import team.projectpulse.user.repository.InvitationRepository;
import team.projectpulse.user.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class SectionService {
  private final SectionRepository sectionRepository;
  private final ActiveWeekRepository activeWeekRepository;
  private final RubricRepository rubricRepository;
  private final SectionInstructorAssignmentRepository sectionInstructorAssignmentRepository;
  private final UserRepository userRepository;
  private final TeamRepository teamRepository;
  private final TeamMembershipRepository teamMembershipRepository;
  private final TeamInstructorAssignmentRepository teamInstructorAssignmentRepository;
  private final InvitationRepository invitationRepository;

  public SectionService(
      SectionRepository sectionRepository,
      ActiveWeekRepository activeWeekRepository,
      RubricRepository rubricRepository,
      SectionInstructorAssignmentRepository sectionInstructorAssignmentRepository,
      UserRepository userRepository,
      TeamRepository teamRepository,
      TeamMembershipRepository teamMembershipRepository,
      TeamInstructorAssignmentRepository teamInstructorAssignmentRepository,
      InvitationRepository invitationRepository) {
    this.sectionRepository = sectionRepository;
    this.activeWeekRepository = activeWeekRepository;
    this.rubricRepository = rubricRepository;
    this.sectionInstructorAssignmentRepository = sectionInstructorAssignmentRepository;
    this.userRepository = userRepository;
    this.teamRepository = teamRepository;
    this.teamMembershipRepository = teamMembershipRepository;
    this.teamInstructorAssignmentRepository = teamInstructorAssignmentRepository;
    this.invitationRepository = invitationRepository;
  }

  public List<SectionResponse> findAll(String name) {
    List<Section> sections = name == null || name.isBlank()
        ? sectionRepository.findAllByOrderByNameDesc()
        : sectionRepository.findByNameContainingIgnoreCaseOrderByNameDesc(name.trim());

    return sections.stream().map(this::toSummaryResponse).toList();
  }

  public SectionDetailsResponse findById(Long id) {
    return toDetailsResponse(getSection(id));
  }

  @Transactional
  public SectionResponse create(SectionRequest request) {
    validateDates(request);
    validateRubric(request.rubricId());

    String name = request.name().trim();
    String academicYear = request.academicYear().trim();
    if (sectionRepository.existsByNameIgnoreCaseAndAcademicYearIgnoreCase(name, academicYear)) {
      throw new ApiException(StatusCode.CONFLICT, "Section name already exists for academic year");
    }

    Section section = new Section();
    apply(section, request);
    return toSummaryResponse(sectionRepository.save(section));
  }

  @Transactional
  public SectionResponse update(Long id, SectionRequest request) {
    validateDates(request);
    validateRubric(request.rubricId());

    Section section = getSection(id);
    apply(section, request);
    section.touch();
    return toSummaryResponse(sectionRepository.save(section));
  }

  @Transactional
  public SectionResponse replaceActiveWeeks(Long sectionId, List<ActiveWeekRequest> requests) {
    Section section = getSection(sectionId);
    for (ActiveWeekRequest request : requests) {
      if (request.weekStartDate().getDayOfWeek() != DayOfWeek.MONDAY) {
        throw new ApiException(StatusCode.INVALID_ARGUMENT, "Active week start date must be a Monday");
      }
    }

    activeWeekRepository.deleteBySectionId(sectionId);
    List<ActiveWeek> activeWeeks = requests.stream()
        .sorted(Comparator.comparing(ActiveWeekRequest::weekStartDate))
        .map(request -> {
          ActiveWeek week = new ActiveWeek();
          week.setSectionId(sectionId);
          week.setWeekStartDate(request.weekStartDate());
          week.setActive(request.active());
          return week;
        })
        .toList();
    activeWeekRepository.saveAll(activeWeeks);
    section.touch();
    return toSummaryResponse(sectionRepository.save(section));
  }

  @Transactional
  public SectionResponse assignInstructors(Long sectionId, AssignSectionInstructorsRequest request) {
    Section section = getSection(sectionId);
    for (Long instructorUserId : request.instructorUserIds()) {
      validateInstructor(instructorUserId);
      if (!sectionInstructorAssignmentRepository.existsBySectionIdAndInstructorUserId(sectionId, instructorUserId)) {
        SectionInstructorAssignment assignment = new SectionInstructorAssignment();
        assignment.setSectionId(sectionId);
        assignment.setInstructorUserId(instructorUserId);
        sectionInstructorAssignmentRepository.save(assignment);
      }
    }
    section.touch();
    return toSummaryResponse(sectionRepository.save(section));
  }

  @Transactional
  public SectionResponse removeInstructor(Long sectionId, Long instructorUserId) {
    Section section = getSection(sectionId);
    sectionInstructorAssignmentRepository.deleteBySectionIdAndInstructorUserId(sectionId, instructorUserId);
    section.touch();
    return toSummaryResponse(sectionRepository.save(section));
  }

  private Section getSection(Long id) {
    return sectionRepository.findById(id)
        .orElseThrow(() -> new ApiException(StatusCode.NOT_FOUND, "Section not found with id " + id));
  }

  private void apply(Section section, SectionRequest request) {
    section.setName(request.name().trim());
    section.setAcademicYear(request.academicYear().trim());
    section.setStartDate(request.startDate());
    section.setEndDate(request.endDate());
    section.setRubricId(request.rubricId());
  }

  private void validateDates(SectionRequest request) {
    if (request.endDate().isBefore(request.startDate())) {
      throw new ApiException(StatusCode.INVALID_ARGUMENT, "Section end date must be on or after start date");
    }
  }

  private void validateRubric(Long rubricId) {
    if (!rubricRepository.existsById(rubricId)) {
      throw new ApiException(StatusCode.NOT_FOUND, "Rubric not found with id " + rubricId);
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

  private SectionResponse toSummaryResponse(Section section) {
    List<ActiveWeekResponse> activeWeeks = activeWeekRepository.findBySectionIdOrderByWeekStartDateAsc(section.getId())
        .stream()
        .map(week -> new ActiveWeekResponse(week.getId(), week.getWeekStartDate(), week.isActive()))
        .toList();

    List<Long> instructorUserIds = sectionInstructorAssignmentRepository
        .findBySectionIdOrderByInstructorUserIdAsc(section.getId())
        .stream()
        .map(SectionInstructorAssignment::getInstructorUserId)
        .toList();

    return new SectionResponse(
        section.getId(),
        section.getName(),
        section.getAcademicYear(),
        section.getStartDate(),
        section.getEndDate(),
        section.getRubricId(),
        section.getCreatedAt(),
        section.getUpdatedAt(),
        activeWeeks,
        instructorUserIds);
  }

  private SectionDetailsResponse toDetailsResponse(Section section) {
    Long sectionId = section.getId();
    List<ActiveWeekResponse> activeWeeks = activeWeekRepository.findBySectionIdOrderByWeekStartDateAsc(sectionId)
        .stream()
        .map(week -> new ActiveWeekResponse(week.getId(), week.getWeekStartDate(), week.isActive()))
        .toList();

    List<Long> instructorUserIds = sectionInstructorAssignmentRepository
        .findBySectionIdOrderByInstructorUserIdAsc(sectionId)
        .stream()
        .map(SectionInstructorAssignment::getInstructorUserId)
        .toList();

    Map<Long, ProjectUser> instructorsById = userRepository.findAllById(instructorUserIds).stream()
        .collect(Collectors.toMap(ProjectUser::getId, Function.identity()));

    List<Team> teams = teamRepository.findBySectionIdOrderByNameAsc(sectionId);
    List<Long> teamIds = teams.stream().map(Team::getId).toList();

    Map<Long, List<TeamMembership>> membershipsByTeamId = teamIds.stream()
        .collect(Collectors.toMap(
            Function.identity(),
            teamId -> teamMembershipRepository.findByTeamIdOrderByStudentUserIdAsc(teamId)));

    Map<Long, List<TeamInstructorAssignment>> instructorAssignmentsByTeamId = teamIds.stream()
        .collect(Collectors.toMap(
            Function.identity(),
            teamId -> teamInstructorAssignmentRepository.findByTeamIdOrderByInstructorUserIdAsc(teamId)));

    Set<Long> assignedStudentIds = membershipsByTeamId.values().stream()
        .flatMap(List::stream)
        .map(TeamMembership::getStudentUserId)
        .collect(Collectors.toSet());
    Set<Long> assignedInstructorIds = instructorAssignmentsByTeamId.values().stream()
        .flatMap(List::stream)
        .map(TeamInstructorAssignment::getInstructorUserId)
        .collect(Collectors.toSet());

    Set<Long> teamStudentIds = new java.util.HashSet<>(assignedStudentIds);
    Set<Long> allStudentIds = new java.util.HashSet<>(assignedStudentIds);
    for (Invitation invitation : invitationRepository.findBySectionIdAndRole(sectionId, UserRole.STUDENT)) {
      userRepository.findByEmailIgnoreCase(invitation.getEmail())
          .map(ProjectUser::getId)
          .ifPresent(allStudentIds::add);
    }

    Map<Long, ProjectUser> studentsById = userRepository.findAllById(allStudentIds).stream()
        .collect(Collectors.toMap(ProjectUser::getId, Function.identity()));

    List<SectionTeamDetailsResponse> teamDetails = teams.stream()
        .map(team -> {
          List<SectionUserSummaryResponse> members = membershipsByTeamId.getOrDefault(team.getId(), List.of()).stream()
              .map(TeamMembership::getStudentUserId)
              .map(studentsById::get)
              .filter(Objects::nonNull)
              .map(this::toUserSummary)
              .toList();

          List<SectionUserSummaryResponse> teamInstructors =
              instructorAssignmentsByTeamId.getOrDefault(team.getId(), List.of()).stream()
                  .map(TeamInstructorAssignment::getInstructorUserId)
                  .map(instructorsById::get)
                  .filter(Objects::nonNull)
                  .map(this::toUserSummary)
                  .toList();

          return new SectionTeamDetailsResponse(team.getId(), team.getName(), members, teamInstructors);
        })
        .toList();

    List<SectionUserSummaryResponse> unassignedInstructors = instructorUserIds.stream()
        .filter(instructorUserId -> !assignedInstructorIds.contains(instructorUserId))
        .map(instructorsById::get)
        .filter(Objects::nonNull)
        .map(this::toUserSummary)
        .toList();

    List<SectionUserSummaryResponse> unassignedStudents = allStudentIds.stream()
        .filter(studentUserId -> !teamStudentIds.contains(studentUserId))
        .map(studentsById::get)
        .filter(Objects::nonNull)
        .sorted(Comparator.comparing(ProjectUser::getDisplayName, String.CASE_INSENSITIVE_ORDER))
        .map(this::toUserSummary)
        .toList();

    SectionRubricResponse rubricUsed = rubricRepository.findById(section.getRubricId())
        .map(this::toRubricResponse)
        .orElse(null);

    return new SectionDetailsResponse(
        section.getId(),
        section.getName(),
        section.getAcademicYear(),
        section.getStartDate(),
        section.getEndDate(),
        section.getRubricId(),
        section.getCreatedAt(),
        section.getUpdatedAt(),
        activeWeeks,
        instructorUserIds,
        teamDetails,
        unassignedInstructors,
        unassignedStudents,
        rubricUsed);
  }

  private SectionUserSummaryResponse toUserSummary(ProjectUser user) {
    UserStatus status = user.getStatus();
    return new SectionUserSummaryResponse(user.getId(), user.getDisplayName(), user.getEmail(), status);
  }

  private SectionRubricResponse toRubricResponse(Rubric rubric) {
    List<SectionRubricCriterionResponse> criteria = rubric.getCriteria().stream()
        .sorted(Comparator.comparing(RubricCriterion::getPosition))
        .map(criterion -> new SectionRubricCriterionResponse(
            criterion.getId(),
            criterion.getName(),
            criterion.getDescription(),
            criterion.getMaxScore(),
            criterion.getPosition()))
        .toList();
    return new SectionRubricResponse(rubric.getId(), rubric.getName(), criteria);
  }
}
