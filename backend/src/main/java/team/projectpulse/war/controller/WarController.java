package team.projectpulse.war.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.projectpulse.system.Result;
import team.projectpulse.war.dto.WarActivityRequest;
import team.projectpulse.war.dto.WarEntryResponse;
import team.projectpulse.war.dto.WarStudentReportResponse;
import team.projectpulse.war.service.WarService;

@Validated
@RestController
@RequestMapping("/api/wars")
public class WarController {
  private final WarService warService;

  public WarController(WarService warService) {
    this.warService = warService;
  }

  @GetMapping
  public Result<WarEntryResponse> findWar(
      @RequestParam @Positive Long studentUserId,
      @RequestParam @Positive Long activeWeekId) {
    return Result.ok("Find WAR Success", warService.findWar(studentUserId, activeWeekId));
  }

  @GetMapping("/student-report")
  public Result<WarStudentReportResponse> findStudentReport(
      @RequestParam @Positive Long studentUserId,
      @RequestParam @Positive Long startActiveWeekId,
      @RequestParam @Positive Long endActiveWeekId) {
    return Result.ok(
        "Find Student WAR Report Success",
        warService.findStudentReport(studentUserId, startActiveWeekId, endActiveWeekId));
  }

  @PostMapping("/activities")
  public Result<WarEntryResponse> addActivity(@Valid @RequestBody WarActivityRequest request) {
    return Result.ok("Add WAR Activity Success", warService.addActivity(request));
  }

  @PutMapping("/activities/{activityId}")
  public Result<WarEntryResponse> updateActivity(
      @PathVariable Long activityId,
      @Valid @RequestBody WarActivityRequest request) {
    return Result.ok("Update WAR Activity Success", warService.updateActivity(activityId, request));
  }

  @DeleteMapping("/activities/{activityId}")
  public Result<WarEntryResponse> deleteActivity(
      @PathVariable Long activityId,
      @RequestParam @Positive Long studentUserId,
      @RequestParam @Positive Long activeWeekId) {
    return Result.ok(
        "Delete WAR Activity Success",
        warService.deleteActivity(activityId, studentUserId, activeWeekId));
  }
}
