package team.projectpulse.team;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static team.projectpulse.system.StatusCode.INVALID_ARGUMENT;
import static team.projectpulse.system.StatusCode.SUCCESS;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TeamInstructorAssignmentIntegrationTest {

  @Autowired
  private MockMvc mvc;

  @Test
  void should_AssignAndRemoveInstructors_FromTeam() throws Exception {
    String suffix = uniqueSuffix();
    Long rubricId = createRubric("UC-19 Rubric " + suffix);
    Long sectionId = createSection(rubricId, "UC-19 Section " + suffix);
    Long teamId = createTeam(sectionId, "UC-19 Team " + suffix);
    Long instructorId = setupInstructor(uniqueEmail("uc19.instructor"), "UC-19 Instructor " + suffix);

    assignInstructorToSection(sectionId, instructorId);

    mvc.perform(post("/api/teams/" + teamId + "/instructors")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "instructorUserIds": [%d]
                }
                """.formatted(instructorId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.instructorUserIds[0]").value(instructorId));

    mvc.perform(delete("/api/teams/" + teamId + "/instructors/" + instructorId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.instructorUserIds.length()").value(0));
  }

  @Test
  void should_RejectTeamAssignment_When_InstructorNotAssignedToSection() throws Exception {
    String suffix = uniqueSuffix();
    Long rubricId = createRubric("UC-19 Rubric Reject " + suffix);
    Long sectionId = createSection(rubricId, "UC-19 Section Reject " + suffix);
    Long teamId = createTeam(sectionId, "UC-19 Team Reject " + suffix);
    Long instructorId = setupInstructor(uniqueEmail("uc19.unassigned.instructor"), "UC-19 Unassigned Instructor " + suffix);

    mvc.perform(post("/api/teams/" + teamId + "/instructors")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "instructorUserIds": [%d]
                }
                """.formatted(instructorId)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT));
  }

  private void assignInstructorToSection(Long sectionId, Long instructorUserId) throws Exception {
    mvc.perform(post("/api/sections/" + sectionId + "/instructors")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "instructorUserIds": [%d]
                }
                """.formatted(instructorUserId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS));
  }

  private String uniqueSuffix() {
    return UUID.randomUUID().toString();
  }

  private String uniqueEmail(String prefix) {
    String token = UUID.randomUUID().toString().replace("-", "");
    return "%s.%s@example.edu".formatted(prefix, token);
  }

  private Long createRubric(String name) throws Exception {
    MvcResult result = mvc.perform(post("/api/rubrics")
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
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andReturn();

    return readId(result);
  }

  private Long createSection(Long rubricId, String name) throws Exception {
    MvcResult result = mvc.perform(post("/api/sections")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "%s",
                  "academicYear": "2026-2027",
                  "startDate": "2026-08-31",
                  "endDate": "2027-04-30",
                  "rubricId": %d
                }
                """.formatted(name, rubricId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andReturn();

    return readId(result);
  }

  private Long createTeam(Long sectionId, String name) throws Exception {
    MvcResult result = mvc.perform(post("/api/teams")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "sectionId": %d,
                  "name": "%s"
                }
                """.formatted(sectionId, name)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
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
                  "password": "TestPassword123!"
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
}
