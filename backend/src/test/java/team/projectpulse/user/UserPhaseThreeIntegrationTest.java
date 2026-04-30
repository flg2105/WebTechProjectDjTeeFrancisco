package team.projectpulse.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static team.projectpulse.system.StatusCode.NOT_FOUND;
import static team.projectpulse.system.StatusCode.SUCCESS;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.hamcrest.Matchers.nullValue;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import team.projectpulse.peereval.domain.PeerEvaluationEntry;
import team.projectpulse.peereval.domain.PeerEvaluationScore;
import team.projectpulse.peereval.domain.PeerEvaluationSubmission;
import team.projectpulse.peereval.repository.PeerEvaluationSubmissionRepository;
import team.projectpulse.rubric.domain.Rubric;
import team.projectpulse.rubric.repository.RubricRepository;
import team.projectpulse.section.domain.ActiveWeek;
import team.projectpulse.section.domain.Section;
import team.projectpulse.section.repository.ActiveWeekRepository;
import team.projectpulse.section.repository.SectionRepository;
import team.projectpulse.team.domain.Team;
import team.projectpulse.team.domain.TeamInstructorAssignment;
import team.projectpulse.team.domain.TeamMembership;
import team.projectpulse.team.repository.TeamInstructorAssignmentRepository;
import team.projectpulse.team.repository.TeamMembershipRepository;
import team.projectpulse.team.repository.TeamRepository;
import team.projectpulse.user.domain.Invitation;
import team.projectpulse.user.domain.UserRole;
import team.projectpulse.user.repository.InvitationRepository;
import team.projectpulse.war.domain.WarActivity;
import team.projectpulse.war.domain.WarActivityCategory;
import team.projectpulse.war.domain.WarActivityStatus;
import team.projectpulse.war.domain.WarEntry;
import team.projectpulse.war.repository.WarEntryRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserPhaseThreeIntegrationTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private TeamMembershipRepository teamMembershipRepository;

  @Autowired
  private InvitationRepository invitationRepository;

  @Autowired
  private RubricRepository rubricRepository;

  @Autowired
  private SectionRepository sectionRepository;

  @Autowired
  private ActiveWeekRepository activeWeekRepository;

  @Autowired
  private TeamRepository teamRepository;

  @Autowired
  private TeamInstructorAssignmentRepository teamInstructorAssignmentRepository;

  @Autowired
  private WarEntryRepository warEntryRepository;

  @Autowired
  private PeerEvaluationSubmissionRepository peerEvaluationSubmissionRepository;

  @Test
  void should_EditAccount_ById() throws Exception {
    Long studentId = setupStudent("phase3.editme@example.edu", "Edit Me");

    mvc.perform(put("/api/users/" + studentId)
            .with(student("phase3.editme@example.edu"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "email": "phase3.edited@example.edu",
                  "displayName": "Edited Name"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.id").value(studentId))
        .andExpect(jsonPath("$.data.email").value("phase3.edited@example.edu"))
        .andExpect(jsonPath("$.data.displayName").value("Edited Name"));
  }

  @Test
  void should_ReturnNotFound_When_EditAccount_UserMissing() throws Exception {
    mvc.perform(put("/api/users/999999")
            .with(admin())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "email": "missing@example.edu",
                  "displayName": "Missing"
                }
                """))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(NOT_FOUND));
  }

  @Test
  void should_FindStudents_AndSupportBasicQueryStub() throws Exception {
    setupStudent("phase3.findstudents.alice@example.edu", "FindStudents Alice");
    setupStudent("phase3.findstudents.bob@example.edu", "FindStudents Bob");
    setupInstructor("phase3.findstudents.instructor@example.edu", "FindStudents Instructor");

    mvc.perform(get("/api/students").with(instructor()).param("q", "findstudents"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.length()").value(2));

    mvc.perform(get("/api/students").with(instructor()).param("q", "phase3.findstudents.bob"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].email").value("phase3.findstudents.bob@example.edu"));
  }

  @Test
  void should_CreateStudent_When_AdminOrInstructorSetsUpAccount() throws Exception {
    mvc.perform(post("/api/students")
            .with(instructor())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "email": "phase3.staff.created.student@example.edu",
                  "displayName": "Staff Created Student",
                  "password": "projectpulse123"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.email").value("phase3.staff.created.student@example.edu"))
        .andExpect(jsonPath("$.data.role").value("STUDENT"))
        .andExpect(jsonPath("$.data.status").value("ACTIVE"));
  }

  @Test
  void should_CreateInstructor_When_AdminUsesInstructorManagementEndpoint() throws Exception {
    mvc.perform(post("/api/instructors")
            .with(admin())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "email": "phase3.admin.created.instructor@example.edu",
                  "displayName": "Admin Created Instructor",
                  "password": "projectpulse123"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.email").value("phase3.admin.created.instructor@example.edu"))
        .andExpect(jsonPath("$.data.role").value("INSTRUCTOR"))
        .andExpect(jsonPath("$.data.status").value("ACTIVE"));
  }

  @Test
  void should_ViewStudent_WithSectionTeamWarsAndPeerEvaluations() throws Exception {
    Long studentId = setupStudent("phase3.viewme@example.edu", "View Me");
    Long evaluatorId = setupStudent("phase3.evaluator@example.edu", "Peer Reviewer");
    StudentTeamContext context = createStudentTeamContext(
        studentId,
        evaluatorId,
        "Capstone Students 2026",
        "Product Crew",
        "2026-2027");
    createWarForStudent(studentId, context.activeWeekId(), context.teamId());
    createPeerEvaluationForStudent(studentId, evaluatorId, context.sectionId(), context.teamId(), context.weekStartDate());

    mvc.perform(get("/api/students/" + studentId).with(instructor()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.id").value(studentId))
        .andExpect(jsonPath("$.data.firstName").value("View"))
        .andExpect(jsonPath("$.data.lastName").value("Me"))
        .andExpect(jsonPath("$.data.sectionName").value("Capstone Students 2026"))
        .andExpect(jsonPath("$.data.teamName").value("Product Crew"))
        .andExpect(jsonPath("$.data.wars.length()").value(1))
        .andExpect(jsonPath("$.data.wars[0].teamName").value("Product Crew"))
        .andExpect(jsonPath("$.data.wars[0].activityCount").value(1))
        .andExpect(jsonPath("$.data.peerEvaluations.length()").value(1))
        .andExpect(jsonPath("$.data.peerEvaluations[0].evaluatorDisplayName").value("Peer Reviewer"))
        .andExpect(jsonPath("$.data.peerEvaluations[0].teamName").value("Product Crew"))
        .andExpect(jsonPath("$.data.peerEvaluations[0].averageScore").value(9.0));
  }

  @Test
  void should_FindInstructors_WithCriteriaAndAssociatedTeams() throws Exception {
    setupStudent("phase3.instructorsearch.student@example.edu", "Instructor Search Student");
    Long adaInstructorId = setupInstructor("phase3.instructorsearch.ada@example.edu", "Ada Searchcase");
    Long alanInstructorId = setupInstructor("phase3.instructorsearch.alan@example.edu", "Alan Turing");

    mvc.perform(post("/api/invitations/instructors")
            .with(admin())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "emails": ["phase3.instructorsearch.invited@example.edu"]
                }
                """))
        .andExpect(status().isOk());

    createSupervisedTeam(adaInstructorId, "Capstone Search 2027", "Compiler Crew", "2027-2028");
    createSupervisedTeam(alanInstructorId, "Capstone Search 2026", "Systems Squad", "2026-2027");

    MvcResult adaSearch = mvc.perform(get("/api/instructors").with(admin()).param("firstName", "Ada").param("lastName", "Searchcase"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andReturn();
    JsonNode adaInstructor = findInstructorByEmail(adaSearch, "phase3.instructorsearch.ada@example.edu");
    assertNotNull(adaInstructor);
    assertEquals("Ada", adaInstructor.path("firstName").asText());
    assertEquals("Searchcase", adaInstructor.path("lastName").asText());
    assertEquals("ACTIVE", adaInstructor.path("status").asText());
    assertEquals(1, adaInstructor.path("supervisedTeams").size());
    assertEquals("Compiler Crew", adaInstructor.path("supervisedTeams").get(0).path("teamName").asText());

    MvcResult alanSearch = mvc.perform(get("/api/instructors").with(admin()).param("teamName", "Systems"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andReturn();
    JsonNode alanInstructor = findInstructorByEmail(alanSearch, "phase3.instructorsearch.alan@example.edu");
    assertNotNull(alanInstructor);
    assertEquals("Systems Squad", alanInstructor.path("supervisedTeams").get(0).path("teamName").asText());

    MvcResult invitedSearch = mvc.perform(get("/api/instructors").with(admin()).param("status", "INVITED"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andReturn();
    JsonNode invitedInstructor = findInstructorByEmail(invitedSearch, "phase3.instructorsearch.invited@example.edu");
    assertNotNull(invitedInstructor);
    assertEquals(0, invitedInstructor.path("supervisedTeams").size());
  }

  @Test
  void should_ViewInstructor_WithSupervisedTeams() throws Exception {
    Long instructorId = setupInstructor("phase3.viewinstructor@example.edu", "Ada Lovelace");
    Long teamId = createSupervisedTeam(instructorId, "Capstone 2026", "Compiler Crew");

    mvc.perform(get("/api/instructors/" + instructorId).with(admin()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.id").value(instructorId))
        .andExpect(jsonPath("$.data.firstName").value("Ada"))
        .andExpect(jsonPath("$.data.lastName").value("Lovelace"))
        .andExpect(jsonPath("$.data.supervisedTeams.length()").value(1))
        .andExpect(jsonPath("$.data.supervisedTeams[0].teamId").value(teamId))
        .andExpect(jsonPath("$.data.supervisedTeams[0].sectionName").value("Capstone 2026"))
        .andExpect(jsonPath("$.data.supervisedTeams[0].teamName").value("Compiler Crew"));
  }

  @Test
  void should_DeactivateInstructor_AndKeepRecord() throws Exception {
    Long instructorId = setupInstructor("phase3.deactivate.instructor@example.edu", "Grace Hopper");

    mvc.perform(post("/api/instructors/" + instructorId + "/deactivate")
            .with(admin())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "reason": "Instructor left the section."
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.id").value(instructorId))
        .andExpect(jsonPath("$.data.status").value("INACTIVE"));

    mvc.perform(get("/api/instructors/" + instructorId).with(admin()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("INACTIVE"));
  }

  @Test
  void should_ReactivateInstructor() throws Exception {
    Long instructorId = setupInstructor("phase3.reactivate.instructor@example.edu", "Katherine Johnson");

    mvc.perform(post("/api/instructors/" + instructorId + "/deactivate")
            .with(admin())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "reason": "Temporary leave."
                }
                """))
        .andExpect(status().isOk());

    mvc.perform(post("/api/instructors/" + instructorId + "/reactivate").with(admin()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.status").value("ACTIVE"));
  }

  @Test
  void should_RejectInstructorDeactivate_When_NotAdmin() throws Exception {
    Long instructorId = setupInstructor("phase3.forbidden.instructor@example.edu", "Denied User");

    mvc.perform(post("/api/instructors/" + instructorId + "/deactivate")
            .with(instructor())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "reason": "Should fail."
                }
                """))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.flag").value(false));
  }

  @Test
  void should_DeleteStudent_AndCleanupMembershipsAndInvites() throws Exception {
    String email = "phase3.deleteme@example.edu";
    Long studentId = setupStudent(email, "Delete Me");
    Long evaluatorId = setupStudent("phase3.delete.evaluator@example.edu", "Delete Evaluator");
    StudentTeamContext context = createStudentTeamContext(
        studentId,
        evaluatorId,
        "Capstone Delete 2026",
        "Delete Crew",
        "2026-2027");
    createWarForStudent(studentId, context.activeWeekId(), context.teamId());
    createPeerEvaluationForStudent(studentId, evaluatorId, context.sectionId(), context.teamId(), context.weekStartDate());
    createPeerEvaluationForStudent(evaluatorId, studentId, context.sectionId(), context.teamId(), context.weekStartDate());

    TeamMembership membership = new TeamMembership();
    membership.setTeamId(123L);
    membership.setStudentUserId(studentId);
    teamMembershipRepository.save(membership);

    Invitation invitation = new Invitation();
    invitation.setEmail(email);
    invitation.setRole(UserRole.STUDENT);
    invitation.setSectionId(null);
    invitationRepository.save(invitation);

    mvc.perform(delete("/api/students/" + studentId).with(admin()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data").value(nullValue()));

    mvc.perform(get("/api/students/" + studentId).with(instructor()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(NOT_FOUND));

    assertFalse(teamMembershipRepository.existsByStudentUserId(studentId));
    assertFalse(invitationRepository.existsByEmailIgnoreCaseAndRole(email, UserRole.STUDENT));
    assertEquals(0, warEntryRepository.findByStudentUserIdOrderByActiveWeekIdDesc(studentId).size());
    assertEquals(0, peerEvaluationSubmissionRepository
        .findDistinctByEntriesEvaluateeStudentUserIdOrderByWeekStartDateDesc(studentId)
        .size());
    assertEquals(0, peerEvaluationSubmissionRepository
        .findByEvaluatorStudentUserIdOrderByWeekStartDateDesc(studentId)
        .size());
  }

  private Long setupStudent(String email, String displayName) throws Exception {
    MvcResult result = mvc.perform(post("/api/users/student-setup")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "email": "%s",
                  "displayName": "%s",
                  "password": "projectpulse123"
                }
                """.formatted(email, displayName)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.role").value("STUDENT"))
        .andReturn();

    return readId(result);
  }

  private Long setupInstructor(String email, String displayName) throws Exception {
    MvcResult result = mvc.perform(post("/api/users/instructor-setup")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "email": "%s",
                  "displayName": "%s",
                  "password": "projectpulse123"
                }
                """.formatted(email, displayName)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.role").value("INSTRUCTOR"))
        .andReturn();

    return readId(result);
  }

  private Long readId(MvcResult result) throws Exception {
    String body = result.getResponse().getContentAsString();
    String marker = "\"id\":";
    int start = body.indexOf(marker) + marker.length();
    int end = body.indexOf(",", start);
    return Long.valueOf(body.substring(start, end));
  }

  private JsonNode findInstructorByEmail(MvcResult result, String email) throws Exception {
    JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    for (JsonNode candidate : data) {
      if (email.equalsIgnoreCase(candidate.path("email").asText())) {
        return candidate;
      }
    }
    return null;
  }

  private Long createSupervisedTeam(Long instructorUserId, String sectionName, String teamName) {
    return createSupervisedTeam(instructorUserId, sectionName, teamName, "2026-2027");
  }

  private Long createSupervisedTeam(Long instructorUserId, String sectionName, String teamName, String academicYear) {
    Rubric rubric = new Rubric();
    rubric.setName("Instructor View Rubric " + instructorUserId);
    rubric = rubricRepository.save(rubric);

    Section section = new Section();
    section.setName(sectionName);
    section.setAcademicYear(academicYear);
    section.setStartDate(LocalDate.of(2026, 1, 12));
    section.setEndDate(LocalDate.of(2026, 5, 1));
    section.setRubricId(rubric.getId());
    section = sectionRepository.save(section);

    Team team = new Team();
    team.setSectionId(section.getId());
    team.setName(teamName);
    team = teamRepository.save(team);

    TeamInstructorAssignment assignment = new TeamInstructorAssignment();
    assignment.setTeamId(team.getId());
    assignment.setInstructorUserId(instructorUserId);
    teamInstructorAssignmentRepository.save(assignment);

    return team.getId();
  }

  private StudentTeamContext createStudentTeamContext(
      Long studentId,
      Long evaluatorStudentId,
      String sectionName,
      String teamName,
      String academicYear) {
    Rubric rubric = new Rubric();
    rubric.setName("Student View Rubric " + studentId);
    rubric = rubricRepository.save(rubric);

    Section section = new Section();
    section.setName(sectionName);
    section.setAcademicYear(academicYear);
    section.setStartDate(LocalDate.of(2026, 1, 12));
    section.setEndDate(LocalDate.of(2026, 5, 1));
    section.setRubricId(rubric.getId());
    section = sectionRepository.save(section);

    Team team = new Team();
    team.setSectionId(section.getId());
    team.setName(teamName);
    team = teamRepository.save(team);

    TeamMembership studentMembership = new TeamMembership();
    studentMembership.setTeamId(team.getId());
    studentMembership.setStudentUserId(studentId);
    teamMembershipRepository.save(studentMembership);

    TeamMembership evaluatorMembership = new TeamMembership();
    evaluatorMembership.setTeamId(team.getId());
    evaluatorMembership.setStudentUserId(evaluatorStudentId);
    teamMembershipRepository.save(evaluatorMembership);

    ActiveWeek activeWeek = new ActiveWeek();
    activeWeek.setSectionId(section.getId());
    activeWeek.setWeekStartDate(LocalDate.of(2026, 2, 2));
    activeWeek.setActive(true);
    activeWeek = activeWeekRepository.save(activeWeek);

    return new StudentTeamContext(section.getId(), team.getId(), activeWeek.getId(), activeWeek.getWeekStartDate());
  }

  private void createWarForStudent(Long studentId, Long activeWeekId, Long teamId) {
    WarEntry entry = new WarEntry();
    entry.setActiveWeekId(activeWeekId);
    entry.setStudentUserId(studentId);
    entry.setTeamId(teamId);

    WarActivity activity = new WarActivity();
    activity.setCategory(WarActivityCategory.DEVELOPMENT);
    activity.setActivity("Implemented endpoint");
    activity.setDescription("Built the student detail endpoint");
    activity.setHoursPlanned(new BigDecimal("4.00"));
    activity.setHoursActual(new BigDecimal("4.50"));
    activity.setStatus(WarActivityStatus.DONE);
    entry.addActivity(activity);

    warEntryRepository.save(entry);
  }

  private void createPeerEvaluationForStudent(
      Long studentId,
      Long evaluatorStudentId,
      Long sectionId,
      Long teamId,
      LocalDate weekStartDate) {
    PeerEvaluationSubmission submission = new PeerEvaluationSubmission();
    submission.setSectionId(sectionId);
    submission.setTeamId(teamId);
    submission.setEvaluatorStudentUserId(evaluatorStudentId);
    submission.setWeekStartDate(weekStartDate);

    PeerEvaluationEntry entry = new PeerEvaluationEntry();
    entry.setEvaluateeStudentUserId(studentId);
    entry.setPublicComment("Reliable teammate");
    entry.setPrivateComment("Strong follow-through");

    PeerEvaluationScore score = new PeerEvaluationScore();
    score.setRubricCriterionId(1L);
    score.setScore(new BigDecimal("9.00"));
    entry.addScore(score);

    submission.addEntry(entry);
    peerEvaluationSubmissionRepository.save(submission);
  }

  private SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor admin() {
    return SecurityMockMvcRequestPostProcessors.user("admin@test.local").roles("ADMIN");
  }

  private SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor instructor() {
    return SecurityMockMvcRequestPostProcessors.user("instructor@test.local").roles("INSTRUCTOR");
  }

  private SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor student(String email) {
    return SecurityMockMvcRequestPostProcessors.user(email).roles("STUDENT");
  }

  private record StudentTeamContext(
      Long sectionId,
      Long teamId,
      Long activeWeekId,
      LocalDate weekStartDate) {}
}
