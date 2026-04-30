package team.projectpulse.war.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import team.projectpulse.war.domain.WarEntry;

public interface WarEntryRepository extends JpaRepository<WarEntry, Long> {
  Optional<WarEntry> findByActiveWeekIdAndStudentUserId(Long activeWeekId, Long studentUserId);

  List<WarEntry> findByStudentUserIdOrderByActiveWeekIdDesc(Long studentUserId);

  void deleteByActiveWeekIdIn(List<Long> activeWeekIds);
}
