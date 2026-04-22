package team.projectpulse.team.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import team.projectpulse.team.domain.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {
  boolean existsBySectionIdAndNameIgnoreCase(Long sectionId, String name);

  List<Team> findBySectionIdOrderByNameAsc(Long sectionId);

  List<Team> findAllByOrderByNameAsc();
}
