package team.projectpulse.war.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "war_activities")
public class WarActivity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "war_entry_id", nullable = false)
  private WarEntry warEntry;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private WarActivityCategory category;

  @Column(name = "activity_name", nullable = false)
  private String activity;

  @Column(nullable = false, length = 2000)
  private String description;

  @Column(name = "hours_planned", nullable = false, precision = 10, scale = 2)
  private BigDecimal hoursPlanned;

  @Column(name = "hours_actual", nullable = false, precision = 10, scale = 2)
  private BigDecimal hoursActual;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private WarActivityStatus status;

  public Long getId() {
    return id;
  }

  public WarEntry getWarEntry() {
    return warEntry;
  }

  public void setWarEntry(WarEntry warEntry) {
    this.warEntry = warEntry;
  }

  public WarActivityCategory getCategory() {
    return category;
  }

  public void setCategory(WarActivityCategory category) {
    this.category = category;
  }

  public String getActivity() {
    return activity;
  }

  public void setActivity(String activity) {
    this.activity = activity;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public BigDecimal getHoursPlanned() {
    return hoursPlanned;
  }

  public void setHoursPlanned(BigDecimal hoursPlanned) {
    this.hoursPlanned = hoursPlanned;
  }

  public BigDecimal getHoursActual() {
    return hoursActual;
  }

  public void setHoursActual(BigDecimal hoursActual) {
    this.hoursActual = hoursActual;
  }

  public WarActivityStatus getStatus() {
    return status;
  }

  public void setStatus(WarActivityStatus status) {
    this.status = status;
  }
}
