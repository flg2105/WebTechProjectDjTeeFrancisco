package team.projectpulse.section.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import team.projectpulse.section.domain.ActiveWeek;

public interface ActiveWeekRepository extends JpaRepository<ActiveWeek, Long> {
  List<ActiveWeek> findBySectionIdOrderByWeekStartDateAsc(Long sectionId);

  void deleteBySectionId(Long sectionId);
}
