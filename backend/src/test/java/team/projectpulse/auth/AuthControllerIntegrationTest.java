package team.projectpulse.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static team.projectpulse.system.StatusCode.SUCCESS;
import static team.projectpulse.system.StatusCode.UNAUTHORIZED;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

  @Autowired
  private MockMvc mvc;

  @Test
  void should_LoginWithBasicAuth_AndReturnJwt() throws Exception {
    setupStudent("auth.student@example.edu", "Auth Student", "projectpulse123");

    mvc.perform(post("/api/auth/login")
            .header(HttpHeaders.AUTHORIZATION, basic("auth.student@example.edu", "projectpulse123")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
        .andExpect(jsonPath("$.data.accessToken").isString())
        .andExpect(jsonPath("$.data.email").value("auth.student@example.edu"))
        .andExpect(jsonPath("$.data.role").value("STUDENT"));
  }

  @Test
  void should_RejectLogin_WhenPasswordIsWrong() throws Exception {
    setupStudent("auth.invalid@example.edu", "Auth Invalid", "projectpulse123");

    mvc.perform(post("/api/auth/login")
            .header(HttpHeaders.AUTHORIZATION, basic("auth.invalid@example.edu", "wrongpassword")))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(UNAUTHORIZED));
  }

  @Test
  void should_RequireJwt_WhenFetchingCurrentUser() throws Exception {
    mvc.perform(get("/api/auth/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(UNAUTHORIZED));
  }

  @Test
  void should_AcceptJwt_WhenFetchingCurrentUser() throws Exception {
    setupStudent("auth.me@example.edu", "Auth Me", "projectpulse123");
    String token = loginAndReadToken("auth.me@example.edu", "projectpulse123");

    mvc.perform(get("/api/auth/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS))
        .andExpect(jsonPath("$.data.email").value("auth.me@example.edu"))
        .andExpect(jsonPath("$.data.role").value("STUDENT"));
  }

  private void setupStudent(String email, String displayName, String password) throws Exception {
    mvc.perform(post("/api/users/student-setup")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "email": "%s",
                  "displayName": "%s",
                  "password": "%s"
                }
                """.formatted(email, displayName, password)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(SUCCESS));
  }

  private String loginAndReadToken(String email, String password) throws Exception {
    MvcResult result = mvc.perform(post("/api/auth/login")
            .header(HttpHeaders.AUTHORIZATION, basic(email, password)))
        .andExpect(status().isOk())
        .andReturn();

    String body = result.getResponse().getContentAsString();
    String marker = "\"accessToken\":\"";
    int start = body.indexOf(marker) + marker.length();
    int end = body.indexOf("\"", start);
    return body.substring(start, end);
  }

  private String basic(String username, String password) {
    String credentials = username + ":" + password;
    String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    return "Basic " + encoded;
  }
}
