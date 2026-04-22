package team.projectpulse.rubric;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.projectpulse.rubric.domain.Rubric;
import team.projectpulse.rubric.dto.CreateRubricCriterionRequest;
import team.projectpulse.rubric.dto.CreateRubricRequest;
import team.projectpulse.rubric.dto.RubricResponse;
import team.projectpulse.rubric.repository.RubricRepository;
import team.projectpulse.rubric.service.RubricService;
import team.projectpulse.system.ApiException;
import team.projectpulse.system.StatusCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RubricServiceTest {
  @Mock
  private RubricRepository rubricRepository;

  @InjectMocks
  private RubricService rubricService;

  @Test
  void should_CreateRubric_When_RequestIsValid() {
    CreateRubricRequest request = rubricRequest("  Peer Eval Rubric v1  ",
        criterion("Productivity", "How productive is this teammate?", "10", 2),
        criterion("Quality of work", "How do you rate quality?", "10", 1));
    when(rubricRepository.existsByNameIgnoreCase("Peer Eval Rubric v1")).thenReturn(false);
    when(rubricRepository.save(any(Rubric.class))).thenAnswer(invocation -> invocation.getArgument(0));

    RubricResponse response = rubricService.create(request);

    assertThat(response.name()).isEqualTo("Peer Eval Rubric v1");
    assertThat(response.criteria()).extracting("name")
        .containsExactly("Quality of work", "Productivity");

    ArgumentCaptor<Rubric> rubricCaptor = ArgumentCaptor.forClass(Rubric.class);
    verify(rubricRepository).save(rubricCaptor.capture());
    Rubric savedRubric = rubricCaptor.getValue();
    assertThat(savedRubric.getName()).isEqualTo("Peer Eval Rubric v1");
    assertThat(savedRubric.getCriteria()).hasSize(2);
    assertThat(savedRubric.getCriteria().get(0).getRubric()).isSameAs(savedRubric);
  }

  @Test
  void should_AssignSequentialPositions_When_CriterionPositionIsMissingOrInvalid() {
    CreateRubricRequest request = rubricRequest("Peer Eval Rubric v1",
        criterion("Quality of work", "How do you rate quality?", "10", null),
        criterion("Productivity", "How productive is this teammate?", "10", 0));
    when(rubricRepository.existsByNameIgnoreCase("Peer Eval Rubric v1")).thenReturn(false);
    when(rubricRepository.save(any(Rubric.class))).thenAnswer(invocation -> invocation.getArgument(0));

    RubricResponse response = rubricService.create(request);

    assertThat(response.criteria()).extracting("position").containsExactly(1, 2);
  }

  @Test
  void should_RejectDuplicateRubricName_When_NameAlreadyExistsIgnoringCase() {
    CreateRubricRequest request = rubricRequest("Peer Eval Rubric v1",
        criterion("Quality of work", "How do you rate quality?", "10", 1));
    when(rubricRepository.existsByNameIgnoreCase("Peer Eval Rubric v1")).thenReturn(true);

    assertThatThrownBy(() -> rubricService.create(request))
        .isInstanceOf(ApiException.class)
        .hasMessage("Rubric name already exists")
        .extracting("code")
        .isEqualTo(StatusCode.CONFLICT);
    verify(rubricRepository, never()).save(any(Rubric.class));
  }

  @Test
  void should_ReturnRubricsSortedByName_When_FindingAll() {
    Rubric second = rubric("beta rubric");
    Rubric first = rubric("Alpha rubric");
    when(rubricRepository.findAll()).thenReturn(List.of(second, first));

    List<RubricResponse> responses = rubricService.findAll();

    assertThat(responses).extracting("name").containsExactly("Alpha rubric", "beta rubric");
  }

  @Test
  void should_ThrowNotFound_When_RubricDoesNotExist() {
    when(rubricRepository.findById(42L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> rubricService.findById(42L))
        .isInstanceOf(ApiException.class)
        .hasMessage("Rubric not found with id 42")
        .extracting("code")
        .isEqualTo(StatusCode.NOT_FOUND);
  }

  private CreateRubricRequest rubricRequest(
      String name,
      CreateRubricCriterionRequest... criteria) {
    CreateRubricRequest request = new CreateRubricRequest();
    request.setName(name);
    request.setCriteria(List.of(criteria));
    return request;
  }

  private CreateRubricCriterionRequest criterion(
      String name,
      String description,
      String maxScore,
      Integer position) {
    CreateRubricCriterionRequest request = new CreateRubricCriterionRequest();
    request.setName(name);
    request.setDescription(description);
    request.setMaxScore(new BigDecimal(maxScore));
    request.setPosition(position);
    return request;
  }

  private Rubric rubric(String name) {
    Rubric rubric = new Rubric();
    rubric.setName(name);
    rubric.addCriterion(criterionDomain("Quality of work", 1));
    return rubric;
  }

  private team.projectpulse.rubric.domain.RubricCriterion criterionDomain(
      String name,
      int position) {
    team.projectpulse.rubric.domain.RubricCriterion criterion =
        new team.projectpulse.rubric.domain.RubricCriterion();
    criterion.setName(name);
    criterion.setDescription("How do you rate quality?");
    criterion.setMaxScore(BigDecimal.TEN);
    criterion.setPosition(position);
    return criterion;
  }
}
