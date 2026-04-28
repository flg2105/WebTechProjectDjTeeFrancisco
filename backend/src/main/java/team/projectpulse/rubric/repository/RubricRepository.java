package team.projectpulse.rubric.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import team.projectpulse.rubric.domain.Rubric;

public interface RubricRepository extends JpaRepository<Rubric, Long> {
  boolean existsByNameIgnoreCase(String name);

  Optional<Rubric> findByNameIgnoreCase(String name);

  Optional<Rubric> findById(Long id);
}
