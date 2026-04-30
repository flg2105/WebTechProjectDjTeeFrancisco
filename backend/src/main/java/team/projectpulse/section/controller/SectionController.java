package team.projectpulse.section.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
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
import team.projectpulse.section.dto.SectionDetailsResponse;
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
  @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
  public Result<List<SectionResponse>> findAll(@RequestParam(required = false) String name) {
    return Result.ok("Find Sections Success", sectionService.findAll(name));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public Result<SectionDetailsResponse> findById(@PathVariable Long id) {
    return Result.ok("Find Section Success", sectionService.findById(id));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public Result<SectionResponse> create(@Valid @RequestBody SectionRequest request) {
    return Result.ok("Create Section Success", sectionService.create(request));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public Result<SectionResponse> update(@PathVariable Long id, @Valid @RequestBody SectionRequest request) {
    return Result.ok("Update Section Success", sectionService.update(id, request));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
  public Result<Void> delete(@PathVariable Long id) {
    sectionService.delete(id);
    return Result.ok("Delete Section Success", null);
  }

  @PutMapping("/{id}/active-weeks")
  @PreAuthorize("hasRole('ADMIN')")
  public Result<SectionResponse> replaceActiveWeeks(
      @PathVariable Long id,
      @Valid @RequestBody List<@Valid ActiveWeekRequest> requests) {
    return Result.ok("Update Active Weeks Success", sectionService.replaceActiveWeeks(id, requests));
  }

  @PostMapping("/{id}/instructors")
  @PreAuthorize("hasRole('ADMIN')")
  public Result<SectionResponse> assignInstructors(
      @PathVariable Long id,
      @Valid @RequestBody AssignSectionInstructorsRequest request) {
    return Result.ok("Assign Section Instructors Success", sectionService.assignInstructors(id, request));
  }

  @DeleteMapping("/{id}/instructors/{instructorUserId}")
  @PreAuthorize("hasRole('ADMIN')")
  public Result<SectionResponse> removeInstructor(@PathVariable Long id, @PathVariable Long instructorUserId) {
    return Result.ok("Remove Section Instructor Success", sectionService.removeInstructor(id, instructorUserId));
  }
}
