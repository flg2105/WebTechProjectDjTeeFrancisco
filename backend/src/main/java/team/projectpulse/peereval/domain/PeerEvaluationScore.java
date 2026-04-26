package team.projectpulse.peereval.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;

@Entity
@Table(
    name = "peer_evaluation_scores",
    uniqueConstraints = @UniqueConstraint(columnNames = {"entry_id", "rubric_criterion_id"}))
public class PeerEvaluationScore {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "entry_id", nullable = false)
  private PeerEvaluationEntry entry;

  @Column(name = "rubric_criterion_id", nullable = false)
  private Long rubricCriterionId;

  @Column(nullable = false, precision = 8, scale = 2)
  private BigDecimal score;

  public Long getId() {
    return id;
  }

  public PeerEvaluationEntry getEntry() {
    return entry;
  }

  public void setEntry(PeerEvaluationEntry entry) {
    this.entry = entry;
  }

  public Long getRubricCriterionId() {
    return rubricCriterionId;
  }

  public void setRubricCriterionId(Long rubricCriterionId) {
    this.rubricCriterionId = rubricCriterionId;
  }

  public BigDecimal getScore() {
    return score;
  }

  public void setScore(BigDecimal score) {
    this.score = score;
  }
}
