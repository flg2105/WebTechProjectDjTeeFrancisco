package team.projectpulse.section.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import team.projectpulse.section.domain.SectionInstructorAssignment;

public interface SectionInstructorAssignmentRepository extends JpaRepository<SectionInstructorAssignment, Long> {
  boolean existsBySectionIdAndInstructorUserId(Long sectionId, Long instructorUserId);

  List<SectionInstructorAssignment> findBySectionIdOrderByInstructorUserIdAsc(Long sectionId);

  void deleteBySectionId(Long sectionId);

  void deleteBySectionIdAndInstructorUserId(Long sectionId, Long instructorUserId);
}
