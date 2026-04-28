package team.projectpulse.war;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static team.projectpulse.system.StatusCode.INVALID_ARGUMENT;
import static team.projectpulse.system.StatusCode.NOT_FOUND;
import static team.projectpulse.system.StatusCode.SUCCESS;

import java.time.DayOfWeek;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WarControllerIntegrationTest {

  @Autowired
  private MockMvc mvc;

  @Test
  void should_ManageWarActivities_ForCurrentWeek() throws Exception {
    Long rubricId = createRubric("Peer Eval Rubric WAR Flow");
    LocalDate currentMonday = LocalDate.now().with(DayOfWeek.MONDAY);
    Long sectionId = createSection("WAR Flow Section", rubricId, currentMonday.minusWeeks(2), currentMonday.plusWeeks(4));
    Long activeWeekId = createActiveWeek(sectionId, currentMonday, true);
    Long studentId = setupStudent("war.student@example.edu", "WAR Student");
    Long teamId = createTeam(sectionId, "WAR Team");
    assignStudent(teamId, studentId);

    mvc.perform(get("/api/wars")
            .with(student("war.student@example.edu"))
            .param("studentUserId", String.valueOf(studentId))
            .param("activeWeekId", String.valueOf(activeWeekId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.activities.length()").value(0));

    MvcResult addResult = mvc.perform(post("/api/wars/activities")
            .with(student("war.student@example.edu"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "studentUserId": %d,
                  "activeWeekId": %d,
                  "category": "DEVELOPMENT",
                  "activity": "Implement weekly WAR endpoint",
                  "description": "Built the student WAR add flow.",
                  "hoursPlanned": 4.0,
                  "hoursActual": 4.5,
                  "status": "IN_PROGRESS"
                }
                """.formatted(studentId, activeWeekId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.activities.length()").value(1))
        .andExpect(jsonPath("$.data.activities[0].activity").value("Implement weekly WAR endpoint"))
        .andReturn();

    Long activityId = readId(addResult, "\"activities\":[{\"id\":");

    mvc.perform(put("/api/wars/activities/" + activityId)
            .with(student("war.student@example.edu"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "studentUserId": %d,
                  "activeWeekId": %d,
                  "category": "TESTING",
                  "activity": "Test weekly WAR endpoint",
                  "description": "Covered add and update behavior.",
                  "hoursPlanned": 2.0,
                  "hoursActual": 2.0,
                  "status": "DONE"
                }
                """.formatted(studentId, activeWeekId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.activities[0].category").value("TESTING"))
        .andExpect(jsonPath("$.data.activities[0].status").value("DONE"));

    mvc.perform(delete("/api/wars/activities/" + activityId)
            .with(student("war.student@example.edu"))
            .param("studentUserId", String.valueOf(studentId))
            .param("activeWeekId", String.valueOf(activeWeekId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.activities.length()").value(0));
  }

  @Test
  void should_RejectFutureWeek_When_AddingWarActivity() throws Exception {
    Long rubricId = createRubric("Peer Eval Rubric Future WAR");
    LocalDate currentMonday = LocalDate.now().with(DayOfWeek.MONDAY);
    Long sectionId = createSection("WAR Future Section", rubricId, currentMonday.minusWeeks(1), currentMonday.plusWeeks(6));
    Long futureWeekId = createActiveWeek(sectionId, currentMonday.plusWeeks(1), true);
    Long studentId = setupStudent("future.war.student@example.edu", "Future WAR Student");
    Long teamId = createTeam(sectionId, "Future WAR Team");
    assignStudent(teamId, studentId);

    mvc.perform(post("/api/wars/activities")
            .with(student("future.war.student@example.edu"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "studentUserId": %d,
                  "activeWeekId": %d,
                  "category": "PLANNING",
                  "activity": "Plan next sprint",
                  "description": "Prepared tasks for next week.",
                  "hoursPlanned": 2.0,
                  "hoursActual": 1.0,
                  "status": "IN_PROGRESS"
                }
                """.formatted(studentId, futureWeekId)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT))
        .andExpect(jsonPath("$.message").value("Selected week cannot be in the future"));
  }

  @Test
  void should_GenerateStudentWarReport_ForDateRange() throws Exception {
    Long rubricId = createRubric("WAR Report Rubric");
    LocalDate currentMonday = LocalDate.now().with(DayOfWeek.MONDAY);
    Long sectionId = createSection("WAR Report Section", rubricId, currentMonday.minusWeeks(4), currentMonday.plusWeeks(2));
    Long[] weekIds = replaceActiveWeeks(sectionId, currentMonday.minusWeeks(1), currentMonday);
    Long weekOneId = weekIds[0];
    Long weekTwoId = weekIds[1];
    Long studentId = setupStudent("war.report.student@example.edu", "WAR Report Student");
    Long teamId = createTeam(sectionId, "WAR Report Team");
    assignStudent(teamId, studentId);

    addActivity("war.report.student@example.edu", studentId, weekOneId, "DEVELOPMENT", "Implement report endpoint");
    addActivity("war.report.student@example.edu", studentId, weekTwoId, "TESTING", "Test report endpoint");

    mvc.perform(get("/api/wars/student-report")
            .with(instructor())
            .param("studentUserId", String.valueOf(studentId))
            .param("startActiveWeekId", String.valueOf(weekOneId))
            .param("endActiveWeekId", String.valueOf(weekTwoId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.sectionId").value(sectionId))
        .andExpect(jsonPath("$.data.entries.length()").value(2))
        .andExpect(jsonPath("$.data.entries[0].activeWeekId").value(weekOneId))
        .andExpect(jsonPath("$.data.entries[0].activities[0].activity").value("Implement report endpoint"))
        .andExpect(jsonPath("$.data.entries[1].activeWeekId").value(weekTwoId))
        .andExpect(jsonPath("$.data.entries[1].activities[0].activity").value("Test report endpoint"));
  }

  @Test
  void should_ReturnNotFound_When_StudentWarReportHasNoActivities() throws Exception {
    Long rubricId = createRubric("WAR Report Empty Rubric");
    LocalDate currentMonday = LocalDate.now().with(DayOfWeek.MONDAY);
    Long sectionId = createSection("WAR Empty Report Section", rubricId, currentMonday.minusWeeks(4), currentMonday.plusWeeks(2));
    Long[] weekIds = replaceActiveWeeks(sectionId, currentMonday.minusWeeks(1), currentMonday);
    Long weekOneId = weekIds[0];
    Long weekTwoId = weekIds[1];
    Long studentId = setupStudent("war.report.empty.student@example.edu", "WAR Empty Report Student");
    Long teamId = createTeam(sectionId, "WAR Empty Report Team");
    assignStudent(teamId, studentId);

    mvc.perform(get("/api/wars/student-report")
            .with(instructor())
            .param("studentUserId", String.valueOf(studentId))
            .param("startActiveWeekId", String.valueOf(weekOneId))
            .param("endActiveWeekId", String.valueOf(weekTwoId)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(NOT_FOUND))
        .andExpect(jsonPath("$.message").value("No WAR report data available for the selected period"));
  }

  private Long createRubric(String name) throws Exception {
    MvcResult result = mvc.perform(post("/api/rubrics")
            .with(admin())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "%s",
                  "criteria": [
                    {
                      "name": "Quality of work",
                      "description": "Rate quality of work.",
                      "maxScore": 10
                    }
                  ]
                }
                """.formatted(name)))
        .andExpect(status().isOk())
        .andReturn();

    return readId(result, "\"id\":");
  }

  private Long createSection(String name, Long rubricId, LocalDate startDate, LocalDate endDate) throws Exception {
    MvcResult result = mvc.perform(post("/api/sections")
            .with(admin())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "%s",
                  "academicYear": "2026-2027",
                  "startDate": "%s",
                  "endDate": "%s",
                  "rubricId": %d
                }
                """.formatted(name, startDate, endDate, rubricId)))
        .andExpect(status().isOk())
        .andReturn();

    return readId(result, "\"id\":");
  }

  private Long createActiveWeek(Long sectionId, LocalDate weekStartDate, boolean active) throws Exception {
    MvcResult result = mvc.perform(put("/api/sections/" + sectionId + "/active-weeks")
            .with(admin())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                [
                  {"weekStartDate": "%s", "active": %s}
                ]
                """.formatted(weekStartDate, active)))
        .andExpect(status().isOk())
        .andReturn();

    return readId(result, "\"activeWeeks\":[{\"id\":");
  }

  private Long[] replaceActiveWeeks(Long sectionId, LocalDate weekOneStartDate, LocalDate weekTwoStartDate) throws Exception {
    MvcResult result = mvc.perform(put("/api/sections/" + sectionId + "/active-weeks")
            .with(admin())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                [
                  {"weekStartDate": "%s", "active": true},
                  {"weekStartDate": "%s", "active": true}
                ]
                """.formatted(weekOneStartDate, weekTwoStartDate)))
        .andExpect(status().isOk())
        .andReturn();

    return new Long[] {
        readNthId(result, "\"activeWeeks\":[", "\"id\":", 0),
        readNthId(result, "\"activeWeeks\":[", "\"id\":", 1)
    };
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

    return readId(result, "\"id\":");
  }

  private Long createTeam(Long sectionId, String name) throws Exception {
    MvcResult result = mvc.perform(post("/api/teams")
            .with(admin())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "sectionId": %d,
                  "name": "%s"
                }
                """.formatted(sectionId, name)))
        .andExpect(status().isOk())
        .andReturn();

    return readId(result, "\"id\":");
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

  private void addActivity(String email, Long studentId, Long activeWeekId, String category, String activity) throws Exception {
    mvc.perform(post("/api/wars/activities")
            .with(student(email))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "studentUserId": %d,
                  "activeWeekId": %d,
                  "category": "%s",
                  "activity": "%s",
                  "description": "Report demo activity.",
                  "hoursPlanned": 1.0,
                  "hoursActual": 1.0,
                  "status": "IN_PROGRESS"
                }
                """.formatted(studentId, activeWeekId, category, activity)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS));
  }

  private Long readId(MvcResult result, String marker) throws Exception {
    String body = result.getResponse().getContentAsString();
    int start = body.indexOf(marker);
    if (start < 0) {
      throw new IllegalStateException("Marker not found: " + marker + " in " + body);
    }
    start += marker.length();
    int end = body.indexOf(",", start);
    if (end < 0) {
      end = body.indexOf("}", start);
    }
    return Long.valueOf(body.substring(start, end));
  }

  private Long readNthId(MvcResult result, String anchor, String marker, int index) throws Exception {
    String body = result.getResponse().getContentAsString();
    int anchorStart = body.indexOf(anchor);
    if (anchorStart < 0) {
      throw new IllegalStateException("Anchor not found: " + anchor + " in " + body);
    }
    String substring = body.substring(anchorStart);
    int pos = -1;
    for (int i = 0; i <= index; i++) {
      pos = substring.indexOf(marker, pos + 1);
      if (pos < 0) {
        throw new IllegalStateException("Marker not found at index " + index + ": " + marker + " in " + substring);
      }
    }
    int start = pos + marker.length();
    int end = substring.indexOf(",", start);
    if (end < 0) {
      end = substring.indexOf("}", start);
    }
    return Long.valueOf(substring.substring(start, end));
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
