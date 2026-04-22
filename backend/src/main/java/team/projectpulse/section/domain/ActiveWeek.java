package team.projectpulse.section.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;

@Entity
@Table(name = "active_weeks", uniqueConstraints = @UniqueConstraint(columnNames = {"section_id", "week_start_date"}))
public class ActiveWeek {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "section_id", nullable = false)
  private Long sectionId;

  @Column(name = "week_start_date", nullable = false)
  private LocalDate weekStartDate;

  @Column(nullable = false)
  private boolean active;

  public Long getId() {
    return id;
  }

  public Long getSectionId() {
    return sectionId;
  }

  public void setSectionId(Long sectionId) {
    this.sectionId = sectionId;
  }

  public LocalDate getWeekStartDate() {
    return weekStartDate;
  }

  public void setWeekStartDate(LocalDate weekStartDate) {
    this.weekStartDate = weekStartDate;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
