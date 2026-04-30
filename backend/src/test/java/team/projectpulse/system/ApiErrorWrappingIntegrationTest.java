package team.projectpulse.system;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiErrorWrappingIntegrationTest {

  @Autowired
  private MockMvc mvc;

  @Test
  void should_ReturnResultWrapper_When_PathNotFound() throws Exception {
    mvc.perform(get("/api/does-not-exist").with(user()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Not found"))
        .andExpect(jsonPath("$.data").value(nullValue()));
  }

  @Test
  void should_ReturnResultWrapper_When_MethodNotAllowed() throws Exception {
    mvc.perform(post("/api/health").with(user()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
        .andExpect(jsonPath("$.message").value("Method not allowed"))
        .andExpect(jsonPath("$.data").value(nullValue()));
  }

  private SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor user() {
    return SecurityMockMvcRequestPostProcessors.user("admin@test.local").roles("ADMIN");
  }
}
