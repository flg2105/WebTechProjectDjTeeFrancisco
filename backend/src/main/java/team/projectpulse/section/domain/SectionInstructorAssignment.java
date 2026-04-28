package team.projectpulse.section.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "section_instructor_assignments",
    uniqueConstraints = @UniqueConstraint(columnNames = {"section_id", "instructor_user_id"}))
public class SectionInstructorAssignment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "section_id", nullable = false)
  private Long sectionId;

  @Column(name = "instructor_user_id", nullable = false)
  private Long instructorUserId;

  public Long getId() {
    return id;
  }

  public Long getSectionId() {
    return sectionId;
  }

  public void setSectionId(Long sectionId) {
    this.sectionId = sectionId;
  }

  public Long getInstructorUserId() {
    return instructorUserId;
  }

  public void setInstructorUserId(Long instructorUserId) {
    this.instructorUserId = instructorUserId;
  }
}

