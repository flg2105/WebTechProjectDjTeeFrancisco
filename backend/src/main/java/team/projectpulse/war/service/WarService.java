package team.projectpulse.war.service;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.projectpulse.auth.service.CurrentUserSecurity;
import team.projectpulse.section.domain.ActiveWeek;
import team.projectpulse.section.repository.ActiveWeekRepository;
import team.projectpulse.system.ApiException;
import team.projectpulse.system.StatusCode;
import team.projectpulse.team.domain.Team;
import team.projectpulse.team.domain.TeamMembership;
import team.projectpulse.team.repository.TeamMembershipRepository;
import team.projectpulse.team.repository.TeamRepository;
import team.projectpulse.user.domain.UserRole;
import team.projectpulse.user.repository.UserRepository;
import team.projectpulse.war.domain.WarActivity;
import team.projectpulse.war.domain.WarEntry;
import team.projectpulse.war.dto.WarActivityRequest;
import team.projectpulse.war.dto.WarActivityResponse;
import team.projectpulse.war.dto.WarEntryResponse;
import team.projectpulse.war.dto.WarStudentReportResponse;
import team.projectpulse.war.repository.WarEntryRepository;

@Service
@Transactional(readOnly = true)
public class WarService {
  private final WarEntryRepository warEntryRepository;
  private final ActiveWeekRepository activeWeekRepository;
  private final TeamRepository teamRepository;
  private final TeamMembershipRepository teamMembershipRepository;
  private final UserRepository userRepository;
  private final CurrentUserSecurity currentUserSecurity;

  public WarService(
      WarEntryRepository warEntryRepository,
      ActiveWeekRepository activeWeekRepository,
      TeamRepository teamRepository,
      TeamMembershipRepository teamMembershipRepository,
      UserRepository userRepository,
      CurrentUserSecurity currentUserSecurity) {
    this.warEntryRepository = warEntryRepository;
    this.activeWeekRepository = activeWeekRepository;
    this.teamRepository = teamRepository;
    this.teamMembershipRepository = teamMembershipRepository;
    this.userRepository = userRepository;
    this.currentUserSecurity = currentUserSecurity;
  }

  public WarEntryResponse findWar(Long studentUserId, Long activeWeekId) {
    currentUserSecurity.requireCurrentUser(studentUserId);
    WarContext context = resolveContext(studentUserId, activeWeekId);
    return warEntryRepository.findByActiveWeekIdAndStudentUserId(activeWeekId, studentUserId)
        .map(entry -> toResponse(entry, context.activeWeek()))
        .orElseGet(() -> emptyResponse(context.activeWeek(), context.teamId(), studentUserId));
  }

  public WarStudentReportResponse findStudentReport(Long studentUserId, Long startActiveWeekId, Long endActiveWeekId) {
    validatePositive(studentUserId, "studentUserId");
    validatePositive(startActiveWeekId, "startActiveWeekId");
    validatePositive(endActiveWeekId, "endActiveWeekId");
    validateStudent(studentUserId);

    ActiveWeek startWeek = getActiveWeek(startActiveWeekId);
    ActiveWeek endWeek = getActiveWeek(endActiveWeekId);
    validateWeek(startWeek);
    validateWeek(endWeek);
    validateSameSection(startWeek, endWeek);
    validateChronologicalRange(startWeek, endWeek);

    Long sectionId = startWeek.getSectionId();
    Long teamId = resolveTeamId(studentUserId, sectionId);

    List<ActiveWeek> weeksInRange = activeWeekRepository.findBySectionIdOrderByWeekStartDateAsc(sectionId).stream()
        .filter(ActiveWeek::isActive)
        .filter(week -> !week.getWeekStartDate().isBefore(startWeek.getWeekStartDate()))
        .filter(week -> !week.getWeekStartDate().isAfter(endWeek.getWeekStartDate()))
        .toList();

    List<WarEntryResponse> entries = weeksInRange.stream()
        .map(week -> warEntryRepository.findByActiveWeekIdAndStudentUserId(week.getId(), studentUserId)
            .map(entry -> toResponse(entry, week))
            .orElseGet(() -> emptyResponse(week, teamId, studentUserId)))
        .toList();

    boolean hasAnyActivity = entries.stream().anyMatch(entry -> !entry.activities().isEmpty());
    if (!hasAnyActivity) {
      throw new ApiException(StatusCode.NOT_FOUND, "No WAR report data available for the selected period");
    }

    return new WarStudentReportResponse(
        studentUserId,
        sectionId,
        startActiveWeekId,
        endActiveWeekId,
        startWeek.getWeekStartDate(),
        endWeek.getWeekStartDate(),
        entries);
  }

