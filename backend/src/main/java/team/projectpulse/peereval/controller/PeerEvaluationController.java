package team.projectpulse.peereval.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.projectpulse.peereval.dto.PeerEvaluationFormResponse;
import team.projectpulse.peereval.dto.PeerEvaluationReportResponse;
import team.projectpulse.peereval.dto.PeerEvaluationSectionReportResponse;
import team.projectpulse.peereval.dto.PeerEvaluationStudentReportResponse;
import team.projectpulse.peereval.dto.PeerEvaluationSubmissionResponse;
import team.projectpulse.peereval.dto.SubmitPeerEvaluationRequest;
import team.projectpulse.peereval.service.PeerEvaluationService;
import team.projectpulse.system.Result;

@RestController
@RequestMapping("/api/peer-evaluations")
public class PeerEvaluationController {
  private final PeerEvaluationService peerEvaluationService;

  public PeerEvaluationController(PeerEvaluationService peerEvaluationService) {
    this.peerEvaluationService = peerEvaluationService;
  }

  @GetMapping
  @PreAuthorize("hasRole('STUDENT')")
  public Result<PeerEvaluationFormResponse> findCurrent(@RequestParam Long studentUserId) {
    return Result.ok("Find Peer Evaluation Form Success", peerEvaluationService.findCurrent(studentUserId));
  }

  @PostMapping
  @PreAuthorize("hasRole('STUDENT')")
  public Result<PeerEvaluationSubmissionResponse> submit(@Valid @RequestBody SubmitPeerEvaluationRequest request) {
    return Result.ok("Submit Peer Evaluation Success", peerEvaluationService.submit(request));
  }

  @GetMapping("/me/report")
  @PreAuthorize("hasRole('STUDENT')")
  public Result<PeerEvaluationReportResponse> findOwnReport(
      @RequestParam Long studentUserId,
      @RequestParam(required = false) LocalDate weekStartDate) {
    return Result.ok("Find Peer Evaluation Report Success",
        peerEvaluationService.findOwnReport(studentUserId, weekStartDate));
  }

  @GetMapping("/section-report")
  @PreAuthorize("hasRole('INSTRUCTOR')")
  public Result<PeerEvaluationSectionReportResponse> findSectionReport(
      @RequestParam Long sectionId,
      @RequestParam LocalDate weekStartDate) {
    return Result.ok(
        "Find Section Peer Evaluation Report Success",
        peerEvaluationService.findSectionReport(sectionId, weekStartDate));
  }

  @GetMapping("/student-report")
  @PreAuthorize("hasRole('INSTRUCTOR')")
  public Result<PeerEvaluationStudentReportResponse> findStudentReport(
      @RequestParam Long studentUserId,
      @RequestParam Long startActiveWeekId,
      @RequestParam Long endActiveWeekId) {
    return Result.ok(
        "Find Student Peer Evaluation Report Success",
        peerEvaluationService.findStudentReport(studentUserId, startActiveWeekId, endActiveWeekId));
  }
}
