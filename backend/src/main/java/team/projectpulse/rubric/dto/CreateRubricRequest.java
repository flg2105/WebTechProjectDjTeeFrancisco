package team.projectpulse.rubric.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

public class CreateRubricRequest {
  @NotBlank
  private String name;

  @Valid
  @NotEmpty
  private List<CreateRubricCriterionRequest> criteria = new ArrayList<>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<CreateRubricCriterionRequest> getCriteria() {
    return criteria;
  }

  public void setCriteria(List<CreateRubricCriterionRequest> criteria) {
    this.criteria = criteria;
  }
}
