package team.projectpulse.system;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api")
public class HealthController {

  @GetMapping("/health")
  @PreAuthorize("hasRole('ADMIN')")
  public Result<String> health() {
    return Result.ok("OK", "ok");
  }
}