  @Transactional
  public WarEntryResponse addActivity(WarActivityRequest request) {
    currentUserSecurity.requireCurrentUser(request.studentUserId());
    WarContext context = resolveContext(request.studentUserId(), request.activeWeekId());
    WarEntry entry = warEntryRepository.findByActiveWeekIdAndStudentUserId(request.activeWeekId(), request.studentUserId())
        .orElseGet(() -> createEntry(request.studentUserId(), context));

    WarActivity activity = new WarActivity();
    apply(activity, request);
    entry.addActivity(activity);
    return toResponse(warEntryRepository.save(entry), context.activeWeek());
  }

  @Transactional
  public WarEntryResponse updateActivity(Long activityId, WarActivityRequest request) {
    currentUserSecurity.requireCurrentUser(request.studentUserId());
    WarContext context = resolveContext(request.studentUserId(), request.activeWeekId());
    WarEntry entry = getExistingEntry(request.studentUserId(), request.activeWeekId());
    WarActivity activity = getActivity(entry, activityId);
    apply(activity, request);
    return toResponse(warEntryRepository.save(entry), context.activeWeek());
  }

  @Transactional
  public WarEntryResponse deleteActivity(Long activityId, Long studentUserId, Long activeWeekId) {
    currentUserSecurity.requireCurrentUser(studentUserId);
    WarContext context = resolveContext(studentUserId, activeWeekId);
    WarEntry entry = getExistingEntry(studentUserId, activeWeekId);
    WarActivity activity = getActivity(entry, activityId);
    entry.removeActivity(activity);
    return toResponse(warEntryRepository.save(entry), context.activeWeek());
  }

  private WarContext resolveContext(Long studentUserId, Long activeWeekId) {
    validatePositive(studentUserId, "studentUserId");
    validatePositive(activeWeekId, "activeWeekId");
    validateStudent(studentUserId);
    ActiveWeek activeWeek = getActiveWeek(activeWeekId);
    validateWeek(activeWeek);
    Long teamId = resolveTeamId(studentUserId, activeWeek.getSectionId());
    return new WarContext(activeWeek, teamId);
  }

  private WarEntry createEntry(Long studentUserId, WarContext context) {
    WarEntry entry = new WarEntry();
    entry.setActiveWeekId(context.activeWeek().getId());
    entry.setTeamId(context.teamId());
    entry.setStudentUserId(studentUserId);
    return entry;
  }

  private WarEntry getExistingEntry(Long studentUserId, Long activeWeekId) {
    return warEntryRepository.findByActiveWeekIdAndStudentUserId(activeWeekId, studentUserId)
        .orElseThrow(() -> new ApiException(StatusCode.NOT_FOUND, "WAR not found for the selected week"));
  }

  private WarActivity getActivity(WarEntry entry, Long activityId) {
    return entry.getActivities().stream()
        .filter(activity -> activity.getId().equals(activityId))
        .findFirst()
        .orElseThrow(() -> new ApiException(StatusCode.NOT_FOUND, "WAR activity not found with id " + activityId));
  }

