package team.projectpulse.peereval;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static team.projectpulse.system.StatusCode.CONFLICT;
import static team.projectpulse.system.StatusCode.NOT_FOUND;
import static team.projectpulse.system.StatusCode.SUCCESS;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PeerEvaluationControllerIntegrationTest {

  @Autowired
  private MockMvc mvc;

  @Test
  void should_LoadCurrentPeerEvaluationForm_ForStudent() throws Exception {
    PeerEvalFixture fixture = createPeerEvalFixture();

    mvc.perform(get("/api/peer-evaluations")
            .with(student(fixture.studentOneEmail()))
            .param("studentUserId", fixture.studentOneId().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.evaluatorStudentUserId").value(fixture.studentOneId()))
        .andExpect(jsonPath("$.data.weekStartDate").value(fixture.previousWeekStart().toString()))
        .andExpect(jsonPath("$.data.alreadySubmitted").value(false))
        .andExpect(jsonPath("$.data.criteria", hasSize(2)))
        .andExpect(jsonPath("$.data.teammates", hasSize(2)));
  }

  @Test
  void should_SubmitPeerEvaluations_AndGenerateOwnReport() throws Exception {
    PeerEvalFixture fixture = createPeerEvalFixture();

    submitEvaluation(
        fixture.studentTwoEmail(),
        fixture.studentTwoId(),
        fixture.previousWeekStart(),
        """
            [
              {
                "evaluateeStudentUserId": %d,
                "publicComment": "Strong delivery this week.",
                "privateComment": "Needs more detail in standups.",
                "scores": [
                  {"rubricCriterionId": %d, "score": 8},
                  {"rubricCriterionId": %d, "score": 9}
                ]
              },
              {
                "evaluateeStudentUserId": %d,
                "publicComment": "Great collaboration.",
                "privateComment": null,
                "scores": [
                  {"rubricCriterionId": %d, "score": 7},
                  {"rubricCriterionId": %d, "score": 8}
                ]
              }
            ]
            """.formatted(
            fixture.studentOneId(),
            fixture.criterionOneId(),
            fixture.criterionTwoId(),
            fixture.studentThreeId(),
            fixture.criterionOneId(),
            fixture.criterionTwoId()));

    submitEvaluation(
        fixture.studentThreeEmail(),
        fixture.studentThreeId(),
        fixture.previousWeekStart(),
        """
            [
              {
                "evaluateeStudentUserId": %d,
                "publicComment": "Consistent progress.",
                "privateComment": "Could speak up sooner.",
                "scores": [
                  {"rubricCriterionId": %d, "score": 10},
                  {"rubricCriterionId": %d, "score": 8}
                ]
              },
              {
                "evaluateeStudentUserId": %d,
                "publicComment": "Helpful teammate.",
                "privateComment": "",
                "scores": [
                  {"rubricCriterionId": %d, "score": 9},
                  {"rubricCriterionId": %d, "score": 9}
                ]
              }
            ]
            """.formatted(
            fixture.studentOneId(),
            fixture.criterionOneId(),
            fixture.criterionTwoId(),
            fixture.studentTwoId(),
            fixture.criterionOneId(),
            fixture.criterionTwoId()));

    mvc.perform(get("/api/peer-evaluations/me/report")
            .with(student(fixture.studentOneEmail()))
            .param("studentUserId", fixture.studentOneId().toString())
            .param("weekStartDate", fixture.previousWeekStart().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.studentUserId").value(fixture.studentOneId()))
        .andExpect(jsonPath("$.data.receivedEvaluations").value(2))
        .andExpect(jsonPath("$.data.averageTotalScore").value(17.50))
        .andExpect(jsonPath("$.data.criterionAverages[0].averageScore").value(9.00))
        .andExpect(jsonPath("$.data.criterionAverages[1].averageScore").value(8.50))
        .andExpect(jsonPath("$.data.publicComments", hasSize(2)))
        .andExpect(jsonPath("$.data.publicComments[0]").value("Strong delivery this week."))
        .andExpect(jsonPath("$.data.publicComments[1]").value("Consistent progress."))
        .andExpect(jsonPath("$.data.privateComments").doesNotExist())
        .andExpect(jsonPath("$.data.evaluators").doesNotExist());
  }

  @Test
  void should_RejectDuplicatePeerEvaluationSubmission_ForSameWeek() throws Exception {
    PeerEvalFixture fixture = createPeerEvalFixture();

    String evaluations = """
        [
          {
            "evaluateeStudentUserId": %d,
            "publicComment": "Clear communication.",
            "privateComment": null,
            "scores": [
              {"rubricCriterionId": %d, "score": 9},
              {"rubricCriterionId": %d, "score": 8}
            ]
          },
          {
            "evaluateeStudentUserId": %d,
            "publicComment": "Reliable teammate.",
            "privateComment": null,
            "scores": [
              {"rubricCriterionId": %d, "score": 8},
              {"rubricCriterionId": %d, "score": 8}
            ]
          }
        ]
        """.formatted(
        fixture.studentTwoId(),
        fixture.criterionOneId(),
        fixture.criterionTwoId(),
        fixture.studentThreeId(),
        fixture.criterionOneId(),
        fixture.criterionTwoId());

    submitEvaluation(fixture.studentOneEmail(), fixture.studentOneId(), fixture.previousWeekStart(), evaluations);

    mvc.perform(post("/api/peer-evaluations")
            .with(student(fixture.studentOneEmail()))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "evaluatorStudentUserId": %d,
                  "weekStartDate": "%s",
                  "evaluations": %s
                }
                """.formatted(
                fixture.studentOneId(),
                fixture.previousWeekStart(),
                evaluations)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(CONFLICT));
  }

  @Test
  void should_GenerateSectionPeerEvaluationReport_AndShowMissingSubmitters() throws Exception {
    PeerEvalFixture fixture = createPeerEvalFixture();

    submitEvaluation(
        fixture.studentTwoEmail(),
        fixture.studentTwoId(),
        fixture.previousWeekStart(),
        """
            [
              {
                "evaluateeStudentUserId": %d,
                "publicComment": "Strong delivery this week.",
                "privateComment": "Needs more detail in standups.",
                "scores": [
                  {"rubricCriterionId": %d, "score": 8},
                  {"rubricCriterionId": %d, "score": 9}
                ]
              },
              {
                "evaluateeStudentUserId": %d,
                "publicComment": "Great collaboration.",
                "privateComment": null,
                "scores": [
                  {"rubricCriterionId": %d, "score": 7},
                  {"rubricCriterionId": %d, "score": 8}
                ]
              }
            ]
            """.formatted(
            fixture.studentOneId(),
            fixture.criterionOneId(),
            fixture.criterionTwoId(),
            fixture.studentThreeId(),
            fixture.criterionOneId(),
            fixture.criterionTwoId()));

    submitEvaluation(
        fixture.studentThreeEmail(),
        fixture.studentThreeId(),
        fixture.previousWeekStart(),
        """
            [
              {
                "evaluateeStudentUserId": %d,
                "publicComment": "Consistent progress.",
                "privateComment": "Could speak up sooner.",
                "scores": [
                  {"rubricCriterionId": %d, "score": 10},
                  {"rubricCriterionId": %d, "score": 8}
                ]
              },
              {
                "evaluateeStudentUserId": %d,
                "publicComment": "Helpful teammate.",
                "privateComment": "",
                "scores": [
                  {"rubricCriterionId": %d, "score": 9},
                  {"rubricCriterionId": %d, "score": 9}
                ]
              }
            ]
            """.formatted(
            fixture.studentOneId(),
            fixture.criterionOneId(),
            fixture.criterionTwoId(),
            fixture.studentTwoId(),
            fixture.criterionOneId(),
            fixture.criterionTwoId()));

    mvc.perform(get("/api/peer-evaluations/section-report")
            .with(instructor())
            .param("sectionId", fixture.sectionId().toString())
            .param("weekStartDate", fixture.previousWeekStart().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.sectionId").value(fixture.sectionId()))
        .andExpect(jsonPath("$.data.weekStartDate").value(fixture.previousWeekStart().toString()))
        .andExpect(jsonPath("$.data.maxTotalScore").value(20.00))
        .andExpect(jsonPath("$.data.students", hasSize(3)))
        .andExpect(jsonPath("$.data.students[0].studentUserId").value(fixture.studentOneId()))
        .andExpect(jsonPath("$.data.students[0].averageTotalScore").value(17.50))
        .andExpect(jsonPath("$.data.students[0].receivedEvaluations").value(2))
        .andExpect(jsonPath("$.data.students[0].evaluations", hasSize(2)))
        .andExpect(jsonPath("$.data.students[0].evaluations[0].evaluatorStudentUserId").value(fixture.studentThreeId()))
        .andExpect(jsonPath("$.data.students[0].evaluations[0].privateComment").value("Could speak up sooner."))
        .andExpect(jsonPath("$.data.students[1].studentUserId").value(fixture.studentThreeId()))
        .andExpect(jsonPath("$.data.students[1].receivedEvaluations").value(1))
        .andExpect(jsonPath("$.data.students[2].studentUserId").value(fixture.studentTwoId()))
        .andExpect(jsonPath("$.data.students[2].receivedEvaluations").value(1))
        .andExpect(jsonPath("$.data.missingSubmitters", hasSize(1)))
        .andExpect(jsonPath("$.data.missingSubmitters[0].studentUserId").value(fixture.studentOneId()))
        .andExpect(jsonPath("$.data.missingSubmitters[0].studentDisplayName", containsString("One")));
  }

  @Test
  void should_GenerateStudentPeerEvaluationReport_ForPeriod() throws Exception {
    PeerEvalFixture fixture = createPeerEvalFixture();

    submitEvaluation(
        fixture.studentTwoEmail(),
        fixture.studentTwoId(),
        fixture.previousWeekStart(),
        """
            [
              {
                "evaluateeStudentUserId": %d,
                "publicComment": "Strong delivery this week.",
                "privateComment": "Needs more detail in standups.",
                "scores": [
                  {"rubricCriterionId": %d, "score": 8},
                  {"rubricCriterionId": %d, "score": 9}
                ]
              },
              {
                "evaluateeStudentUserId": %d,
                "publicComment": "Great collaboration.",
                "privateComment": null,
                "scores": [
                  {"rubricCriterionId": %d, "score": 7},
                  {"rubricCriterionId": %d, "score": 8}
                ]
              }
            ]
            """.formatted(
            fixture.studentOneId(),
            fixture.criterionOneId(),
            fixture.criterionTwoId(),
            fixture.studentThreeId(),
            fixture.criterionOneId(),
            fixture.criterionTwoId()));

    submitEvaluation(
        fixture.studentThreeEmail(),
        fixture.studentThreeId(),
        fixture.previousWeekStart(),
        """
            [
              {
                "evaluateeStudentUserId": %d,
                "publicComment": "Consistent progress.",
                "privateComment": "Could speak up sooner.",
                "scores": [
                  {"rubricCriterionId": %d, "score": 10},
                  {"rubricCriterionId": %d, "score": 8}
                ]
              },
              {
                "evaluateeStudentUserId": %d,
                "publicComment": "Helpful teammate.",
                "privateComment": "",
                "scores": [
                  {"rubricCriterionId": %d, "score": 9},
                  {"rubricCriterionId": %d, "score": 9}
                ]
              }
            ]
            """.formatted(
            fixture.studentOneId(),
            fixture.criterionOneId(),
            fixture.criterionTwoId(),
            fixture.studentTwoId(),
            fixture.criterionOneId(),
            fixture.criterionTwoId()));

    mvc.perform(get("/api/peer-evaluations/student-report")
            .with(instructor())
            .param("studentUserId", fixture.studentOneId().toString())
            .param("startActiveWeekId", fixture.previousActiveWeekId().toString())
            .param("endActiveWeekId", fixture.currentActiveWeekId().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.studentUserId").value(fixture.studentOneId()))
        .andExpect(jsonPath("$.data.startWeekStartDate").value(fixture.previousWeekStart().toString()))
        .andExpect(jsonPath("$.data.endWeekStartDate").value(fixture.currentWeekStart().toString()))
        .andExpect(jsonPath("$.data.maxTotalScore").value(20.00))
        .andExpect(jsonPath("$.data.weeks.length()").value(2))
        .andExpect(jsonPath("$.data.weeks[0].weekStartDate").value(fixture.previousWeekStart().toString()))
        .andExpect(jsonPath("$.data.weeks[0].averageTotalScore").value(17.50))
        .andExpect(jsonPath("$.data.weeks[0].receivedEvaluations").value(2))
        .andExpect(jsonPath("$.data.weeks[0].evaluations", hasSize(2)))
        .andExpect(jsonPath("$.data.weeks[0].evaluations[0].privateComment").value("Could speak up sooner."))
        .andExpect(jsonPath("$.data.weeks[1].weekStartDate").value(fixture.currentWeekStart().toString()))
        .andExpect(jsonPath("$.data.weeks[1].receivedEvaluations").value(0));
  }

  @Test
  void should_ReturnNotFound_When_StudentPeerEvaluationReportHasNoEvaluations() throws Exception {
    PeerEvalFixture fixture = createPeerEvalFixture();

    mvc.perform(get("/api/peer-evaluations/student-report")
            .with(instructor())
            .param("studentUserId", fixture.studentOneId().toString())
            .param("startActiveWeekId", fixture.previousActiveWeekId().toString())
            .param("endActiveWeekId", fixture.currentActiveWeekId().toString()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(NOT_FOUND))
        .andExpect(jsonPath("$.message").value("No peer evaluation report data available for the selected period"));
  }

  private void submitEvaluation(String email, Long evaluatorStudentUserId, LocalDate weekStartDate, String evaluationsJson)
      throws Exception {
    mvc.perform(post("/api/peer-evaluations")
            .with(student(email))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "evaluatorStudentUserId": %d,
                  "weekStartDate": "%s",
                  "evaluations": %s
                }
                """.formatted(evaluatorStudentUserId, weekStartDate, evaluationsJson)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS));
  }

  private PeerEvalFixture createPeerEvalFixture() throws Exception {
    LocalDate currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    LocalDate previousWeekStart = currentWeekStart.minusWeeks(1);
    long unique = System.nanoTime();

    Long rubricId = createRubric();
    Long criterionOneId = findCriterionId(rubricId, 0);
    Long criterionTwoId = findCriterionId(rubricId, 1);
    Long sectionId = createSection(rubricId);
    createActiveWeeks(sectionId, previousWeekStart, currentWeekStart);
    Long previousActiveWeekId = findActiveWeekId(sectionId, previousWeekStart);
    Long currentActiveWeekId = findActiveWeekId(sectionId, currentWeekStart);

    String studentOneEmail = "phase3.student.one.%d@example.edu".formatted(unique);
    String studentTwoEmail = "phase3.student.two.%d@example.edu".formatted(unique);
    String studentThreeEmail = "phase3.student.three.%d@example.edu".formatted(unique);

    Long studentOneId = setupStudent(studentOneEmail, "Phase Three Student One");
    Long studentTwoId = setupStudent(studentTwoEmail, "Phase Three Student Two");
    Long studentThreeId = setupStudent(studentThreeEmail, "Phase Three Student Three");

    Long teamId = createTeam(sectionId);
    assignStudent(teamId, studentOneId);
    assignStudent(teamId, studentTwoId);
    assignStudent(teamId, studentThreeId);

    return new PeerEvalFixture(
        rubricId,
        criterionOneId,
        criterionTwoId,
        sectionId,
        teamId,
        studentOneId,
        studentOneEmail,
        studentTwoId,
        studentTwoEmail,
        studentThreeId,
        studentThreeEmail,
        previousActiveWeekId,
        currentActiveWeekId,
        previousWeekStart,
        currentWeekStart);
  }

  private Long createRubric() throws Exception {
    MvcResult result = mvc.perform(post("/api/rubrics")
            .with(admin())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "Peer Eval Rubric Phase 3 %s",
                  "criteria": [
                    {
                      "name": "Quality of work",
                      "description": "How do you rate the quality of this teammate's work?",
                      "maxScore": 10
                    },
                    {
                      "name": "Productivity",
                      "description": "How productive is this teammate?",
                      "maxScore": 10
                    }
                  ]
                }
                """.formatted(System.nanoTime())))
        .andExpect(status().isOk())
        .andReturn();
    return readId(result);
  }

  private Long findCriterionId(Long rubricId, int index) throws Exception {
    MvcResult result = mvc.perform(get("/api/rubrics/" + rubricId).with(admin()))
        .andExpect(status().isOk())
        .andReturn();

    String body = result.getResponse().getContentAsString();
    String marker = "\"id\":";
    int start = nthIndexOf(body, marker, index + 2) + marker.length();
    int end = body.indexOf(",", start);
    return Long.valueOf(body.substring(start, end));
  }

  private int nthIndexOf(String text, String marker, int occurrence) {
    int fromIndex = -1;
    for (int i = 0; i < occurrence; i++) {
      fromIndex = text.indexOf(marker, fromIndex + 1);
    }
    return fromIndex;
  }

  private Long createSection(Long rubricId) throws Exception {
    MvcResult result = mvc.perform(post("/api/sections")
            .with(admin())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "Phase 3 Section %s",
                  "academicYear": "2026-2027",
                  "startDate": "2026-01-05",
                  "endDate": "2026-12-31",
                  "rubricId": %d
                }
                """.formatted(System.nanoTime(), rubricId)))
        .andExpect(status().isOk())
        .andReturn();
    return readId(result);
  }

  private void createActiveWeeks(Long sectionId, LocalDate previousWeekStart, LocalDate currentWeekStart) throws Exception {
    mvc.perform(put("/api/sections/" + sectionId + "/active-weeks")
            .with(admin())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                [
                  {"weekStartDate": "%s", "active": true},
                  {"weekStartDate": "%s", "active": true}
                ]
                """.formatted(previousWeekStart, currentWeekStart)))
        .andExpect(status().isOk());
  }

  private Long findActiveWeekId(Long sectionId, LocalDate weekStartDate) throws Exception {
    MvcResult result = mvc.perform(get("/api/sections/" + sectionId).with(admin()))
        .andExpect(status().isOk())
        .andReturn();

    String body = result.getResponse().getContentAsString();
    Pattern pattern = Pattern.compile("\\{\"id\":(\\d+),\"weekStartDate\":\"" + weekStartDate + "\"");
    Matcher matcher = pattern.matcher(body);
    if (!matcher.find()) {
      throw new IllegalStateException("Active week id not found for " + weekStartDate + " in " + body);
    }
    return Long.valueOf(matcher.group(1));
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
        .andReturn();
    return readId(result);
  }

  private Long createTeam(Long sectionId) throws Exception {
    MvcResult result = mvc.perform(post("/api/teams")
            .with(admin())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "sectionId": %d,
                  "name": "Phase 3 Team %s"
                }
                """.formatted(sectionId, System.nanoTime())))
        .andExpect(status().isOk())
        .andReturn();
    return readId(result);
  }

  private void assignStudent(Long teamId, Long studentId) throws Exception {
    mvc.perform(post("/api/teams/" + teamId + "/students")
            .with(admin())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "studentUserId": %d
                }
                """.formatted(studentId)))
        .andExpect(status().isOk());
  }

  private Long readId(MvcResult result) throws Exception {
    String body = result.getResponse().getContentAsString();
    String marker = "\"id\":";
    int start = body.indexOf(marker) + marker.length();
    int end = body.indexOf(",", start);
    return Long.valueOf(body.substring(start, end));
  }

  private record PeerEvalFixture(
      Long rubricId,
      Long criterionOneId,
      Long criterionTwoId,
      Long sectionId,
      Long teamId,
      Long studentOneId,
      String studentOneEmail,
      Long studentTwoId,
      String studentTwoEmail,
      Long studentThreeId,
      String studentThreeEmail,
      Long previousActiveWeekId,
      Long currentActiveWeekId,
      LocalDate previousWeekStart,
      LocalDate currentWeekStart) {
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
}
