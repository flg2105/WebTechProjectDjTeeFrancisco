package team.projectpulse.war.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "war_entries", uniqueConstraints = @UniqueConstraint(columnNames = {"active_week_id", "student_user_id"}))
public class WarEntry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "active_week_id", nullable = false)
  private Long activeWeekId;

  @Column(name = "team_id", nullable = false)
  private Long teamId;

  @Column(name = "student_user_id", nullable = false)
  private Long studentUserId;

  @Column(name = "submitted_at")
  private Instant submittedAt;

  @OneToMany(mappedBy = "warEntry", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("id ASC")
  private List<WarActivity> activities = new ArrayList<>();

  public Long getId() {
    return id;
  }

  public Long getActiveWeekId() {
    return activeWeekId;
  }

  public void setActiveWeekId(Long activeWeekId) {
    this.activeWeekId = activeWeekId;
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

  public Instant getSubmittedAt() {
    return submittedAt;
  }

  public void setSubmittedAt(Instant submittedAt) {
    this.submittedAt = submittedAt;
  }

  public List<WarActivity> getActivities() {
    return activities;
  }

  public void addActivity(WarActivity activity) {
    activities.add(activity);
    activity.setWarEntry(this);
  }

  public void removeActivity(WarActivity activity) {
    activities.remove(activity);
    activity.setWarEntry(null);
  }
}
