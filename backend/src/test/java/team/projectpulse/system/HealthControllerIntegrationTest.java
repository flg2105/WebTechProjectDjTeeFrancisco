package team.projectpulse.system;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import team.projectpulse.user.domain.ProjectUser;
import team.projectpulse.user.domain.UserRole;
import team.projectpulse.user.domain.UserStatus;
import team.projectpulse.user.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HealthControllerIntegrationTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Test
  void should_ReturnResultWrapper_When_HealthCalled() throws Exception {
    setupAdmin("health.admin@example.edu", "Health Admin", "projectpulse123");
    String token = loginAndReadToken("health.admin@example.edu", "projectpulse123");

    mvc.perform(get("/api/health")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("OK"))
        .andExpect(jsonPath("$.data").value("ok"));
  }

  private void setupAdmin(String email, String displayName, String password) {
    if (userRepository.existsByEmailIgnoreCase(email)) {
      return;
    }
    ProjectUser admin = new ProjectUser();
    admin.setEmail(email);
    admin.setDisplayName(displayName);
    admin.setPasswordHash(passwordEncoder.encode(password));
    admin.setRole(UserRole.ADMIN);
    admin.setStatus(UserStatus.ACTIVE);
    userRepository.save(admin);
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
