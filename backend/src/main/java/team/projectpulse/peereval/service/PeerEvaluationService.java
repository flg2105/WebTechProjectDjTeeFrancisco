package team.projectpulse.peereval.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.projectpulse.auth.service.CurrentUserSecurity;
import team.projectpulse.peereval.domain.PeerEvaluationEntry;
import team.projectpulse.peereval.domain.PeerEvaluationScore;
import team.projectpulse.peereval.domain.PeerEvaluationSubmission;
import team.projectpulse.peereval.dto.PeerEvaluationCriterionAverageResponse;
import team.projectpulse.peereval.dto.PeerEvaluationCriterionFormResponse;
import team.projectpulse.peereval.dto.PeerEvaluationCriterionScoreRequest;
import team.projectpulse.peereval.dto.PeerEvaluationEntryRequest;
import team.projectpulse.peereval.dto.PeerEvaluationFormResponse;
import team.projectpulse.peereval.dto.PeerEvaluationReportResponse;
import team.projectpulse.peereval.dto.PeerEvaluationSectionEvaluationDetailResponse;
import team.projectpulse.peereval.dto.PeerEvaluationSectionMissingSubmissionResponse;
import team.projectpulse.peereval.dto.PeerEvaluationSectionReportResponse;
import team.projectpulse.peereval.dto.PeerEvaluationSectionStudentReportResponse;
import team.projectpulse.peereval.dto.PeerEvaluationStudentEvaluationDetailResponse;
import team.projectpulse.peereval.dto.PeerEvaluationStudentReportResponse;
import team.projectpulse.peereval.dto.PeerEvaluationStudentWeeklyReportResponse;
import team.projectpulse.peereval.dto.PeerEvaluationSubmissionResponse;
import team.projectpulse.peereval.dto.PeerEvaluationTeammateResponse;
import team.projectpulse.peereval.dto.SubmitPeerEvaluationRequest;
import team.projectpulse.peereval.repository.PeerEvaluationSubmissionRepository;
import team.projectpulse.rubric.domain.Rubric;
import team.projectpulse.rubric.domain.RubricCriterion;
import team.projectpulse.rubric.repository.RubricRepository;
import team.projectpulse.section.domain.ActiveWeek;
import team.projectpulse.section.domain.Section;
import team.projectpulse.section.repository.ActiveWeekRepository;
import team.projectpulse.section.repository.SectionRepository;
import team.projectpulse.system.ApiException;
import team.projectpulse.system.StatusCode;
import team.projectpulse.team.domain.Team;
import team.projectpulse.team.domain.TeamMembership;
import team.projectpulse.team.repository.TeamMembershipRepository;
import team.projectpulse.team.repository.TeamRepository;
import team.projectpulse.user.domain.ProjectUser;
import team.projectpulse.user.domain.UserRole;
import team.projectpulse.user.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class PeerEvaluationService {
  private final PeerEvaluationSubmissionRepository submissionRepository;
  private final TeamMembershipRepository teamMembershipRepository;
  private final TeamRepository teamRepository;
  private final SectionRepository sectionRepository;
  private final ActiveWeekRepository activeWeekRepository;
  private final RubricRepository rubricRepository;
  private final UserRepository userRepository;
  private final CurrentUserSecurity currentUserSecurity;
  private final Clock clock = Clock.systemDefaultZone();

  public PeerEvaluationService(
      PeerEvaluationSubmissionRepository submissionRepository,
      TeamMembershipRepository teamMembershipRepository,
      TeamRepository teamRepository,
      SectionRepository sectionRepository,
      ActiveWeekRepository activeWeekRepository,
      RubricRepository rubricRepository,
      UserRepository userRepository,
      CurrentUserSecurity currentUserSecurity) {
    this.submissionRepository = submissionRepository;
    this.teamMembershipRepository = teamMembershipRepository;
    this.teamRepository = teamRepository;
    this.sectionRepository = sectionRepository;
    this.activeWeekRepository = activeWeekRepository;
    this.rubricRepository = rubricRepository;
    this.userRepository = userRepository;
    this.currentUserSecurity = currentUserSecurity;
  }

  public PeerEvaluationFormResponse findCurrent(Long studentUserId) {
    currentUserSecurity.requireCurrentUser(studentUserId);
    StudentContext context = loadStudentContext(studentUserId);
    LocalDate targetWeek = resolveCurrentSubmissionWeek(context.section().getId());
    List<RubricCriterion> criteria = loadRubricCriteria(context.section().getRubricId());

    return new PeerEvaluationFormResponse(
        context.student().getId(),
        context.student().getDisplayName(),
        context.section().getId(),
        context.team().getId(),
        targetWeek,
        submissionRepository.existsByEvaluatorStudentUserIdAndWeekStartDate(studentUserId, targetWeek),
        criteria.stream().map(this::toCriterionFormResponse).toList(),
        teammateResponses(context.memberships(), context.student().getId()));
  }

  @Transactional
  public PeerEvaluationSubmissionResponse submit(SubmitPeerEvaluationRequest request) {
    currentUserSecurity.requireCurrentUser(request.evaluatorStudentUserId());
    StudentContext context = loadStudentContext(request.evaluatorStudentUserId());
    LocalDate expectedWeek = resolveCurrentSubmissionWeek(context.section().getId());
    if (!expectedWeek.equals(request.weekStartDate())) {
      throw new ApiException(StatusCode.INVALID_ARGUMENT, "Peer evaluation must target the previous active week");
    }
    if (submissionRepository.existsByEvaluatorStudentUserIdAndWeekStartDate(
        request.evaluatorStudentUserId(), request.weekStartDate())) {
      throw new ApiException(StatusCode.CONFLICT, "Peer evaluation already submitted for this week");
    }

    List<RubricCriterion> criteria = loadRubricCriteria(context.section().getRubricId());
    validateEvaluations(request.evaluations(), context, criteria);

    PeerEvaluationSubmission submission = new PeerEvaluationSubmission();
    submission.setEvaluatorStudentUserId(context.student().getId());
    submission.setTeamId(context.team().getId());
    submission.setSectionId(context.section().getId());
    submission.setWeekStartDate(request.weekStartDate());

    for (PeerEvaluationEntryRequest evaluation : request.evaluations()) {
      PeerEvaluationEntry entry = new PeerEvaluationEntry();
      entry.setEvaluateeStudentUserId(evaluation.evaluateeStudentUserId());
      entry.setPublicComment(normalizeComment(evaluation.publicComment()));
      entry.setPrivateComment(normalizeComment(evaluation.privateComment()));

      for (PeerEvaluationCriterionScoreRequest scoreRequest : evaluation.scores()) {
        PeerEvaluationScore score = new PeerEvaluationScore();
        score.setRubricCriterionId(scoreRequest.rubricCriterionId());
        score.setScore(scoreRequest.score().setScale(2, RoundingMode.HALF_UP));
        entry.addScore(score);
      }

      submission.addEntry(entry);
    }

    PeerEvaluationSubmission saved = submissionRepository.save(submission);
    return new PeerEvaluationSubmissionResponse(
        saved.getId(),
        saved.getEvaluatorStudentUserId(),
        saved.getTeamId(),
        saved.getSectionId(),
        saved.getWeekStartDate(),
        saved.getEntries().size(),
        saved.getSubmittedAt());
  }

  public PeerEvaluationReportResponse findOwnReport(Long studentUserId, LocalDate weekStartDate) {
    currentUserSecurity.requireCurrentUser(studentUserId);
    StudentContext context = loadStudentContext(studentUserId);
    LocalDate targetWeek = weekStartDate == null
        ? resolveCurrentSubmissionWeek(context.section().getId())
        : validateActiveWeek(context.section().getId(), weekStartDate);

    List<RubricCriterion> criteria = loadRubricCriteria(context.section().getRubricId());
    List<PeerEvaluationEntry> receivedEntries = submissionRepository
        .findByTeamIdAndWeekStartDate(context.team().getId(), targetWeek)
        .stream()
        .filter(submission -> !Objects.equals(submission.getEvaluatorStudentUserId(), studentUserId))
        .flatMap(submission -> submission.getEntries().stream())
        .filter(entry -> Objects.equals(entry.getEvaluateeStudentUserId(), studentUserId))
        .toList();

    if (receivedEntries.isEmpty()) {
      throw new ApiException(StatusCode.NOT_FOUND, "No peer evaluation report available for this week");
    }

    Map<Long, List<BigDecimal>> scoresByCriterion = new HashMap<>();
    List<BigDecimal> totals = new ArrayList<>();
    List<String> publicComments = new ArrayList<>();

    for (PeerEvaluationEntry entry : receivedEntries) {
      BigDecimal total = BigDecimal.ZERO;
      for (PeerEvaluationScore score : entry.getScores()) {
        scoresByCriterion.computeIfAbsent(score.getRubricCriterionId(), ignored -> new ArrayList<>())
            .add(score.getScore());
        total = total.add(score.getScore());
      }
      totals.add(total);
      if (entry.getPublicComment() != null && !entry.getPublicComment().isBlank()) {
        publicComments.add(entry.getPublicComment());
      }
    }

    List<PeerEvaluationCriterionAverageResponse> criterionAverages = criteria.stream()
        .map(criterion -> new PeerEvaluationCriterionAverageResponse(
            criterion.getId(),
            criterion.getName(),
            criterion.getDescription(),
            criterion.getMaxScore(),
            criterion.getPosition(),
            average(scoresByCriterion.getOrDefault(criterion.getId(), List.of()))))
        .toList();

    return new PeerEvaluationReportResponse(
        context.student().getId(),
        context.student().getDisplayName(),
        context.section().getId(),
        context.team().getId(),
        targetWeek,
        average(totals),
        receivedEntries.size(),
        criterionAverages,
        publicComments);
  }

  public PeerEvaluationSectionReportResponse findSectionReport(Long sectionId, LocalDate weekStartDate) {
    validatePositive(sectionId, "sectionId");
    Section section = sectionRepository.findById(sectionId)
        .orElseThrow(() -> new ApiException(StatusCode.NOT_FOUND, "Section not found with id " + sectionId));
    LocalDate targetWeek = validateActiveWeek(sectionId, weekStartDate);

    List<Team> teams = teamRepository.findBySectionIdOrderByNameAsc(sectionId);
    Map<Long, Team> teamsById = new HashMap<>();
    Map<Long, Long> studentTeamIds = new HashMap<>();
    for (Team team : teams) {
      teamsById.put(team.getId(), team);
      for (TeamMembership membership : teamMembershipRepository.findByTeamIdOrderByStudentUserIdAsc(team.getId())) {
        studentTeamIds.put(membership.getStudentUserId(), team.getId());
      }
    }

    List<PeerEvaluationSubmission> submissions = submissionRepository.findBySectionIdAndWeekStartDate(sectionId, targetWeek);
    if (submissions.isEmpty()) {
      throw new ApiException(StatusCode.NOT_FOUND, "No peer evaluation report data available for this week");
    }

    Map<Long, ProjectUser> studentsById = loadStudents(studentTeamIds.keySet());
    Set<Long> submittingStudentIds = submissions.stream()
        .map(PeerEvaluationSubmission::getEvaluatorStudentUserId)
        .collect(LinkedHashSet::new, Set::add, Set::addAll);

    List<PeerEvaluationSectionStudentReportResponse> studentReports = studentTeamIds.keySet().stream()
        .map(studentUserId -> toSectionStudentReport(studentUserId, studentTeamIds, teamsById, studentsById, submissions))
        .sorted(sectionStudentComparator())
        .toList();

    List<PeerEvaluationSectionMissingSubmissionResponse> missingSubmitters = studentTeamIds.keySet().stream()
        .filter(studentUserId -> !submittingStudentIds.contains(studentUserId))
        .map(studentUserId -> toMissingSubmission(studentUserId, studentTeamIds, teamsById, studentsById))
        .sorted(sectionMissingComparator())
        .toList();

    return new PeerEvaluationSectionReportResponse(
        section.getId(),
        section.getName(),
        targetWeek,
        maxTotalScore(section.getRubricId()),
        studentReports,
        missingSubmitters);
  }

  public PeerEvaluationStudentReportResponse findStudentReport(Long studentUserId, Long startActiveWeekId, Long endActiveWeekId) {
    validatePositive(studentUserId, "studentUserId");
    validatePositive(startActiveWeekId, "startActiveWeekId");
    validatePositive(endActiveWeekId, "endActiveWeekId");

    StudentContext context = loadStudentContext(studentUserId);
    ActiveWeek startWeek = getActiveWeek(startActiveWeekId);
    ActiveWeek endWeek = getActiveWeek(endActiveWeekId);
    validateReportWeek(startWeek);
    validateReportWeek(endWeek);
    validateReportWeeksBelongToSection(context.section().getId(), startWeek, endWeek);
    validateReportRange(startWeek, endWeek);

    BigDecimal maxTotalScore = maxTotalScore(context.section().getRubricId());
    List<ActiveWeek> weeksInRange = activeWeekRepository.findBySectionIdOrderByWeekStartDateAsc(context.section().getId()).stream()
        .filter(ActiveWeek::isActive)
        .filter(week -> !week.getWeekStartDate().isBefore(startWeek.getWeekStartDate()))
        .filter(week -> !week.getWeekStartDate().isAfter(endWeek.getWeekStartDate()))
        .toList();

    Map<Long, ProjectUser> studentsById = loadStudents(context.memberships().stream()
        .map(TeamMembership::getStudentUserId)
        .collect(LinkedHashSet::new, Set::add, Set::addAll));

    List<PeerEvaluationStudentWeeklyReportResponse> weeklyReports = weeksInRange.stream()
        .map(week -> toWeeklyStudentReport(context, week.getWeekStartDate(), studentsById))
        .toList();

    boolean hasAnyEvaluation = weeklyReports.stream().anyMatch(report -> report.receivedEvaluations() > 0);
    if (!hasAnyEvaluation) {
      throw new ApiException(StatusCode.NOT_FOUND, "No peer evaluation report data available for the selected period");
    }

    return new PeerEvaluationStudentReportResponse(
        context.student().getId(),
        context.student().getDisplayName(),
        context.section().getId(),
        context.team().getId(),
        context.team().getName(),
        startActiveWeekId,
        endActiveWeekId,
        startWeek.getWeekStartDate(),
        endWeek.getWeekStartDate(),
        maxTotalScore,
        weeklyReports);
  }

  private PeerEvaluationStudentWeeklyReportResponse toWeeklyStudentReport(
      StudentContext context,
      LocalDate weekStartDate,
      Map<Long, ProjectUser> studentsById) {
    List<PeerEvaluationStudentEvaluationDetailResponse> evaluations = submissionRepository
        .findByTeamIdAndWeekStartDate(context.team().getId(), weekStartDate)
        .stream()
        .filter(submission -> !Objects.equals(submission.getEvaluatorStudentUserId(), context.student().getId()))
        .flatMap(submission -> submission.getEntries().stream()
            .filter(entry -> Objects.equals(entry.getEvaluateeStudentUserId(), context.student().getId()))
            .map(entry -> toStudentEvaluationDetail(submission, entry, studentsById)))
        .sorted(Comparator.comparing(
            PeerEvaluationStudentEvaluationDetailResponse::evaluatorDisplayName,
            String.CASE_INSENSITIVE_ORDER))
        .toList();

    List<BigDecimal> totals = evaluations.stream()
        .map(PeerEvaluationStudentEvaluationDetailResponse::totalScore)
        .toList();

    return new PeerEvaluationStudentWeeklyReportResponse(
        weekStartDate,
        average(totals),
        evaluations.size(),
        evaluations);
  }

  private PeerEvaluationStudentEvaluationDetailResponse toStudentEvaluationDetail(
      PeerEvaluationSubmission submission,
      PeerEvaluationEntry entry,
      Map<Long, ProjectUser> studentsById) {
    ProjectUser evaluator = studentsById.get(submission.getEvaluatorStudentUserId());
    if (evaluator == null) {
      throw new ApiException(StatusCode.NOT_FOUND, "Student not found with id " + submission.getEvaluatorStudentUserId());
    }

    BigDecimal totalScore = entry.getScores().stream()
        .map(PeerEvaluationScore::getScore)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .setScale(2, RoundingMode.HALF_UP);

    return new PeerEvaluationStudentEvaluationDetailResponse(
        submission.getEvaluatorStudentUserId(),
        evaluator.getDisplayName(),
        totalScore,
        entry.getPublicComment(),
        entry.getPrivateComment());
  }

  private void validateReportWeek(ActiveWeek activeWeek) {
    if (!activeWeek.isActive()) {
      throw new ApiException(StatusCode.INVALID_ARGUMENT, "Selected week is inactive");
    }
    if (activeWeek.getWeekStartDate().isAfter(LocalDate.now(clock))) {
      throw new ApiException(StatusCode.INVALID_ARGUMENT, "Selected week cannot be in the future");
    }
  }

  private ActiveWeek getActiveWeek(Long activeWeekId) {
    return activeWeekRepository.findById(activeWeekId)
        .orElseThrow(() -> new ApiException(StatusCode.NOT_FOUND, "Active week not found with id " + activeWeekId));
  }

  private void validateReportWeeksBelongToSection(Long sectionId, ActiveWeek startWeek, ActiveWeek endWeek) {
    if (!sectionId.equals(startWeek.getSectionId()) || !sectionId.equals(endWeek.getSectionId())) {
      throw new ApiException(StatusCode.INVALID_ARGUMENT, "Start and end weeks must belong to the student's section");
    }
  }

  private void validateReportRange(ActiveWeek startWeek, ActiveWeek endWeek) {
    if (startWeek.getWeekStartDate().isAfter(endWeek.getWeekStartDate())) {
      throw new ApiException(StatusCode.INVALID_ARGUMENT, "Start week must be on or before end week");
    }
  }

  private void validateEvaluations(
      List<PeerEvaluationEntryRequest> evaluations,
      StudentContext context,
      List<RubricCriterion> criteria) {
    Set<Long> expectedEvaluateeIds = context.memberships().stream()
        .map(TeamMembership::getStudentUserId)
        .filter(studentUserId -> !Objects.equals(studentUserId, context.student().getId()))
        .collect(LinkedHashSet::new, Set::add, Set::addAll);

    if (expectedEvaluateeIds.isEmpty()) {
      throw new ApiException(StatusCode.INVALID_ARGUMENT, "At least one teammate is required for peer evaluation");
    }

    Set<Long> actualEvaluateeIds = new LinkedHashSet<>();
    for (PeerEvaluationEntryRequest evaluation : evaluations) {
      if (Objects.equals(evaluation.evaluateeStudentUserId(), context.student().getId())) {
        throw new ApiException(StatusCode.INVALID_ARGUMENT, "Students cannot evaluate themselves");
      }
      if (!actualEvaluateeIds.add(evaluation.evaluateeStudentUserId())) {
        throw new ApiException(StatusCode.INVALID_ARGUMENT, "Each teammate can only be evaluated once");
      }
      validateScores(evaluation, criteria);
    }

    if (!actualEvaluateeIds.equals(expectedEvaluateeIds)) {
      throw new ApiException(StatusCode.INVALID_ARGUMENT, "Peer evaluation must include every teammate exactly once");
    }
  }

  private void validateScores(PeerEvaluationEntryRequest evaluation, List<RubricCriterion> criteria) {
    Map<Long, RubricCriterion> criteriaById = new HashMap<>();
    for (RubricCriterion criterion : criteria) {
      criteriaById.put(criterion.getId(), criterion);
    }

    Set<Long> scoreCriterionIds = new HashSet<>();
    for (PeerEvaluationCriterionScoreRequest scoreRequest : evaluation.scores()) {
      RubricCriterion criterion = criteriaById.get(scoreRequest.rubricCriterionId());
      if (criterion == null) {
        throw new ApiException(StatusCode.INVALID_ARGUMENT, "Unknown rubric criterion id " + scoreRequest.rubricCriterionId());
      }
      if (!scoreCriterionIds.add(scoreRequest.rubricCriterionId())) {
        throw new ApiException(StatusCode.INVALID_ARGUMENT, "Each rubric criterion can only be scored once per teammate");
      }
      if (scoreRequest.score().compareTo(criterion.getMaxScore()) > 0) {
        throw new ApiException(
            StatusCode.INVALID_ARGUMENT,
            "Score for criterion '%s' cannot exceed %s".formatted(criterion.getName(), criterion.getMaxScore()));
      }
    }

    if (scoreCriterionIds.size() != criteria.size()) {
      throw new ApiException(StatusCode.INVALID_ARGUMENT, "Each teammate evaluation must include every rubric criterion");
    }
  }

  private Map<Long, ProjectUser> loadStudents(Set<Long> studentUserIds) {
    Map<Long, ProjectUser> studentsById = new HashMap<>();
    for (ProjectUser student : userRepository.findAllById(studentUserIds)) {
      studentsById.put(student.getId(), student);
    }
    for (Long studentUserId : studentUserIds) {
      ProjectUser student = studentsById.get(studentUserId);
      if (student == null || student.getRole() != UserRole.STUDENT) {
        throw new ApiException(StatusCode.NOT_FOUND, "Student not found with id " + studentUserId);
      }
    }
    return studentsById;
  }

  private PeerEvaluationSectionStudentReportResponse toSectionStudentReport(
      Long studentUserId,
      Map<Long, Long> studentTeamIds,
      Map<Long, Team> teamsById,
      Map<Long, ProjectUser> studentsById,
      List<PeerEvaluationSubmission> submissions) {
    ProjectUser student = studentsById.get(studentUserId);
    Team team = requireTeam(studentTeamIds.get(studentUserId), teamsById);

    List<PeerEvaluationSectionEvaluationDetailResponse> evaluations = submissions.stream()
        .flatMap(submission -> submission.getEntries().stream()
            .filter(entry -> Objects.equals(entry.getEvaluateeStudentUserId(), studentUserId))
            .map(entry -> toEvaluationDetail(submission, entry, studentsById)))
        .sorted(Comparator.comparing(
            PeerEvaluationSectionEvaluationDetailResponse::evaluatorDisplayName,
            String.CASE_INSENSITIVE_ORDER))
        .toList();

    List<BigDecimal> totals = evaluations.stream()
        .map(PeerEvaluationSectionEvaluationDetailResponse::totalScore)
        .toList();

    return new PeerEvaluationSectionStudentReportResponse(
        student.getId(),
        student.getDisplayName(),
        team.getId(),
        team.getName(),
        average(totals),
        evaluations.size(),
        evaluations);
  }

  private PeerEvaluationSectionEvaluationDetailResponse toEvaluationDetail(
      PeerEvaluationSubmission submission,
      PeerEvaluationEntry entry,
      Map<Long, ProjectUser> studentsById) {
    ProjectUser evaluator = studentsById.get(submission.getEvaluatorStudentUserId());
    if (evaluator == null) {
      throw new ApiException(
          StatusCode.NOT_FOUND,
          "Student not found with id " + submission.getEvaluatorStudentUserId());
    }

    BigDecimal totalScore = entry.getScores().stream()
        .map(PeerEvaluationScore::getScore)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .setScale(2, RoundingMode.HALF_UP);

    return new PeerEvaluationSectionEvaluationDetailResponse(
        submission.getEvaluatorStudentUserId(),
        evaluator.getDisplayName(),
        totalScore,
        entry.getPublicComment(),
        entry.getPrivateComment());
  }

  private PeerEvaluationSectionMissingSubmissionResponse toMissingSubmission(
      Long studentUserId,
      Map<Long, Long> studentTeamIds,
      Map<Long, Team> teamsById,
      Map<Long, ProjectUser> studentsById) {
    ProjectUser student = studentsById.get(studentUserId);
    Team team = requireTeam(studentTeamIds.get(studentUserId), teamsById);
    return new PeerEvaluationSectionMissingSubmissionResponse(
        student.getId(),
        student.getDisplayName(),
        team.getId(),
        team.getName());
  }

  private Team requireTeam(Long teamId, Map<Long, Team> teamsById) {
    Team team = teamsById.get(teamId);
    if (team == null) {
      throw new ApiException(StatusCode.NOT_FOUND, "Team not found with id " + teamId);
    }
    return team;
  }

  private Comparator<PeerEvaluationSectionStudentReportResponse> sectionStudentComparator() {
    return Comparator
        .comparing(
            (PeerEvaluationSectionStudentReportResponse report) -> sortName(report.studentDisplayName()),
            String.CASE_INSENSITIVE_ORDER)
        .thenComparing(PeerEvaluationSectionStudentReportResponse::studentDisplayName, String.CASE_INSENSITIVE_ORDER);
  }

  private Comparator<PeerEvaluationSectionMissingSubmissionResponse> sectionMissingComparator() {
    return Comparator
        .comparing(
            (PeerEvaluationSectionMissingSubmissionResponse report) -> sortName(report.studentDisplayName()),
            String.CASE_INSENSITIVE_ORDER)
        .thenComparing(
            PeerEvaluationSectionMissingSubmissionResponse::studentDisplayName,
            String.CASE_INSENSITIVE_ORDER);
  }

  private BigDecimal maxTotalScore(Long rubricId) {
    return loadRubricCriteria(rubricId).stream()
        .map(RubricCriterion::getMaxScore)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .setScale(2, RoundingMode.HALF_UP);
  }

  private String sortName(String displayName) {
    String trimmed = displayName == null ? "" : displayName.trim();
    if (trimmed.isEmpty()) {
      return "";
    }
    String[] parts = trimmed.split("\\s+");
    return parts[parts.length - 1];
  }

  private StudentContext loadStudentContext(Long studentUserId) {
    ProjectUser student = userRepository.findById(studentUserId)
        .filter(user -> user.getRole() == UserRole.STUDENT)
        .orElseThrow(() -> new ApiException(StatusCode.NOT_FOUND, "Student not found with id " + studentUserId));

    List<TeamMembership> studentMemberships = teamMembershipRepository.findByStudentUserIdOrderByTeamIdAsc(studentUserId);
    if (studentMemberships.isEmpty()) {
      throw new ApiException(StatusCode.NOT_FOUND, "Student is not assigned to a team");
    }
    if (studentMemberships.size() > 1) {
      throw new ApiException(StatusCode.CONFLICT, "Student is assigned to multiple teams");
    }
    TeamMembership membership = studentMemberships.get(0);

    Team team = teamRepository.findById(membership.getTeamId())
        .orElseThrow(() -> new ApiException(StatusCode.NOT_FOUND, "Team not found with id " + membership.getTeamId()));
    Section section = sectionRepository.findById(team.getSectionId())
        .orElseThrow(() -> new ApiException(StatusCode.NOT_FOUND, "Section not found with id " + team.getSectionId()));
    List<TeamMembership> memberships = teamMembershipRepository.findByTeamIdOrderByStudentUserIdAsc(team.getId());

    return new StudentContext(student, team, section, memberships);
  }

  private LocalDate resolveCurrentSubmissionWeek(Long sectionId) {
    LocalDate currentWeekStart = LocalDate.now(clock).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    List<ActiveWeek> weeks = activeWeekRepository.findBySectionIdOrderByWeekStartDateAsc(sectionId);
    boolean currentWeekIsActive = weeks.stream()
        .anyMatch(week -> week.isActive() && currentWeekStart.equals(week.getWeekStartDate()));
    if (!currentWeekIsActive) {
      throw new ApiException(StatusCode.LOCKED, "Peer evaluations can only be submitted during an active week");
    }

    return weeks.stream()
        .filter(ActiveWeek::isActive)
        .map(ActiveWeek::getWeekStartDate)
        .filter(weekStartDate -> weekStartDate.isBefore(currentWeekStart))
        .max(LocalDate::compareTo)
        .orElseThrow(() -> new ApiException(StatusCode.NOT_FOUND, "No previous active week available for peer evaluation"));
  }

  private LocalDate validateActiveWeek(Long sectionId, LocalDate weekStartDate) {
    if (weekStartDate.getDayOfWeek() != DayOfWeek.MONDAY) {
      throw new ApiException(StatusCode.INVALID_ARGUMENT, "weekStartDate must be a Monday");
    }
    boolean validWeek = activeWeekRepository.findBySectionIdOrderByWeekStartDateAsc(sectionId).stream()
        .anyMatch(week -> week.isActive() && weekStartDate.equals(week.getWeekStartDate()));
    if (!validWeek) {
      throw new ApiException(StatusCode.NOT_FOUND, "Active week not found for " + weekStartDate);
    }
    return weekStartDate;
  }

  private List<RubricCriterion> loadRubricCriteria(Long rubricId) {
    Rubric rubric = rubricRepository.findById(rubricId)
        .orElseThrow(() -> new ApiException(StatusCode.NOT_FOUND, "Rubric not found with id " + rubricId));
    return rubric.getCriteria().stream()
        .sorted(Comparator.comparing(RubricCriterion::getPosition))
        .toList();
  }

  private List<PeerEvaluationTeammateResponse> teammateResponses(List<TeamMembership> memberships, Long studentUserId) {
    return memberships.stream()
        .map(TeamMembership::getStudentUserId)
        .filter(id -> !Objects.equals(id, studentUserId))
        .map(this::loadStudentName)
        .sorted(Comparator.comparing(PeerEvaluationTeammateResponse::displayName, String.CASE_INSENSITIVE_ORDER))
        .toList();
  }

  private PeerEvaluationTeammateResponse loadStudentName(Long studentUserId) {
    ProjectUser user = userRepository.findById(studentUserId)
        .orElseThrow(() -> new ApiException(StatusCode.NOT_FOUND, "Student not found with id " + studentUserId));
    return new PeerEvaluationTeammateResponse(user.getId(), user.getDisplayName());
  }

  private PeerEvaluationCriterionFormResponse toCriterionFormResponse(RubricCriterion criterion) {
    return new PeerEvaluationCriterionFormResponse(
        criterion.getId(),
        criterion.getName(),
        criterion.getDescription(),
        criterion.getMaxScore(),
        criterion.getPosition());
  }

  private String normalizeComment(String comment) {
    if (comment == null) {
      return null;
    }
    String trimmed = comment.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private void validatePositive(Long value, String field) {
    if (value == null || value <= 0) {
      throw new ApiException(StatusCode.INVALID_ARGUMENT, field + " must be positive");
    }
  }

  private BigDecimal average(List<BigDecimal> scores) {
    if (scores.isEmpty()) {
      return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
    BigDecimal total = scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    return total.divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
  }

  private record StudentContext(
      ProjectUser student,
      Team team,
      Section section,
      List<TeamMembership> memberships) {
  }
}
