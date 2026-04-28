package team.projectpulse.system.config;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import team.projectpulse.peereval.domain.PeerEvaluationEntry;
import team.projectpulse.peereval.domain.PeerEvaluationScore;
import team.projectpulse.peereval.domain.PeerEvaluationSubmission;
import team.projectpulse.peereval.repository.PeerEvaluationSubmissionRepository;
import team.projectpulse.rubric.domain.Rubric;
import team.projectpulse.rubric.domain.RubricCriterion;
import team.projectpulse.rubric.repository.RubricRepository;
import team.projectpulse.section.domain.ActiveWeek;
import team.projectpulse.section.domain.Section;
import team.projectpulse.section.repository.ActiveWeekRepository;
import team.projectpulse.section.repository.SectionRepository;
import team.projectpulse.team.domain.Team;
import team.projectpulse.team.domain.TeamMembership;
import team.projectpulse.team.repository.TeamMembershipRepository;
import team.projectpulse.team.repository.TeamRepository;
import team.projectpulse.user.domain.Invitation;
import team.projectpulse.user.domain.ProjectUser;
import team.projectpulse.user.domain.UserRole;
import team.projectpulse.user.domain.UserStatus;
import team.projectpulse.user.repository.InvitationRepository;
import team.projectpulse.user.repository.UserRepository;
import team.projectpulse.war.domain.WarActivity;
import team.projectpulse.war.domain.WarActivityCategory;
import team.projectpulse.war.domain.WarActivityStatus;
import team.projectpulse.war.domain.WarEntry;
import team.projectpulse.war.repository.WarEntryRepository;

