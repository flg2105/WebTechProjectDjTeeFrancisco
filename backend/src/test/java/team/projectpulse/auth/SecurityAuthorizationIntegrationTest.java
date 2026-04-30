package team.projectpulse.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static team.projectpulse.system.StatusCode.FORBIDDEN;
import static team.projectpulse.system.StatusCode.SUCCESS;
import static team.projectpulse.system.StatusCode.UNAUTHORIZED;

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
class SecurityAuthorizationIntegrationTest {

  @Autowired
  private MockMvc mvc;

  @Test
  void should_ReturnUnauthorized_When_RequestHasNoAuth() throws Exception {
    mvc.perform(get("/api/sections"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(UNAUTHORIZED));
  }

  @Test
  void should_ReturnForbidden_When_StudentCreatesRubric() throws Exception {
    mvc.perform(post("/api/rubrics")
            .with(student("student.authz@example.edu"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "Blocked Rubric",
                  "criteria": [
                    {
                      "name": "Quality of work",
                      "description": "Rate quality of work.",
                      "maxScore": 10
                    }
                  ]
                }
                """))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(FORBIDDEN));
  }

  @Test
  void should_ReturnForbidden_When_StudentViewsAnotherStudentsWar() throws Exception {
    Long studentOneId = setupStudent("student.one.authz@example.edu", "Student One");
    Long studentTwoId = setupStudent("student.two.authz@example.edu", "Student Two");

    mvc.perform(get("/api/wars")
            .with(student("student.one.authz@example.edu"))
            .param("studentUserId", String.valueOf(studentTwoId))
            .param("activeWeekId", "1"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(FORBIDDEN));
  }

  @Test
  void should_AllowInstructorToViewStudents() throws Exception {
    mvc.perform(get("/api/students")
            .with(instructor())
            .param("q", "nobody"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS));
  }

  @Test
  void should_AllowInstructorToDeleteSection() throws Exception {
    Long sectionId = createSectionForAuthorization();

    mvc.perform(delete("/api/sections/" + sectionId)
            .with(instructor()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS));
  }

  @Test
  void should_ReturnForbidden_When_StudentDeletesSection() throws Exception {
    Long sectionId = createSectionForAuthorization();

    mvc.perform(delete("/api/sections/" + sectionId)
            .with(student("student.authz@example.edu")))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(FORBIDDEN));
  }

  private Long createSectionForAuthorization() throws Exception {
    long unique = System.nanoTime();

    MvcResult rubricResult = mvc.perform(post("/api/rubrics")
            .with(admin())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "Authorization Section Rubric %d",
                  "criteria": [
                    {
                      "name": "Quality of work",
                      "description": "Rate quality of work.",
                      "maxScore": 10
                    }
                  ]
                }
                """.formatted(unique)))
        .andExpect(status().isOk())
        .andReturn();

    Long rubricId = readId(rubricResult);

    MvcResult sectionResult = mvc.perform(post("/api/sections")
            .with(admin())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "Authorization Section %d",
                  "academicYear": "2026-2027",
                  "startDate": "2026-08-31",
                  "endDate": "2027-04-30",
                  "rubricId": %d
                }
                """.formatted(unique, rubricId)))
        .andExpect(status().isOk())
        .andReturn();

    return readId(sectionResult);
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

    String body = result.getResponse().getContentAsString();
    String marker = "\"id\":";
    int start = body.indexOf(marker) + marker.length();
    int end = body.indexOf(",", start);
    return Long.valueOf(body.substring(start, end));
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

  private SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor instructor() {
    return SecurityMockMvcRequestPostProcessors.user("instructor@test.local").roles("INSTRUCTOR");
  }

  private SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor student(String email) {
    return SecurityMockMvcRequestPostProcessors.user(email).roles("STUDENT");
  }
}
