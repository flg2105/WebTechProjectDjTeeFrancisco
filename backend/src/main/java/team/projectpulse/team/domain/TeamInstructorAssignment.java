package team.projectpulse.team.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "team_instructor_assignments",
    uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "instructor_user_id"}))
public class TeamInstructorAssignment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "team_id", nullable = false)
  private Long teamId;

  @Column(name = "instructor_user_id", nullable = false)
  private Long instructorUserId;

  public Long getId() {
    return id;
  }

  public Long getTeamId() {
    return teamId;
  }

  public void setTeamId(Long teamId) {
    this.teamId = teamId;
  }

  public Long getInstructorUserId() {
    return instructorUserId;
  }

  public void setInstructorUserId(Long instructorUserId) {
    this.instructorUserId = instructorUserId;
  }
}

