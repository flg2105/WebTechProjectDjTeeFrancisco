package team.projectpulse.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static team.projectpulse.system.StatusCode.INVALID_ARGUMENT;
import static team.projectpulse.system.StatusCode.NOT_FOUND;
import static team.projectpulse.system.StatusCode.SUCCESS;

import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminSetupFlowIntegrationTest {

  @Autowired
  private MockMvc mvc;

  @Test
  void should_CreateAdminSetupContainerData_ForPhaseTwoFlow() throws Exception {
    Long rubricId = createRubric();
    Long sectionId = createSection(rubricId);
    createActiveWeeks(sectionId);
    Long studentId = setupStudent();
    Long teamId = createTeam(sectionId);

    mvc.perform(post("/api/teams/" + teamId + "/students")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"studentUserId\":" + studentId + "}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.studentUserIds[0]").value(studentId));
  }

  @Test
  void should_RejectActiveWeek_When_StartDateIsNotMonday() throws Exception {
    Long rubricId = createRubricWithName("Peer Eval Rubric Invalid Week Test");
    Long sectionId = createSectionWithName(rubricId, "Invalid Week Section");

    mvc.perform(put("/api/sections/" + sectionId + "/active-weeks")
            .contentType(MediaType.APPLICATION_JSON)
            .content("[{\"weekStartDate\":\"2026-09-01\",\"active\":true}]"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT));
  }

  @Test
  void should_InviteStudents_AndRecordInvitedUsers() throws Exception {
    Long rubricId = createRubricWithName("Peer Eval Rubric Invite Students Test");
    Long sectionId = createSectionWithName(rubricId, "Invite Students Section");

    mvc.perform(post("/api/invitations/students")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "sectionId": %d,
                  "emails": ["invite.student.one@example.edu", "invite.student.two@example.edu"]
                }
                """.formatted(sectionId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.role").value("STUDENT"))
        .andExpect(jsonPath("$.data.sectionId").value(sectionId))
        .andExpect(jsonPath("$.data.users.length()").value(2))
        .andExpect(jsonPath("$.data.users[0].status").value("INVITED"))
        .andExpect(jsonPath("$.data.users[1].status").value("INVITED"));
  }

  @Test
  void should_RejectStudentInvite_When_SectionIdMissing() throws Exception {
    mvc.perform(post("/api/invitations/students")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "emails": ["missing.section@example.edu"]
                }
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT));
  }

  @Test
  void should_RejectStudentInvite_When_SectionNotFound() throws Exception {
    mvc.perform(post("/api/invitations/students")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "sectionId": 999999,
                  "emails": ["bad.section@example.edu"]
                }
                """))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(NOT_FOUND));
  }

  @Test
  void should_InviteInstructors_AndRecordInvitedUsers() throws Exception {
    mvc.perform(post("/api/invitations/instructors")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "emails": ["invite.instructor@example.edu"]
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.role").value("INSTRUCTOR"))
        .andExpect(jsonPath("$.data.sectionId").value(nullValue()))
        .andExpect(jsonPath("$.data.users.length()").value(1))
        .andExpect(jsonPath("$.data.users[0].status").value("INVITED"));
  }

  private Long createRubric() throws Exception {
    return createRubricWithName("Peer Eval Rubric Phase 2 Test");
  }

  private Long createRubricWithName(String name) throws Exception {
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

  private Long createSection(Long rubricId) throws Exception {
    return createSectionWithName(rubricId, "Phase 2 Section");
  }

  private Long createSectionWithName(Long rubricId, String name) throws Exception {
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

  private void createActiveWeeks(Long sectionId) throws Exception {
    mvc.perform(put("/api/sections/" + sectionId + "/active-weeks")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                [
                  {"weekStartDate": "2026-08-31", "active": true},
                  {"weekStartDate": "2026-09-07", "active": false}
                ]
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.activeWeeks.length()").value(2));
  }

  private Long setupStudent() throws Exception {
    MvcResult result = mvc.perform(post("/api/users/student-setup")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "email": "phase2.student@example.edu",
                  "displayName": "Phase Two Student",
                  "password": "projectpulse123"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.role").value("STUDENT"))
        .andReturn();

    return readId(result);
  }

  private Long createTeam(Long sectionId) throws Exception {
    MvcResult result = mvc.perform(post("/api/teams")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "sectionId": %d,
                  "name": "Phase 2 Team"
                }
                """.formatted(sectionId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
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
