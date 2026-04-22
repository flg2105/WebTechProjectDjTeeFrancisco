package team.projectpulse.rubric.service;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.projectpulse.rubric.domain.Rubric;
import team.projectpulse.rubric.domain.RubricCriterion;
import team.projectpulse.rubric.dto.CreateRubricCriterionRequest;
import team.projectpulse.rubric.dto.CreateRubricRequest;
import team.projectpulse.rubric.dto.RubricCriterionResponse;
import team.projectpulse.rubric.dto.RubricResponse;
import team.projectpulse.rubric.repository.RubricRepository;
import team.projectpulse.system.ApiException;
import team.projectpulse.system.NotFoundException;
import team.projectpulse.system.StatusCode;

@Service
public class RubricService {
  private final RubricRepository rubricRepository;

  public RubricService(RubricRepository rubricRepository) {
    this.rubricRepository = rubricRepository;
  }

  @Transactional
  public RubricResponse create(CreateRubricRequest request) {
    String rubricName = request.getName().trim();
    if (rubricRepository.existsByNameIgnoreCase(rubricName)) {
      throw new ApiException(StatusCode.CONFLICT, "Rubric name already exists");
    }

    Rubric rubric = new Rubric();
    rubric.setName(rubricName);

    for (int i = 0; i < request.getCriteria().size(); i++) {
      CreateRubricCriterionRequest criterionRequest = request.getCriteria().get(i);
      RubricCriterion criterion = new RubricCriterion();
      criterion.setName(criterionRequest.getName().trim());
      criterion.setDescription(criterionRequest.getDescription().trim());
      criterion.setMaxScore(criterionRequest.getMaxScore());
      criterion.setPosition(resolvePosition(criterionRequest, i));
      rubric.addCriterion(criterion);
    }

    return toResponse(rubricRepository.save(rubric));
  }

  @Transactional(readOnly = true)
  public List<RubricResponse> findAll() {
    return rubricRepository.findAll().stream()
        .sorted(Comparator.comparing(Rubric::getName, String.CASE_INSENSITIVE_ORDER))
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public RubricResponse findById(Long id) {
    Rubric rubric = rubricRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Rubric not found with id " + id));
    return toResponse(rubric);
  }

  private Integer resolvePosition(CreateRubricCriterionRequest request, int index) {
    if (request.getPosition() == null || request.getPosition() <= 0) {
      return index + 1;
    }
    return request.getPosition();
  }

  private RubricResponse toResponse(Rubric rubric) {
    List<RubricCriterionResponse> criteria = rubric.getCriteria().stream()
        .sorted(Comparator.comparing(RubricCriterion::getPosition))
        .map(criterion -> new RubricCriterionResponse(
            criterion.getId(),
            criterion.getName(),
            criterion.getDescription(),
            criterion.getMaxScore(),
            criterion.getPosition()))
        .toList();

    return new RubricResponse(
        rubric.getId(),
        rubric.getName(),
        rubric.getCreatedAt(),
        criteria);
  }
}
