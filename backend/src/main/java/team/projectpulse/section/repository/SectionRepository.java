package team.projectpulse.section.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import team.projectpulse.section.domain.Section;

public interface SectionRepository extends JpaRepository<Section, Long> {
  boolean existsByNameIgnoreCaseAndAcademicYearIgnoreCase(String name, String academicYear);

  List<Section> findByNameContainingIgnoreCaseOrderByNameDesc(String name);

  List<Section> findAllByOrderByNameDesc();
}