  private ActiveWeek getActiveWeek(Long activeWeekId) {
    return activeWeekRepository.findById(activeWeekId)
        .orElseThrow(() -> new ApiException(StatusCode.NOT_FOUND, "Active week not found with id " + activeWeekId));
  }

  private void validateWeek(ActiveWeek activeWeek) {
    if (!activeWeek.isActive()) {
      throw new ApiException(StatusCode.INVALID_ARGUMENT, "Selected week is inactive");
    }
    if (activeWeek.getWeekStartDate().isAfter(LocalDate.now())) {
      throw new ApiException(StatusCode.INVALID_ARGUMENT, "Selected week cannot be in the future");
    }
  }

  private void validateSameSection(ActiveWeek startWeek, ActiveWeek endWeek) {
    if (!startWeek.getSectionId().equals(endWeek.getSectionId())) {
      throw new ApiException(StatusCode.INVALID_ARGUMENT, "Start and end weeks must be in the same section");
    }
  }

  private void validateChronologicalRange(ActiveWeek startWeek, ActiveWeek endWeek) {
    if (startWeek.getWeekStartDate().isAfter(endWeek.getWeekStartDate())) {
      throw new ApiException(StatusCode.INVALID_ARGUMENT, "Start week must be on or before end week");
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

  private Long resolveTeamId(Long studentUserId, Long sectionId) {
    List<TeamMembership> memberships = teamMembershipRepository.findByStudentUserIdOrderByTeamIdAsc(studentUserId);
    if (memberships.isEmpty()) {
      throw new ApiException(StatusCode.CONFLICT, "Student is not assigned to a team for this section");
    }

    Set<Long> membershipTeamIds = memberships.stream()
        .map(TeamMembership::getTeamId)
        .collect(LinkedHashSet::new, Set::add, Set::addAll);

    List<Team> sectionTeams = teamRepository.findAllById(List.copyOf(membershipTeamIds)).stream()
        .filter(team -> team.getSectionId().equals(sectionId))
        .sorted((left, right) -> left.getName().compareToIgnoreCase(right.getName()))
        .toList();

    if (sectionTeams.isEmpty()) {
      throw new ApiException(StatusCode.CONFLICT, "Student is not assigned to a team for this section");
    }
    if (sectionTeams.size() > 1) {
      throw new ApiException(StatusCode.CONFLICT, "Student is assigned to multiple teams in this section");
    }
    return sectionTeams.get(0).getId();
  }

  private void validatePositive(Long value, String field) {
    if (value == null || value <= 0) {
      throw new ApiException(StatusCode.INVALID_ARGUMENT, field + " must be positive");
    }
  }

  private void apply(WarActivity activity, WarActivityRequest request) {
    activity.setCategory(request.category());
    activity.setActivity(request.activity().trim());
    activity.setDescription(request.description().trim());
    activity.setHoursPlanned(request.hoursPlanned());
    activity.setHoursActual(request.hoursActual());
    activity.setStatus(request.status());
  }

  private WarEntryResponse emptyResponse(ActiveWeek activeWeek, Long teamId, Long studentUserId) {
    return new WarEntryResponse(
        null,
        activeWeek.getId(),
        activeWeek.getWeekStartDate(),
        teamId,
        studentUserId,
        null,
        List.of());
  }

  private WarEntryResponse toResponse(WarEntry entry, ActiveWeek activeWeek) {
    List<WarActivityResponse> activities = entry.getActivities().stream()
        .map(activity -> new WarActivityResponse(
            activity.getId(),
            activity.getCategory(),
            activity.getActivity(),
            activity.getDescription(),
            activity.getHoursPlanned(),
            activity.getHoursActual(),
            activity.getStatus()))
        .toList();

    return new WarEntryResponse(
        entry.getId(),
        entry.getActiveWeekId(),
        activeWeek.getWeekStartDate(),
        entry.getTeamId(),
        entry.getStudentUserId(),
        entry.getSubmittedAt(),
        activities);
  }

  private record WarContext(ActiveWeek activeWeek, Long teamId) {}
}
