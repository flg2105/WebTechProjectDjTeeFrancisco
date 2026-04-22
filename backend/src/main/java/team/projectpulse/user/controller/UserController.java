package team.projectpulse.user.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.projectpulse.system.Result;
import team.projectpulse.user.domain.UserRole;
import team.projectpulse.user.dto.InvitationRequest;
import team.projectpulse.user.dto.InvitationResponse;
import team.projectpulse.user.dto.SetupAccountRequest;
import team.projectpulse.user.dto.UserResponse;
import team.projectpulse.user.service.UserService;

@RestController
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/api/users")
  public Result<List<UserResponse>> findAll(@RequestParam(required = false) UserRole role) {
    return Result.ok("Find Users Success", userService.findAll(role));
  }

  @PostMapping("/api/invitations/students")
  public Result<InvitationResponse> inviteStudents(@Valid @RequestBody InvitationRequest request) {
    return Result.ok("Invite Students Success", userService.inviteStudents(request));
  }

  @PostMapping("/api/invitations/instructors")
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
}