@Profile("dev")
@Component
public class DevDataInitializer implements ApplicationRunner {
  private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);

  private final boolean enabled;
  private final UserRepository userRepository;
  private final InvitationRepository invitationRepository;
  private final SectionRepository sectionRepository;
  private final ActiveWeekRepository activeWeekRepository;
  private final TeamRepository teamRepository;
  private final TeamMembershipRepository teamMembershipRepository;
  private final RubricRepository rubricRepository;
  private final WarEntryRepository warEntryRepository;
  private final PeerEvaluationSubmissionRepository peerEvaluationSubmissionRepository;

  public DevDataInitializer(
      @Value("${projectpulse.dev-seed.enabled:false}") boolean enabled,
      UserRepository userRepository,
      InvitationRepository invitationRepository,
      SectionRepository sectionRepository,
      ActiveWeekRepository activeWeekRepository,
      TeamRepository teamRepository,
      TeamMembershipRepository teamMembershipRepository,
      RubricRepository rubricRepository,
      WarEntryRepository warEntryRepository,
      PeerEvaluationSubmissionRepository peerEvaluationSubmissionRepository) {
    this.enabled = enabled;
    this.userRepository = userRepository;
    this.invitationRepository = invitationRepository;
    this.sectionRepository = sectionRepository;
    this.activeWeekRepository = activeWeekRepository;
    this.teamRepository = teamRepository;
    this.teamMembershipRepository = teamMembershipRepository;
    this.rubricRepository = rubricRepository;
    this.warEntryRepository = warEntryRepository;
    this.peerEvaluationSubmissionRepository = peerEvaluationSubmissionRepository;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (!enabled) {
      log.info("Dev seed is disabled (set projectpulse.dev-seed.enabled=true to enable).");
      return;
    }

    reset();
    SeedSnapshot snapshot = seed();
    log.info("Dev seed complete: sectionId={}, teamId={}, users={}, activeWeeks={}",
        snapshot.sectionId(),
        snapshot.teamId(),
        snapshot.userEmails(),
        snapshot.activeWeekStartDates());
  }

  private void reset() {
    // Use entity deletes (not bulk deletes) for aggregate roots that rely on JPA cascade/orphanRemoval.
    // Bulk deletes (deleteAllInBatch) bypass cascade and can violate FK constraints.
    peerEvaluationSubmissionRepository.deleteAll();
    peerEvaluationSubmissionRepository.flush();

    warEntryRepository.deleteAll();
    warEntryRepository.flush();

    teamMembershipRepository.deleteAllInBatch();
    teamRepository.deleteAllInBatch();
    activeWeekRepository.deleteAllInBatch();
    sectionRepository.deleteAllInBatch();
    invitationRepository.deleteAllInBatch();
    userRepository.deleteAllInBatch();
    rubricRepository.deleteAll();
    rubricRepository.flush();
  }

  private SeedSnapshot seed() {
    Rubric rubric =
        rubricRepository
            .findByNameIgnoreCase("Dev Seed Rubric")
            .orElseGet(
                () -> {
                  Rubric newRubric = new Rubric();
                  newRubric.setName("Dev Seed Rubric");

                  RubricCriterion quality = new RubricCriterion();
                  quality.setName("Quality of work");
                  quality.setDescription("How do you rate the quality of this teammate's work?");
                  quality.setMaxScore(new BigDecimal("10.00"));
                  quality.setPosition(0);
                  newRubric.addCriterion(quality);

                  RubricCriterion productivity = new RubricCriterion();
                  productivity.setName("Productivity");
                  productivity.setDescription("How productive is this teammate?");
                  productivity.setMaxScore(new BigDecimal("10.00"));
                  productivity.setPosition(1);
                  newRubric.addCriterion(productivity);

                  return rubricRepository.save(newRubric);
                });

    LocalDate currentMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
    LocalDate previousMonday = currentMonday.minusWeeks(1);

    Section section = new Section();
    section.setName("Dev Seed Section");
    section.setAcademicYear(String.valueOf(currentMonday.getYear()));
    section.setStartDate(currentMonday.minusWeeks(4));
    section.setEndDate(currentMonday.plusWeeks(10));
    section.setRubricId(rubric.getId());
    section = sectionRepository.save(section);

    ActiveWeek previousWeek = activeWeekRepository.save(activeWeek(section.getId(), previousMonday, true));
    ActiveWeek currentWeek = activeWeekRepository.save(activeWeek(section.getId(), currentMonday, true));

    Team team = new Team();
    team.setSectionId(section.getId());
    team.setName("Dev Seed Team");
    team = teamRepository.save(team);

    ProjectUser admin = userRepository.save(user("admin@dev.local", "Dev Admin", UserRole.ADMIN, UserStatus.ACTIVE));
    ProjectUser instructor = userRepository.save(user("instructor@dev.local", "Dev Instructor", UserRole.INSTRUCTOR, UserStatus.ACTIVE));
    ProjectUser studentOne = userRepository.save(user("student1@dev.local", "Dev Student One", UserRole.STUDENT, UserStatus.ACTIVE));
    ProjectUser studentTwo = userRepository.save(user("student2@dev.local", "Dev Student Two", UserRole.STUDENT, UserStatus.ACTIVE));
    ProjectUser studentThree = userRepository.save(user("student3@dev.local", "Dev Student Three", UserRole.STUDENT, UserStatus.ACTIVE));

    teamMembershipRepository.saveAll(List.of(
        membership(team.getId(), studentOne.getId()),
        membership(team.getId(), studentTwo.getId()),
        membership(team.getId(), studentThree.getId())));

    invitationRepository.save(invitation("invited.student@dev.local", UserRole.STUDENT, section.getId()));

    warEntryRepository.save(warEntry(previousWeek.getId(), team.getId(), studentOne.getId(), List.of(
        warActivity(WarActivityCategory.DEVELOPMENT, "Implement WAR UI", "Built the weekly WAR entry form.", "4.00", "4.50", WarActivityStatus.DONE),
        warActivity(WarActivityCategory.TESTING, "WAR endpoint tests", "Added integration tests for WAR endpoints.", "2.00", "2.00", WarActivityStatus.DONE))));

    warEntryRepository.save(warEntry(currentWeek.getId(), team.getId(), studentOne.getId(), List.of(
        warActivity(WarActivityCategory.PLANNING, "Plan instructor report demo", "Planned the report demo path.", "1.50", "1.00", WarActivityStatus.IN_PROGRESS))));

    peerEvaluationSubmissionRepository.save(peerEvaluationSubmission(
        section.getId(),
        team.getId(),
        previousMonday,
        studentTwo.getId(),
        List.of(
            peerEvaluationEntry(studentOne.getId(), "Strong delivery this week.", null, rubric.getCriteria()),
            peerEvaluationEntry(studentThree.getId(), "Good collaboration.", null, rubric.getCriteria()))));

    peerEvaluationSubmissionRepository.save(peerEvaluationSubmission(
        section.getId(),
        team.getId(),
        previousMonday,
        studentThree.getId(),
        List.of(
            peerEvaluationEntry(studentOne.getId(), "Consistent progress.", null, rubric.getCriteria()),
            peerEvaluationEntry(studentTwo.getId(), "Reliable teammate.", null, rubric.getCriteria()))));

    return new SeedSnapshot(
        section.getId(),
        team.getId(),
        List.of(admin.getEmail(), instructor.getEmail(), studentOne.getEmail(), studentTwo.getEmail(), studentThree.getEmail()),
        List.of(previousWeek.getWeekStartDate(), currentWeek.getWeekStartDate()));
  }

  private static ActiveWeek activeWeek(Long sectionId, LocalDate weekStartDate, boolean active) {
    ActiveWeek week = new ActiveWeek();
    week.setSectionId(sectionId);
    week.setWeekStartDate(weekStartDate);
    week.setActive(active);
    return week;
  }

  private static ProjectUser user(String email, String displayName, UserRole role, UserStatus status) {
    ProjectUser user = new ProjectUser();
    user.setEmail(email);
    user.setDisplayName(displayName);
    user.setRole(role);
    user.setStatus(status);
    return user;
  }

  private static TeamMembership membership(Long teamId, Long studentUserId) {
    TeamMembership membership = new TeamMembership();
    membership.setTeamId(teamId);
    membership.setStudentUserId(studentUserId);
    return membership;
  }

  private static Invitation invitation(String email, UserRole role, Long sectionId) {
    Invitation invitation = new Invitation();
    invitation.setEmail(email);
    invitation.setRole(role);
    invitation.setSectionId(sectionId);
    return invitation;
  }

  private static WarEntry warEntry(Long activeWeekId, Long teamId, Long studentUserId, List<WarActivity> activities) {
    WarEntry entry = new WarEntry();
    entry.setActiveWeekId(activeWeekId);
    entry.setTeamId(teamId);
    entry.setStudentUserId(studentUserId);
    entry.setSubmittedAt(Instant.now());
    for (WarActivity activity : activities) {
      entry.addActivity(activity);
    }
    return entry;
  }

  private static WarActivity warActivity(
      WarActivityCategory category,
      String activity,
      String description,
      String hoursPlanned,
      String hoursActual,
      WarActivityStatus status) {
    WarActivity warActivity = new WarActivity();
    warActivity.setCategory(category);
    warActivity.setActivity(activity);
    warActivity.setDescription(description);
    warActivity.setHoursPlanned(new BigDecimal(hoursPlanned));
    warActivity.setHoursActual(new BigDecimal(hoursActual));
    warActivity.setStatus(status);
    return warActivity;
  }

  private static PeerEvaluationSubmission peerEvaluationSubmission(
      Long sectionId,
      Long teamId,
      LocalDate weekStartDate,
      Long evaluatorStudentUserId,
      List<PeerEvaluationEntry> entries) {
    PeerEvaluationSubmission submission = new PeerEvaluationSubmission();
    submission.setSectionId(sectionId);
    submission.setTeamId(teamId);
    submission.setWeekStartDate(weekStartDate);
    submission.setEvaluatorStudentUserId(evaluatorStudentUserId);
    for (PeerEvaluationEntry entry : entries) {
      submission.addEntry(entry);
    }
    return submission;
  }

  private static PeerEvaluationEntry peerEvaluationEntry(
      Long evaluateeStudentUserId,
      String publicComment,
      String privateComment,
      List<RubricCriterion> criteria) {
    PeerEvaluationEntry entry = new PeerEvaluationEntry();
    entry.setEvaluateeStudentUserId(evaluateeStudentUserId);
    entry.setPublicComment(publicComment);
    entry.setPrivateComment(privateComment);
    for (RubricCriterion criterion : criteria) {
      PeerEvaluationScore score = new PeerEvaluationScore();
      score.setRubricCriterionId(criterion.getId());
      score.setScore(criterion.getMaxScore().subtract(new BigDecimal("1.00")));
      entry.addScore(score);
    }
    return entry;
  }

  private record SeedSnapshot(
      Long sectionId,
      Long teamId,
      List<String> userEmails,
      List<LocalDate> activeWeekStartDates) {}
}
