package team.projectpulse.rubric;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import team.projectpulse.rubric.repository.RubricRepository;
import team.projectpulse.system.StatusCode;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RubricControllerIntegrationTest {
  @Autowired
  private MockMvc mvc;

  @Autowired
  private RubricRepository rubricRepository;

  @BeforeEach
  void setUp() {
    rubricRepository.deleteAll();
  }

  @Test
  void should_CreateRubric_When_RequestIsValid() throws Exception {
    mvc.perform(post("/api/rubrics")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validRubricJson("Peer Eval Rubric v1")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Create Rubric Success"))
        .andExpect(jsonPath("$.data.name").value("Peer Eval Rubric v1"))
        .andExpect(jsonPath("$.data.criteria[0].name").value("Quality of work"))
        .andExpect(jsonPath("$.data.criteria[0].maxScore").value(10))
        .andExpect(jsonPath("$.data.criteria[0].position").value(1))
        .andExpect(jsonPath("$.data.criteria[1].name").value("Productivity"))
        .andExpect(jsonPath("$.data.criteria[1].position").value(2));
  }

  @Test
  void should_ReturnConflict_When_RubricNameAlreadyExists() throws Exception {
    mvc.perform(post("/api/rubrics")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validRubricJson("Peer Eval Rubric v1")))
        .andExpect(status().isOk());

    mvc.perform(post("/api/rubrics")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validRubricJson("peer eval rubric v1")))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.CONFLICT))
        .andExpect(jsonPath("$.message").value("Rubric name already exists"))
        .andExpect(jsonPath("$.data").value(nullValue()));
  }

  @Test
  void should_ReturnInvalidArgument_When_CriterionMaxScoreIsNotPositive() throws Exception {
    mvc.perform(post("/api/rubrics")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "Invalid Rubric",
                  "criteria": [
                    {
                      "name": "Quality of work",
                      "description": "How do you rate quality?",
                      "maxScore": 0,
                      "position": 1
                    }
                  ]
                }
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT));
  }

  @Test
  void should_FindAllRubrics_When_RubricsExist() throws Exception {
    mvc.perform(post("/api/rubrics")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validRubricJson("Peer Eval Rubric v1")))
        .andExpect(status().isOk());

    mvc.perform(get("/api/rubrics"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find Rubrics Success"))
        .andExpect(jsonPath("$.data[0].name").value("Peer Eval Rubric v1"))
        .andExpect(jsonPath("$.data[0].criteria[0].name").value("Quality of work"));
  }

  @Test
  void should_FindRubricById_When_RubricExists() throws Exception {
    String response = mvc.perform(post("/api/rubrics")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validRubricJson("Peer Eval Rubric v1")))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    Number id = com.jayway.jsonpath.JsonPath.read(response, "$.data.id");

    mvc.perform(get("/api/rubrics/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find Rubric Success"))
        .andExpect(jsonPath("$.data.id").value(id))
        .andExpect(jsonPath("$.data.criteria[1].description").value("How productive is this teammate?"));
  }

  @Test
  void should_ReturnNotFound_When_RubricDoesNotExist() throws Exception {
    mvc.perform(get("/api/rubrics/{id}", 999L))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Rubric not found with id 999"))
        .andExpect(jsonPath("$.data").value(nullValue()));
  }

  private String validRubricJson(String name) {
    return """
        {
          "name": "%s",
          "criteria": [
            {
              "name": "Quality of work",
              "description": "How do you rate the quality of this teammate's work?",
              "maxScore": 10,
              "position": 1
            },
            {
              "name": "Productivity",
              "description": "How productive is this teammate?",
              "maxScore": 10,
              "position": 2
            }
          ]
        }
        """.formatted(name);
  }
}
