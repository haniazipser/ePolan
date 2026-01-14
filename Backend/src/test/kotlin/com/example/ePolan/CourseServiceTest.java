package com.example.ePolan;

import com.example.ePolan.Model.Dtos.CourseDto;
import com.example.ePolan.Model.Dtos.UserDto;
import com.example.ePolan.Model.Entities.Course;
import com.example.ePolan.Model.Entities.InvitationStatus;
import com.example.ePolan.Model.Entities.Participant;
import com.example.ePolan.Model.Entities.User;
import com.example.ePolan.Model.requests.NewCourseRequest;
import com.example.ePolan.Repositories.CourseRepository;
import com.example.ePolan.Repositories.LessonRepository;
import com.example.ePolan.Repositories.ParticipantRepository;
import com.example.ePolan.Services.CourseService;
import com.example.ePolan.Services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    CourseRepository courseRepository;
    @Mock
    ParticipantRepository participantRepository;
    @Mock
    LessonRepository lessonRepository;
    @Mock
    UserService userService;
    @Mock
    CourseGenerator courseGenerator;
    @Mock
    LessonGenerator lessonGenerator;

    @InjectMocks
    CourseService service;

    User loggedUser;
    UUID courseId;

    @Test
    void getUsersGroups_returnsCourseDtos() {
        loggedUser = new User();
        loggedUser.setId("user1");
        when(userService.getLoggedUser()).thenReturn(loggedUser);

        Course c1 = new Course();
        c1.setId(UUID.randomUUID());
        c1.setCreator(loggedUser);

        when(courseRepository.findDistinctByStudents_Student_IdAndStudents_InvitationStatus(
                loggedUser.getId(), InvitationStatus.ACCEPTED))
                .thenReturn(Set.of(c1));

        List<CourseDto> result = service.getUsersGroups();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(c1.getId());
    }

    @Test
    void createCourse_createsCourseAndParticipant() {
        loggedUser = new User();
        loggedUser.setId("user-1");
        when(userService.getLoggedUser()).thenReturn(loggedUser);

        NewCourseRequest req = new NewCourseRequest();
        Course course = new Course();
        course.setId(UUID.randomUUID());
        course.setCreator(loggedUser);

        when(courseGenerator.create(req, loggedUser)).thenReturn(course);
        when(courseRepository.save(course)).thenReturn(course);
        when(lessonGenerator.generateLessons(course)).thenReturn(Set.of());

        CourseDto result = service.createCourse(req);

        assertThat(result.getId()).isEqualTo(course.getId());
        verify(participantRepository).save(any(Participant.class));
    }

    @Test
    void addStudentToGroup_courseNotFound_throws404() {
        courseId = UUID.randomUUID();
        loggedUser = new User();
        when(userService.getLoggedUser()).thenReturn(loggedUser);
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addStudentToGroup("a@a.com", courseId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Course not found");
    }

    @Test
    void addStudentToGroup_userNotCreator_throws403() {
        courseId = UUID.randomUUID();
        Course course = mock(Course.class);
        loggedUser = new User();
        when(userService.getLoggedUser()).thenReturn(loggedUser);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(course.isStudentACreator(loggedUser)).thenReturn(false);

        assertThatThrownBy(() -> service.addStudentToGroup("a@a.com", courseId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    void addStudentToGroup_savesParticipant() {
        courseId = UUID.randomUUID();
        Course course = mock(Course.class);
        loggedUser = new User();
        when(userService.getLoggedUser()).thenReturn(loggedUser);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(course.isStudentACreator(loggedUser)).thenReturn(true);
        when(participantRepository.findByStudent_EmailAndCourse("s@a.com", course))
                .thenReturn(Optional.empty());
        User student = new User();
        when(userService.getUserByEmail("s@a.com")).thenReturn(student);

        service.addStudentToGroup("s@a.com", courseId);

        verify(participantRepository).save(any(Participant.class));
    }

    @Test
    void deleteStudentFromGroup_courseNotFound_throws404() {
        courseId = UUID.randomUUID();
        loggedUser = new User();
        when(userService.getLoggedUser()).thenReturn(loggedUser);
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteStudentFromGroup("user2", courseId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Group not found");
    }

    @Test
    void deleteStudentFromGroup_userNotMember_throws400() {//tu byl blad brak zaprzeczenia !
        courseId = UUID.randomUUID();
        Course course = mock(Course.class);
        loggedUser = new User();
        when(userService.getLoggedUser()).thenReturn(loggedUser);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(course.isStudentACreator(loggedUser)).thenReturn(true);
        when(participantRepository.findByStudent_IdAndCourse("user2", course))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteStudentFromGroup("user2", courseId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not a member");
    }

    @Test
    void deleteStudentFromGroup_userNotCreator_throws403() {
        courseId = UUID.randomUUID();
        Course course = mock(Course.class);
        loggedUser = new User();
        when(userService.getLoggedUser()).thenReturn(loggedUser);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(course.isStudentACreator(loggedUser)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteStudentFromGroup("user2", courseId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    void deleteStudentFromGroup_success() {

        courseId = UUID.randomUUID();
        String studentId = "student1";

        loggedUser = new User();
        loggedUser.setId("creator1");

        Course course = mock(Course.class);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userService.getLoggedUser()).thenReturn(loggedUser);
        when(course.isStudentACreator(loggedUser)).thenReturn(true);

        Participant participant = new Participant();
        User student = new User();
        student.setId(studentId);
        participant.setStudent(student);

        when(participantRepository.findByStudent_IdAndCourse(studentId, course))
                .thenReturn(Optional.of(participant));


        service.deleteStudentFromGroup(studentId, courseId);


        verify(participantRepository).delete(participant);
    }

    @Test
    void joinCourse_success() {

        String groupCode = "ABC123";
        loggedUser = new User();
        loggedUser.setId("user1");

        Course course = new Course();
        when(courseRepository.findByCourseCode(groupCode)).thenReturn(Optional.of(course));
        when(userService.getLoggedUser()).thenReturn(loggedUser);

        Participant participant = new Participant();
        participant.setStudent(loggedUser);
        participant.setInvitationStatus(InvitationStatus.WAITING);

        when(participantRepository.findByStudent_IdAndCourse(loggedUser.getId(), course))
                .thenReturn(Optional.of(participant));

        service.joinCourse(groupCode);

        assertThat(participant.getInvitationStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        verify(participantRepository).save(participant);
    }

    @Test
    void joinCourse_courseNotFound_throws404() {
        String groupCode = "XYZ123";
        loggedUser = new User();
        when(userService.getLoggedUser()).thenReturn(loggedUser);
        when(courseRepository.findByCourseCode(groupCode)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.joinCourse(groupCode))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Course not found");
    }

    @Test
    void joinCourse_userNotInvited_throws403() {
        String groupCode = "ABC123";
        loggedUser = new User();
        loggedUser.setId("user1");

        Course course = new Course();
        when(courseRepository.findByCourseCode(groupCode)).thenReturn(Optional.of(course));
        when(userService.getLoggedUser()).thenReturn(loggedUser);

        when(participantRepository.findByStudent_IdAndCourse(loggedUser.getId(), course))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.joinCourse(groupCode))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("You were not invited");
    }





    @Test
    void getStudentsInGroup_courseNotFound_throws404() {
        courseId = UUID.randomUUID();
        loggedUser = new User();
        when(userService.getLoggedUser()).thenReturn(loggedUser);
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getStudentsInGroup(courseId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Group not found");
    }

    @Test
    void getStudentsInGroup_userNotMember_throws403() {
        courseId = UUID.randomUUID();
        Course course = mock(Course.class);
        loggedUser = new User();
        when(userService.getLoggedUser()).thenReturn(loggedUser);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(course.isStudentAMemeber(loggedUser)).thenReturn(false);

        assertThatThrownBy(() -> service.getStudentsInGroup(courseId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not a member");
    }

    @Test
    void getStudentsInGroup_success() {
        courseId = UUID.randomUUID();
        Course course = mock(Course.class);
        loggedUser = new User();

        Participant participant = new Participant();
        participant.setStudent(loggedUser);
        participant.setInvitationStatus(InvitationStatus.ACCEPTED);

        when(userService.getLoggedUser()).thenReturn(loggedUser);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(course.isStudentAMemeber(loggedUser)).thenReturn(true);
        when(participantRepository.findByCourseAndInvitationStatus(course, InvitationStatus.ACCEPTED)).thenReturn(Set.of(participant));

        List<UserDto> students = service.getStudentsInGroup(courseId);

        assertThat(students).hasSize(1);
        assertThat(students.get(0).getId()).isEqualTo(loggedUser.getId());

    }

    @Test
    void archiveCourse_setsArchivedStatus() {
        courseId = UUID.randomUUID();
        loggedUser = new User();
        loggedUser.setId("user1");
        Participant participant = new Participant();
        participant.setStudent(loggedUser);
        Course course = new Course();
        course.setStudents(Set.of(participant));
        when(userService.getLoggedUser()).thenReturn(loggedUser);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        service.archiveCourse(courseId);

        assertThat(participant.getInvitationStatus()).isEqualTo(InvitationStatus.ARCHIVED);
        verify(participantRepository).save(participant);
    }

    @Test
    void unarchiveCourse_setsAcceptedStatus() {
        courseId = UUID.randomUUID();
        loggedUser = new User();
        loggedUser.setId("user1");
        Participant participant = new Participant();
        participant.setStudent(loggedUser);
        participant.setInvitationStatus(InvitationStatus.ARCHIVED);
        Course course = new Course();
        course.setStudents(Set.of(participant));
        when(userService.getLoggedUser()).thenReturn(loggedUser);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        service.unarchiveCourse(courseId);

        assertThat(participant.getInvitationStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        verify(participantRepository).save(participant);
    }

    @Test
    void getUsersArchivedGroups_success() {
        loggedUser = new User();
        loggedUser.setId("user1");
        when(userService.getLoggedUser()).thenReturn(loggedUser);

        Course c1 = new Course();
        c1.setId(UUID.randomUUID());
        Course c2 = new Course();
        c2.setId(UUID.randomUUID());
        c1.setCreator(loggedUser);
        c2.setCreator(loggedUser);

        when(courseRepository.findDistinctByStudents_Student_IdAndStudents_InvitationStatus(
                loggedUser.getId(),
                InvitationStatus.ARCHIVED
        )).thenReturn(Set.of(c1, c2));

        List<CourseDto> archived = service.getUsersArchivedGroups();

        assertThat(archived).hasSize(2);

    }

    @Test
    void getGroupInfo_success() {

        UUID groupId = UUID.randomUUID();
        Course course = new Course();
        course.setId(groupId);
        course.setCreator(new User());

        when(courseRepository.findById(groupId)).thenReturn(Optional.of(course));


        CourseDto result = service.getGroupInfo(groupId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(groupId);
    }


}
