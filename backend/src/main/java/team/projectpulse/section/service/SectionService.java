package team.projectpulse.section.service;

import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.projectpulse.rubric.repository.RubricRepository;
import team.projectpulse.section.domain.ActiveWeek;
import team.projectpulse.section.domain.Section;
import team.projectpulse.section.domain.SectionInstructorAssignment;
import team.projectpulse.section.dto.ActiveWeekRequest;
import team.projectpulse.section.dto.ActiveWeekResponse;
import team.projectpulse.section.dto.AssignSectionInstructorsRequest;
import team.projectpulse.section.dto.SectionRequest;
import team.projectpulse.section.dto.SectionResponse;
import team.projectpulse.section.repository.ActiveWeekRepository;
import team.projectpulse.section.repository.SectionInstructorAssignmentRepository;
import team.projectpulse.section.repository.SectionRepository;
import team.projectpulse.system.ApiException;
import team.projectpulse.system.StatusCode;
import team.projectpulse.user.domain.UserRole;
import team.projectpulse.user.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class SectionService {
  private final SectionRepository sectionRepository;
  private final ActiveWeekRepository activeWeekRepository;
  private final RubricRepository rubricRepository;
  private final SectionInstructorAssignmentRepository sectionInstructorAssignmentRepository;
  private final UserRepository userRepository;

  public SectionService(
      SectionRepository sectionRepository,
      ActiveWeekRepository activeWeekRepository,
      RubricRepository rubricRepository,
      SectionInstructorAssignmentRepository sectionInstructorAssignmentRepository,
      UserRepository userRepository) {
    this.sectionRepository = sectionRepository;
    this.activeWeekRepository = activeWeekRepository;
    this.rubricRepository = rubricRepository;
    this.sectionInstructorAssignmentRepository = sectionInstructorAssignmentRepository;
    this.userRepository = userRepository;
  }

  public List<SectionResponse> findAll(String name) {
    List<Section> sections = name == null || name.isBlank()
        ? sectionRepository.findAllByOrderByNameDesc()
        : sectionRepository.findByNameContainingIgnoreCaseOrderByNameDesc(name.trim());

    return sections.stream().map(this::toResponse).toList();
  }

  public SectionResponse findById(Long id) {
    return toResponse(getSection(id));
  }

  @Transactional
  public SectionResponse create(SectionRequest request) {
    validateDates(request);
    validateRubric(request.rubricId());

    String name = request.name().trim();
    String academicYear = request.academicYear().trim();
    if (sectionRepository.existsByNameIgnoreCaseAndAcademicYearIgnoreCase(name, academicYear)) {
      throw new ApiException(StatusCode.CONFLICT, "Section name already exists for academic year");
    }

    Section section = new Section();
    apply(section, request);
    return toResponse(sectionRepository.save(section));
  }

  @Transactional
  public SectionResponse update(Long id, SectionRequest request) {
    validateDates(request);
    validateRubric(request.rubricId());

    Section section = getSection(id);
    apply(section, request);
    section.touch();
    return toResponse(sectionRepository.save(section));
  }

  @Transactional
  public SectionResponse replaceActiveWeeks(Long sectionId, List<ActiveWeekRequest> requests) {
    Section section = getSection(sectionId);
    for (ActiveWeekRequest request : requests) {
      if (request.weekStartDate().getDayOfWeek() != DayOfWeek.MONDAY) {
        throw new ApiException(StatusCode.INVALID_ARGUMENT, "Active week start date must be a Monday");
      }
    }

    activeWeekRepository.deleteBySectionId(sectionId);
    List<ActiveWeek> activeWeeks = requests.stream()
        .sorted(Comparator.comparing(ActiveWeekRequest::weekStartDate))
        .map(request -> {
          ActiveWeek week = new ActiveWeek();
          week.setSectionId(sectionId);
          week.setWeekStartDate(request.weekStartDate());
          week.setActive(request.active());
          return week;
        })
        .toList();
    activeWeekRepository.saveAll(activeWeeks);
    section.touch();
    return toResponse(sectionRepository.save(section));
  }

  @Transactional
  public SectionResponse assignInstructors(Long sectionId, AssignSectionInstructorsRequest request) {
    Section section = getSection(sectionId);
    for (Long instructorUserId : request.instructorUserIds()) {
      validateInstructor(instructorUserId);
      if (!sectionInstructorAssignmentRepository.existsBySectionIdAndInstructorUserId(sectionId, instructorUserId)) {
        SectionInstructorAssignment assignment = new SectionInstructorAssignment();
        assignment.setSectionId(sectionId);
        assignment.setInstructorUserId(instructorUserId);
        sectionInstructorAssignmentRepository.save(assignment);
      }
    }
    section.touch();
    return toResponse(sectionRepository.save(section));
  }

  @Transactional
  public SectionResponse removeInstructor(Long sectionId, Long instructorUserId) {
    Section section = getSection(sectionId);
    sectionInstructorAssignmentRepository.deleteBySectionIdAndInstructorUserId(sectionId, instructorUserId);
    section.touch();
    return toResponse(sectionRepository.save(section));
  }

  private Section getSection(Long id) {
    return sectionRepository.findById(id)
        .orElseThrow(() -> new ApiException(StatusCode.NOT_FOUND, "Section not found with id " + id));
  }

  private void apply(Section section, SectionRequest request) {
    section.setName(request.name().trim());
    section.setAcademicYear(request.academicYear().trim());
    section.setStartDate(request.startDate());
    section.setEndDate(request.endDate());
    section.setRubricId(request.rubricId());
  }

  private void validateDates(SectionRequest request) {
    if (request.endDate().isBefore(request.startDate())) {
      throw new ApiException(StatusCode.INVALID_ARGUMENT, "Section end date must be on or after start date");
    }
  }

  private void validateRubric(Long rubricId) {
    if (!rubricRepository.existsById(rubricId)) {
      throw new ApiException(StatusCode.NOT_FOUND, "Rubric not found with id " + rubricId);
    }
  }

  private void validateInstructor(Long instructorUserId) {
    boolean validInstructor = userRepository.findById(instructorUserId)
        .map(user -> user.getRole() == UserRole.INSTRUCTOR)
        .orElse(false);
    if (!validInstructor) {
      throw new ApiException(StatusCode.NOT_FOUND, "Instructor not found with id " + instructorUserId);
    }
  }

  private SectionResponse toResponse(Section section) {
    List<ActiveWeekResponse> activeWeeks = activeWeekRepository.findBySectionIdOrderByWeekStartDateAsc(section.getId())
        .stream()
        .map(week -> new ActiveWeekResponse(week.getId(), week.getWeekStartDate(), week.isActive()))
        .toList();

    List<Long> instructorUserIds = sectionInstructorAssignmentRepository
        .findBySectionIdOrderByInstructorUserIdAsc(section.getId())
        .stream()
        .map(SectionInstructorAssignment::getInstructorUserId)
        .toList();

    return new SectionResponse(
        section.getId(),
        section.getName(),
        section.getAcademicYear(),
        section.getStartDate(),
        section.getEndDate(),
        section.getRubricId(),
        section.getCreatedAt(),
        section.getUpdatedAt(),
        activeWeeks,
        instructorUserIds);
  }
}
