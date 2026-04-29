package team.projectpulse.user.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.projectpulse.system.Result;
import team.projectpulse.user.domain.UserRole;
import team.projectpulse.user.domain.UserStatus;
import team.projectpulse.user.dto.DeactivateInstructorRequest;
import team.projectpulse.user.dto.EditAccountRequest;
import team.projectpulse.user.dto.InstructorDetailsResponse;
import team.projectpulse.user.dto.InstructorSearchResultResponse;
import team.projectpulse.user.dto.InvitationRequest;
import team.projectpulse.user.dto.InvitationResponse;
import team.projectpulse.user.dto.SetupAccountRequest;
import team.projectpulse.user.dto.StudentDetailsResponse;
import team.projectpulse.user.dto.StudentSearchResultResponse;
import team.projectpulse.user.dto.UserResponse;
import team.projectpulse.user.service.UserService;

@RestController
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/api/users")
  @PreAuthorize("hasRole('ADMIN')")
  public Result<List<UserResponse>> findAll(@RequestParam(required = false) UserRole role) {
    return Result.ok("Find Users Success", userService.findAll(role));
  }

  @PutMapping("/api/users/{id}")
  @PreAuthorize("@currentUserSecurity.canEditAccount(#id)")
  public Result<UserResponse> editAccount(
      @PathVariable Long id,
      @Valid @RequestBody EditAccountRequest request) {
    return Result.ok("Edit Account Success", userService.editAccount(id, request));
  }

  @PostMapping("/api/invitations/students")
  @PreAuthorize("hasRole('ADMIN')")
  public Result<InvitationResponse> inviteStudents(@Valid @RequestBody InvitationRequest request) {
    return Result.ok("Invite Students Success", userService.inviteStudents(request));
  }

  @PostMapping("/api/invitations/instructors")
  @PreAuthorize("hasRole('ADMIN')")
  public Result<InvitationResponse> inviteInstructors(@Valid @RequestBody InvitationRequest request) {
    return Result.ok("Invite Instructors Success", userService.inviteInstructors(request));
  }

  @PostMapping("/api/users/student-setup")
  public Result<UserResponse> setupStudent(@Valid @RequestBody SetupAccountRequest request) {
    return Result.ok("Setup Student Success", userService.setupStudent(request));
  }

  @PostMapping("/api/users/instructor-setup")
  public Result<UserResponse> setupInstructor(@Valid @RequestBody SetupAccountRequest request) {
    return Result.ok("Setup Instructor Success", userService.setupInstructor(request));
  }

  @GetMapping("/api/students")
  @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
  public Result<List<StudentSearchResultResponse>> findStudents(
      @RequestParam(required = false) String q) {
    return Result.ok("Find Students Success", userService.findStudents(q));
  }

  @GetMapping("/api/students/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
  public Result<StudentDetailsResponse> viewStudent(@PathVariable Long id) {
    return Result.ok("View Student Success", userService.viewStudent(id));
  }

  @GetMapping("/api/instructors")
  @PreAuthorize("hasRole('ADMIN')")
  public Result<List<InstructorSearchResultResponse>> findInstructors(
      @RequestParam(required = false) String firstName,
      @RequestParam(required = false) String lastName,
      @RequestParam(required = false) String teamName,
      @RequestParam(required = false) UserStatus status) {
    return Result.ok("Find Instructors Success", userService.findInstructors(firstName, lastName, teamName, status));
  }

  @GetMapping("/api/instructors/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public Result<InstructorDetailsResponse> viewInstructor(@PathVariable Long id) {
    return Result.ok("View Instructor Success", userService.viewInstructor(id));
  }

  @PostMapping("/api/instructors/{id}/deactivate")
  @PreAuthorize("hasRole('ADMIN')")
  public Result<UserResponse> deactivateInstructor(
      @PathVariable Long id,
      @Valid @RequestBody DeactivateInstructorRequest request) {
    return Result.ok("Deactivate Instructor Success", userService.deactivateInstructor(id, request));
  }

  @PostMapping("/api/instructors/{id}/reactivate")
  @PreAuthorize("hasRole('ADMIN')")
  public Result<UserResponse> reactivateInstructor(@PathVariable Long id) {
    return Result.ok("Reactivate Instructor Success", userService.reactivateInstructor(id));
  }

  @DeleteMapping("/api/students/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public Result<Void> deleteStudent(@PathVariable Long id) {
    boolean deleted = userService.deleteStudent(id);
    String message = deleted ? "Delete Student Success" : "Deactivate Student Success";
    return Result.ok(message, null);
  }
}
