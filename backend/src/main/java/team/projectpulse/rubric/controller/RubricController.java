package team.projectpulse.rubric.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.projectpulse.rubric.dto.CreateRubricRequest;
import team.projectpulse.rubric.dto.RubricResponse;
import team.projectpulse.rubric.service.RubricService;
import team.projectpulse.system.Result;

@RestController
@RequestMapping("/api/rubrics")
public class RubricController {
  private final RubricService rubricService;

  public RubricController(RubricService rubricService) {
    this.rubricService = rubricService;
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public Result<RubricResponse> create(@Valid @RequestBody CreateRubricRequest request) {
    return Result.ok("Create Rubric Success", rubricService.create(request));
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public Result<List<RubricResponse>> findAll() {
    return Result.ok("Find Rubrics Success", rubricService.findAll());
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public Result<RubricResponse> findById(@PathVariable Long id) {
    return Result.ok("Find Rubric Success", rubricService.findById(id));
  }
}
