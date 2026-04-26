package team.projectpulse.peereval.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "peer_evaluation_entries",
    uniqueConstraints = @UniqueConstraint(columnNames = {"submission_id", "evaluatee_student_user_id"}))
public class PeerEvaluationEntry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "submission_id", nullable = false)
  private PeerEvaluationSubmission submission;

  @Column(name = "evaluatee_student_user_id", nullable = false)
  private Long evaluateeStudentUserId;

  @Column(name = "public_comment", length = 2000)
  private String publicComment;

  @Column(name = "private_comment", length = 2000)
  private String privateComment;

  @OneToMany(mappedBy = "entry", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("rubricCriterionId ASC")
  private List<PeerEvaluationScore> scores = new ArrayList<>();

  public Long getId() {
    return id;
  }

  public PeerEvaluationSubmission getSubmission() {
    return submission;
  }

  public void setSubmission(PeerEvaluationSubmission submission) {
    this.submission = submission;
  }

  public Long getEvaluateeStudentUserId() {
    return evaluateeStudentUserId;
  }

  public void setEvaluateeStudentUserId(Long evaluateeStudentUserId) {
    this.evaluateeStudentUserId = evaluateeStudentUserId;
  }

  public String getPublicComment() {
    return publicComment;
  }

  public void setPublicComment(String publicComment) {
    this.publicComment = publicComment;
  }

  public String getPrivateComment() {
    return privateComment;
  }

  public void setPrivateComment(String privateComment) {
    this.privateComment = privateComment;
  }

  public List<PeerEvaluationScore> getScores() {
    return scores;
  }

  public void addScore(PeerEvaluationScore score) {
    score.setEntry(this);
    scores.add(score);
  }
}
