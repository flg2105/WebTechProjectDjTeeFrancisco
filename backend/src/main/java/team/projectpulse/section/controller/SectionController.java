package team.projectpulse.section.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.projectpulse.section.dto.ActiveWeekRequest;
import team.projectpulse.section.dto.AssignSectionInstructorsRequest;
import team.projectpulse.section.dto.SectionRequest;
import team.projectpulse.section.dto.SectionResponse;
import team.projectpulse.section.service.SectionService;
import team.projectpulse.system.Result;

@RestController
@RequestMapping("/api/sections")
public class SectionController {
  private final SectionService sectionService;

  public SectionController(SectionService sectionService) {
    this.sectionService = sectionService;
  }

  @GetMapping
  public Result<List<SectionResponse>> findAll(@RequestParam(required = false) String name) {
    return Result.ok("Find Sections Success", sectionService.findAll(name));
  }

  @GetMapping("/{id}")
  public Result<SectionResponse> findById(@PathVariable Long id) {
    return Result.ok("Find Section Success", sectionService.findById(id));
  }

  @PostMapping
  public Result<SectionResponse> create(@Valid @RequestBody SectionRequest request) {
    return Result.ok("Create Section Success", sectionService.create(request));
  }

  @PutMapping("/{id}")
  public Result<SectionResponse> update(@PathVariable Long id, @Valid @RequestBody SectionRequest request) {
    return Result.ok("Update Section Success", sectionService.update(id, request));
  }

  @PutMapping("/{id}/active-weeks")
  public Result<SectionResponse> replaceActiveWeeks(
      @PathVariable Long id,
      @Valid @RequestBody List<@Valid ActiveWeekRequest> requests) {
    return Result.ok("Update Active Weeks Success", sectionService.replaceActiveWeeks(id, requests));
  }

  @PostMapping("/{id}/instructors")
  public Result<SectionResponse> assignInstructors(
      @PathVariable Long id,
      @Valid @RequestBody AssignSectionInstructorsRequest request) {
    return Result.ok("Assign Section Instructors Success", sectionService.assignInstructors(id, request));
  }

  @DeleteMapping("/{id}/instructors/{instructorUserId}")
  public Result<SectionResponse> removeInstructor(@PathVariable Long id, @PathVariable Long instructorUserId) {
    return Result.ok("Remove Section Instructor Success", sectionService.removeInstructor(id, instructorUserId));
  }
}
