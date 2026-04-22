package team.projectpulse.team.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "team_memberships", uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "student_user_id"}))
public class TeamMembership {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "team_id", nullable = false)
  private Long teamId;

  @Column(name = "student_user_id", nullable = false)
  private Long studentUserId;

  public Long getId() {
    return id;
  }

  public Long getTeamId() {
    return teamId;
  }

  public void setTeamId(Long teamId) {
    this.teamId = teamId;
  }

  public Long getStudentUserId() {
    return studentUserId;
  }

  public void setStudentUserId(Long studentUserId) {
    this.studentUserId = studentUserId;
  }
}
