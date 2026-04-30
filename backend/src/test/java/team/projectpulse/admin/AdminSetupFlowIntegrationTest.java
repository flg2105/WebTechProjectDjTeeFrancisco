package team.projectpulse.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
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
            .with(admin())
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
            .with(admin())
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
            .with(admin())
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
            .with(admin())
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
            .with(admin())
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
            .with(admin())
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

  @Test
  void should_ViewSectionDetails_WithTeamsAssignmentsUnassignedListsAndRubric() throws Exception {
    Long rubricId = createRubricWithName("Peer Eval Rubric Section Details Test");
    Long sectionId = createSectionWithName(rubricId, "Section Details Section");
    createActiveWeeks(sectionId);

    Long assignedStudentId = setupStudentWithEmail("assigned.student@example.edu", "Assigned Student");
    Long unassignedStudentId = setupStudentWithEmail("unassigned.student@example.edu", "Unassigned Student");
    inviteStudentToSection(sectionId, "assigned.student@example.edu");
    inviteStudentToSection(sectionId, "unassigned.student@example.edu");

    Long teamInstructorId = setupInstructorWithEmail("team.instructor@example.edu", "Team Instructor");
    Long sectionOnlyInstructorId = setupInstructorWithEmail("section.only.instructor@example.edu", "Section Only Instructor");

    assignInstructorToSection(sectionId, teamInstructorId, sectionOnlyInstructorId);

    Long teamId = createTeamWithName(sectionId, "Section Details Team");
    assignStudentToTeam(teamId, assignedStudentId);
    assignInstructorToTeam(teamId, teamInstructorId);

    mvc.perform(get("/api/sections/" + sectionId).with(admin()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.name").value("Section Details Section"))
        .andExpect(jsonPath("$.data.teams.length()").value(1))
        .andExpect(jsonPath("$.data.teams[0].name").value("Section Details Team"))
        .andExpect(jsonPath("$.data.teams[0].members.length()").value(1))
        .andExpect(jsonPath("$.data.teams[0].members[0].id").value(assignedStudentId))
        .andExpect(jsonPath("$.data.teams[0].instructors.length()").value(1))
        .andExpect(jsonPath("$.data.teams[0].instructors[0].id").value(teamInstructorId))
        .andExpect(jsonPath("$.data.unassignedStudents.length()").value(1))
        .andExpect(jsonPath("$.data.unassignedStudents[0].id").value(unassignedStudentId))
        .andExpect(jsonPath("$.data.unassignedInstructors.length()").value(1))
        .andExpect(jsonPath("$.data.unassignedInstructors[0].id").value(sectionOnlyInstructorId))
        .andExpect(jsonPath("$.data.rubricUsed.id").value(rubricId))
        .andExpect(jsonPath("$.data.rubricUsed.criteria.length()").value(1));
  }

  @Test
  void should_DeleteSection_AndCascadeRelatedRecords() throws Exception {
    Long rubricId = createRubricWithName("Delete Section Rubric");
    Long sectionId = createSectionWithName(rubricId, "Delete Section");
    createActiveWeeks(sectionId);

    Long studentId = setupStudentWithEmail("delete.section.student@example.edu", "Delete Section Student");
    Long instructorId = setupInstructorWithEmail("delete.section.instructor@example.edu", "Delete Section Instructor");

    inviteStudentToSection(sectionId, "delete.section.student@example.edu");
    assignInstructorToSection(sectionId, instructorId);

    Long teamId = createTeamWithName(sectionId, "Delete Section Team");
    assignStudentToTeam(teamId, studentId);
    assignInstructorToTeam(teamId, instructorId);

    mvc.perform(delete("/api/sections/" + sectionId).with(admin()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS));

    mvc.perform(get("/api/sections/" + sectionId).with(admin()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(NOT_FOUND));
  }

  private Long createRubric() throws Exception {
    return createRubricWithName("Peer Eval Rubric Phase 2 Test");
  }

  private Long createRubricWithName(String name) throws Exception {
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
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andReturn();

    return readId(result);
  }

  private Long createSection(Long rubricId) throws Exception {
    return createSectionWithName(rubricId, "Phase 2 Section");
  }

  private Long createTeamWithName(Long sectionId, String name) throws Exception {
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
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andReturn();

    return readId(result);
  }

  private Long createSectionWithName(Long rubricId, String name) throws Exception {
    MvcResult result = mvc.perform(post("/api/sections")
            .with(admin())
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
            .with(admin())
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
    return setupStudentWithEmail("phase2.student@example.edu", "Phase Two Student");
  }

  private Long setupStudentWithEmail(String email, String displayName) throws Exception {
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

  private Long setupInstructorWithEmail(String email, String displayName) throws Exception {
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

  private void inviteStudentToSection(Long sectionId, String email) throws Exception {
    mvc.perform(post("/api/invitations/students")
            .with(admin())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "sectionId": %d,
                  "emails": ["%s"]
                }
                """.formatted(sectionId, email)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS));
  }

  private void assignInstructorToSection(Long sectionId, Long... instructorUserIds) throws Exception {
    String ids = java.util.Arrays.stream(instructorUserIds)
        .map(String::valueOf)
        .collect(java.util.stream.Collectors.joining(","));

    mvc.perform(post("/api/sections/" + sectionId + "/instructors")
            .with(admin())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "instructorUserIds": [%s]
                }
                """.formatted(ids)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS));
  }

  private void assignInstructorToTeam(Long teamId, Long instructorUserId) throws Exception {
    mvc.perform(post("/api/teams/" + teamId + "/instructors")
            .with(admin())
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

  private void assignStudentToTeam(Long teamId, Long studentUserId) throws Exception {
    mvc.perform(post("/api/teams/" + teamId + "/students")
            .with(admin())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "studentUserId": %d
                }
                """.formatted(studentUserId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS));
  }

  private Long createTeam(Long sectionId) throws Exception {
    MvcResult result = mvc.perform(post("/api/teams")
            .with(admin())
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

  private SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor admin() {
    return SecurityMockMvcRequestPostProcessors.user("admin@test.local").roles("ADMIN");
  }
}
