package team.projectpulse.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static team.projectpulse.system.StatusCode.NOT_FOUND;
import static team.projectpulse.system.StatusCode.SUCCESS;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import team.projectpulse.team.domain.TeamMembership;
import team.projectpulse.team.repository.TeamMembershipRepository;
import team.projectpulse.user.domain.Invitation;
import team.projectpulse.user.domain.UserRole;
import team.projectpulse.user.repository.InvitationRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserPhaseThreeIntegrationTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private TeamMembershipRepository teamMembershipRepository;

  @Autowired
  private InvitationRepository invitationRepository;

  @Test
  void should_EditAccount_ById() throws Exception {
    Long studentId = setupStudent("phase3.editme@example.edu", "Edit Me");

    mvc.perform(put("/api/users/" + studentId)
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

    mvc.perform(get("/api/students").param("q", "findstudents"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.length()").value(2));

    mvc.perform(get("/api/students").param("q", "phase3.findstudents.bob"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].email").value("phase3.findstudents.bob@example.edu"));
  }

  @Test
  void should_ViewStudent_WithStubbedWarAndPeerEvalFields() throws Exception {
    Long studentId = setupStudent("phase3.viewme@example.edu", "View Me");

    mvc.perform(get("/api/students/" + studentId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.id").value(studentId))
        .andExpect(jsonPath("$.data.warEntryIds.length()").value(0))
        .andExpect(jsonPath("$.data.peerEvaluationIds.length()").value(0));
  }

  @Test
  void should_FindInstructors_AsLookupStub() throws Exception {
    setupStudent("phase3.instructorstub.student@example.edu", "Instructor Stub Student");
    setupInstructor("phase3.instructorstub.instructor@example.edu", "Instructor Stub Instructor");

    mvc.perform(get("/api/instructors").param("q", "phase3.instructorstub"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].email").value("phase3.instructorstub.instructor@example.edu"))
        .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
  }

  @Test
  void should_DeleteStudent_AndCleanupMembershipsAndInvites() throws Exception {
    String email = "phase3.deleteme@example.edu";
    Long studentId = setupStudent(email, "Delete Me");

    TeamMembership membership = new TeamMembership();
    membership.setTeamId(123L);
    membership.setStudentUserId(studentId);
    teamMembershipRepository.save(membership);

    Invitation invitation = new Invitation();
    invitation.setEmail(email);
    invitation.setRole(UserRole.STUDENT);
    invitation.setSectionId(null);
    invitationRepository.save(invitation);

    mvc.perform(delete("/api/students/" + studentId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data").value(nullValue()));

    mvc.perform(get("/api/students/" + studentId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(NOT_FOUND));

    assertFalse(teamMembershipRepository.existsByStudentUserId(studentId));
    assertFalse(invitationRepository.existsByEmailIgnoreCaseAndRole(email, UserRole.STUDENT));
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
}
