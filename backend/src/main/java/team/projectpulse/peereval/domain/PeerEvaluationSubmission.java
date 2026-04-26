package team.projectpulse.peereval.domain;

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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "peer_evaluation_submissions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"evaluator_student_user_id", "week_start_date"}))
public class PeerEvaluationSubmission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "section_id", nullable = false)
  private Long sectionId;

  @Column(name = "team_id", nullable = false)
  private Long teamId;

  @Column(name = "evaluator_student_user_id", nullable = false)
  private Long evaluatorStudentUserId;

  @Column(name = "week_start_date", nullable = false)
  private LocalDate weekStartDate;

  @Column(name = "submitted_at", nullable = false, updatable = false)
  private Instant submittedAt = Instant.now();

  @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("evaluateeStudentUserId ASC")
  private List<PeerEvaluationEntry> entries = new ArrayList<>();

  public Long getId() {
    return id;
  }

  public Long getSectionId() {
    return sectionId;
  }

  public void setSectionId(Long sectionId) {
    this.sectionId = sectionId;
  }

  public Long getTeamId() {
    return teamId;
  }

  public void setTeamId(Long teamId) {
    this.teamId = teamId;
  }

  public Long getEvaluatorStudentUserId() {
    return evaluatorStudentUserId;
  }

  public void setEvaluatorStudentUserId(Long evaluatorStudentUserId) {
    this.evaluatorStudentUserId = evaluatorStudentUserId;
  }

  public LocalDate getWeekStartDate() {
    return weekStartDate;
  }

  public void setWeekStartDate(LocalDate weekStartDate) {
    this.weekStartDate = weekStartDate;
  }

  public Instant getSubmittedAt() {
    return submittedAt;
  }

  public List<PeerEvaluationEntry> getEntries() {
    return entries;
  }

  public void addEntry(PeerEvaluationEntry entry) {
    entry.setSubmission(this);
    entries.add(entry);
  }
}
