package team.projectpulse.team.controller;

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
import team.projectpulse.system.Result;
import team.projectpulse.team.dto.AssignInstructorsRequest;
import team.projectpulse.team.dto.AssignStudentRequest;
import team.projectpulse.team.dto.TeamRequest;
import team.projectpulse.team.dto.TeamResponse;
import team.projectpulse.team.service.TeamService;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
  private final TeamService teamService;

  public TeamController(TeamService teamService) {
    this.teamService = teamService;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
  public Result<List<TeamResponse>> findAll(@RequestParam(required = false) Long sectionId) {
    return Result.ok("Find Teams Success", teamService.findAll(sectionId));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
  public Result<TeamResponse> findById(@PathVariable Long id) {
    return Result.ok("Find Team Success", teamService.findById(id));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public Result<TeamResponse> create(@Valid @RequestBody TeamRequest request) {
    return Result.ok("Create Team Success", teamService.create(request));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public Result<TeamResponse> update(@PathVariable Long id, @Valid @RequestBody TeamRequest request) {
    return Result.ok("Update Team Success", teamService.update(id, request));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public Result<Void> delete(@PathVariable Long id) {
    teamService.delete(id);
    return Result.ok("Delete Team Success", null);
  }

  @PostMapping("/{id}/students")
  @PreAuthorize("hasRole('ADMIN')")
  public Result<TeamResponse> assignStudent(@PathVariable Long id, @Valid @RequestBody AssignStudentRequest request) {
    return Result.ok("Assign Student Success", teamService.assignStudent(id, request));
  }

  @DeleteMapping("/{id}/students/{studentUserId}")
  @PreAuthorize("hasRole('ADMIN')")
  public Result<TeamResponse> removeStudent(@PathVariable Long id, @PathVariable Long studentUserId) {
    return Result.ok("Remove Student Success", teamService.removeStudent(id, studentUserId));
  }

  @PostMapping("/{id}/instructors")
  public Result<TeamResponse> assignInstructors(
      @PathVariable Long id,
      @Valid @RequestBody AssignInstructorsRequest request) {
    return Result.ok("Assign Instructors Success", teamService.assignInstructors(id, request));
  }

  @DeleteMapping("/{id}/instructors/{instructorUserId}")
  public Result<TeamResponse> removeInstructor(@PathVariable Long id, @PathVariable Long instructorUserId) {
    return Result.ok("Remove Instructor Success", teamService.removeInstructor(id, instructorUserId));
  }
}
