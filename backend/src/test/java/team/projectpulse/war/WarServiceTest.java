package team.projectpulse.war;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.projectpulse.auth.service.CurrentUserSecurity;
import team.projectpulse.section.domain.ActiveWeek;
import team.projectpulse.section.repository.ActiveWeekRepository;
import team.projectpulse.system.ApiException;
import team.projectpulse.system.StatusCode;
import team.projectpulse.team.domain.Team;
import team.projectpulse.team.domain.TeamMembership;
import team.projectpulse.team.repository.TeamMembershipRepository;
import team.projectpulse.team.repository.TeamRepository;
import team.projectpulse.user.domain.ProjectUser;
import team.projectpulse.user.domain.UserRole;
import team.projectpulse.user.repository.UserRepository;
import team.projectpulse.war.domain.WarActivityCategory;
import team.projectpulse.war.domain.WarActivityStatus;
import team.projectpulse.war.domain.WarEntry;
import team.projectpulse.war.dto.WarActivityRequest;
import team.projectpulse.war.dto.WarEntryResponse;
import team.projectpulse.war.repository.WarEntryRepository;
import team.projectpulse.war.service.WarService;

@ExtendWith(MockitoExtension.class)
class WarServiceTest {
  @Mock
  private WarEntryRepository warEntryRepository;
  @Mock
  private ActiveWeekRepository activeWeekRepository;
  @Mock
  private TeamRepository teamRepository;
  @Mock
  private TeamMembershipRepository teamMembershipRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private CurrentUserSecurity currentUserSecurity;

  @InjectMocks
  private WarService warService;

  @Test
  void should_AddActivity_ForEligibleWeek() {
    Long studentUserId = 10L;
    Long activeWeekId = 20L;
    ActiveWeek activeWeek = activeWeek(activeWeekId, 30L, LocalDate.now().minusWeeks(1), true);
    ProjectUser student = student(studentUserId);
    TeamMembership membership = membership(40L, studentUserId);
    Team team = team(40L, 30L, "Pulse Team");

    when(userRepository.findById(studentUserId)).thenReturn(Optional.of(student));
    org.mockito.Mockito.doNothing().when(currentUserSecurity).requireCurrentUser(studentUserId);
    when(activeWeekRepository.findById(activeWeekId)).thenReturn(Optional.of(activeWeek));
    when(teamMembershipRepository.findByStudentUserIdOrderByTeamIdAsc(studentUserId)).thenReturn(List.of(membership));
    when(teamRepository.findAllById(List.of(40L))).thenReturn(List.of(team));
    when(warEntryRepository.findByActiveWeekIdAndStudentUserId(activeWeekId, studentUserId)).thenReturn(Optional.empty());
    when(warEntryRepository.save(any(WarEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

    WarEntryResponse response = warService.addActivity(request(studentUserId, activeWeekId));

    assertThat(response.activeWeekId()).isEqualTo(activeWeekId);
    assertThat(response.teamId()).isEqualTo(40L);
    assertThat(response.activities()).hasSize(1);
    assertThat(response.activities().get(0).activity()).isEqualTo("Implement WAR endpoint");

    ArgumentCaptor<WarEntry> entryCaptor = ArgumentCaptor.forClass(WarEntry.class);
    org.mockito.Mockito.verify(warEntryRepository).save(entryCaptor.capture());
    WarEntry savedEntry = entryCaptor.getValue();
    assertThat(savedEntry.getStudentUserId()).isEqualTo(studentUserId);
    assertThat(savedEntry.getTeamId()).isEqualTo(40L);
    assertThat(savedEntry.getActivities()).hasSize(1);
  }

  @Test
  void should_RejectFutureWeek_When_AddingActivity() {
    Long studentUserId = 10L;
    Long activeWeekId = 20L;
    ActiveWeek activeWeek = activeWeek(activeWeekId, 30L, LocalDate.now().plusWeeks(1), true);

    when(userRepository.findById(studentUserId)).thenReturn(Optional.of(student(studentUserId)));
    org.mockito.Mockito.doNothing().when(currentUserSecurity).requireCurrentUser(studentUserId);
    when(activeWeekRepository.findById(activeWeekId)).thenReturn(Optional.of(activeWeek));

    assertThatThrownBy(() -> warService.addActivity(request(studentUserId, activeWeekId)))
        .isInstanceOf(ApiException.class)
        .hasMessage("Selected week cannot be in the future")
        .extracting("code")
        .isEqualTo(StatusCode.INVALID_ARGUMENT);
  }

  @Test
  void should_RejectInactiveWeek_When_FindingWar() {
    Long studentUserId = 10L;
    Long activeWeekId = 20L;
    ActiveWeek activeWeek = activeWeek(activeWeekId, 30L, LocalDate.now().minusWeeks(1), false);

    when(userRepository.findById(studentUserId)).thenReturn(Optional.of(student(studentUserId)));
    org.mockito.Mockito.doNothing().when(currentUserSecurity).requireCurrentUser(studentUserId);
    when(activeWeekRepository.findById(activeWeekId)).thenReturn(Optional.of(activeWeek));

    assertThatThrownBy(() -> warService.findWar(studentUserId, activeWeekId))
        .isInstanceOf(ApiException.class)
        .hasMessage("Selected week is inactive")
        .extracting("code")
        .isEqualTo(StatusCode.INVALID_ARGUMENT);
  }

  private WarActivityRequest request(Long studentUserId, Long activeWeekId) {
    return new WarActivityRequest(
        studentUserId,
        activeWeekId,
        WarActivityCategory.DEVELOPMENT,
        "  Implement WAR endpoint  ",
        "  Added the UC-27 controller and service.  ",
        new BigDecimal("3.5"),
        new BigDecimal("4.0"),
        WarActivityStatus.IN_PROGRESS);
  }

  private ActiveWeek activeWeek(Long id, Long sectionId, LocalDate weekStartDate, boolean active) {
    ActiveWeek week = new ActiveWeek();
    trySetId(week, id);
    week.setSectionId(sectionId);
    week.setWeekStartDate(weekStartDate);
    week.setActive(active);
    return week;
  }

  private TeamMembership membership(Long teamId, Long studentUserId) {
    TeamMembership membership = new TeamMembership();
    membership.setTeamId(teamId);
    membership.setStudentUserId(studentUserId);
    return membership;
  }

  private Team team(Long id, Long sectionId, String name) {
    Team team = new Team();
    trySetId(team, id);
    team.setSectionId(sectionId);
    team.setName(name);
    return team;
  }

  private ProjectUser student(Long id) {
    ProjectUser user = new ProjectUser();
    trySetId(user, id);
    user.setEmail("student@example.edu");
    user.setDisplayName("Student");
    user.setRole(UserRole.STUDENT);
    return user;
  }

  private void trySetId(Object target, Long id) {
    try {
      java.lang.reflect.Field field = target.getClass().getDeclaredField("id");
      field.setAccessible(true);
      field.set(target, id);
    } catch (ReflectiveOperationException ex) {
      throw new IllegalStateException(ex);
    }
  }
}
