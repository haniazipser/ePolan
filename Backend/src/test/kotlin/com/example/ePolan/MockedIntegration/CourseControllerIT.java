package com.example.ePolan.MockedIntegration;

import com.example.ePolan.Model.Entities.Course;
import com.example.ePolan.Model.Entities.InvitationStatus;
import com.example.ePolan.Model.Entities.Participant;
import com.example.ePolan.Model.Entities.User;
import com.example.ePolan.Repositories.CourseRepository;
import com.example.ePolan.Repositories.ParticipantRepository;
import com.example.ePolan.Repositories.UserRepository;
import com.example.ePolan.Services.UserService;
import com.example.ePolan.CourseGenerator;
import com.example.ePolan.LessonGenerator;
import com.example.ePolan.Utils.TestCourses;
import com.example.ePolan.Utils.TestUsers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("null")
class CourseControllerIT {

    @MockBean
    CourseRepository courseRepository;

    @MockBean
    UserRepository userRepository;

    @MockBean
    ParticipantRepository participantRepository;

    @MockBean
    UserService userService;

    @MockBean
    CourseGenerator courseGenerator;

    @MockBean
    LessonGenerator lessonGenerator;

    @Autowired
    MockMvc mockMvc;

    @Test
    void getStudentGroups_returnsCourses() throws Exception {

        User user = TestUsers.user("test-user");
        User creator = TestUsers.user("test-creator");
        Course course = TestCourses.course(UUID.randomUUID(),"Algebra", creator);
        course.setInstructor("Cooke");

        when(userService.getLoggedUser()).thenReturn(user);

        when(courseRepository.findDistinctByStudents_Student_IdAndStudents_InvitationStatus("test-user", InvitationStatus.ACCEPTED))
                .thenReturn(Set.of(course));

        mockMvc.perform(get("/course")
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Algebra"))
                .andExpect(jsonPath("$[0].instructor").value("Cooke"));
    }

    @Test
    void getStudentGroups_returnsEmpty_whenNoCourses() throws Exception {
        User student = TestUsers.user("test-user");

        when(userService.getLoggedUser()).thenReturn(student);

        when(courseRepository.findDistinctByStudents_Student_IdAndStudents_InvitationStatus(
                "test-user", InvitationStatus.ACCEPTED))
                .thenReturn(Set.of());

        mockMvc.perform(get("/course")
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getStudentGroups_returnsMultipleCourses() throws Exception {
        User student = TestUsers.user("test-user");
        User creator1 = TestUsers.user("creator-1");
        User creator2 = TestUsers.user("creator-2");

        Course course1 = TestCourses.course(UUID.randomUUID(),"Algebra", creator1);
        Course course2 = TestCourses.course(UUID.randomUUID(),"Geometry", creator2);

        when(userService.getLoggedUser()).thenReturn(student);

        when(courseRepository.findDistinctByStudents_Student_IdAndStudents_InvitationStatus(
                "test-user", InvitationStatus.ACCEPTED))
                .thenReturn(Set.of(course1, course2));

        mockMvc.perform(get("/course")
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].name").value(hasItem("Algebra")))
                .andExpect(jsonPath("$[*].name").value(hasItem("Geometry")));
    }

    @Test
    void getStudentsInGroup_returnsStudents() throws Exception {
        UUID courseId = UUID.randomUUID();

        User creator = TestUsers.user("creator");
        User student1 = TestUsers.user("s1");
        User student2 = TestUsers.user("s2");
        Course course = TestCourses.course(courseId, "Algebra", creator);

        Participant creatorParticipant = new Participant();
        creatorParticipant.setStudent(creator);
        creatorParticipant.setCourse(course);
        creatorParticipant.setInvitationStatus(InvitationStatus.ACCEPTED);

        Participant participant1 = new Participant();
        participant1.setStudent(student1);
        participant1.setCourse(course);
        participant1.setInvitationStatus(InvitationStatus.ACCEPTED);

        Participant participant2 = new Participant();
        participant2.setStudent(student2);
        participant2.setCourse(course);
        participant2.setInvitationStatus(InvitationStatus.ACCEPTED);

        course.setStudents(new HashSet<>(Set.of(creatorParticipant, participant1, participant2)));

        when(userService.getLoggedUser()).thenReturn(creator);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(participantRepository.findByCourseAndInvitationStatus(course, InvitationStatus.ACCEPTED))
                .thenReturn(Set.of(participant1, participant2));

        mockMvc.perform(get("/course/{courseId}/students", courseId)
                        .with(jwt().jwt(jwt -> jwt.subject("creator"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].id").value(hasItem("s1")))
                .andExpect(jsonPath("$[*].id").value(hasItem("s2")));
    }

    @Test
    void createCourse_createsCourse() throws Exception {
        User creator = TestUsers.user("creator");
        Course newCourse = TestCourses.course(UUID.randomUUID(), "Physics", creator);

        when(userService.getLoggedUser()).thenReturn(creator);
        when(courseGenerator.create(any(), any())).thenReturn(newCourse);
        when(courseRepository.save(any())).thenReturn(newCourse);
        when(lessonGenerator.generateLessons(any())).thenReturn(new HashSet<>());

        mockMvc.perform(post("/course")
                        .with(jwt().jwt(jwt -> jwt.subject("creator")))
                        .contentType("application/json")
                        .content("""
                        {
                          "name": "Physics"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Physics"));
    }

    @Test
    void joinCourse_joinsSuccessfully() throws Exception {
        User student = TestUsers.user("student");
        Course course = TestCourses.course(UUID.randomUUID(), "Math", TestUsers.user("creator"));
        course.setCourseCode("JOIN123");

        Participant participant = new Participant();
        participant.setStudent(student);
        participant.setCourse(course);
        participant.setInvitationStatus(InvitationStatus.WAITING);

        when(userService.getLoggedUser()).thenReturn(student);
        when(courseRepository.findByCourseCode("JOIN123")).thenReturn(Optional.of(course));
        when(participantRepository.findByStudent_IdAndCourse(student.getId(), course))
                .thenReturn(Optional.of(participant));

        mockMvc.perform(post("/course/join")
                        .with(jwt().jwt(jwt -> jwt.subject("student")))
                        .contentType("application/json")
                        .content("""
                        {
                          "groupCode": "JOIN123"
                        }
                        """))
                .andExpect(status().isOk());
    }

    @Test
    void inviteStudent_sendsInvitation() throws Exception {
        UUID courseId = UUID.randomUUID();
        User creator = TestUsers.user("creator");
        User student = TestUsers.user("student@test.com");
        Course course = TestCourses.course(courseId, "Math", creator);

        when(userService.getLoggedUser()).thenReturn(creator);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(participantRepository.findByStudent_EmailAndCourse("student@test.com", course))
                .thenReturn(Optional.empty());
        when(userService.getUserByEmail("student@test.com")).thenReturn(student);

        mockMvc.perform(post("/course/{courseId}/invitations", courseId)
                        .with(jwt().jwt(jwt -> jwt.subject("creator")))
                        .contentType("application/json")
                        .content("""
                        {
                          "email": "student@test.com"
                        }
                        """))
                .andExpect(status().isOk());
    }

    @Test
    void deleteStudentFromGroup_removesStudent() throws Exception {
        UUID courseId = UUID.randomUUID();

        User creator = TestUsers.user("creator");
        User student = TestUsers.user("student");
        Course course = TestCourses.course(courseId, "Math", creator);

        Participant creatorParticipant = new Participant();
        creatorParticipant.setStudent(creator);
        creatorParticipant.setCourse(course);
        creatorParticipant.setInvitationStatus(InvitationStatus.ACCEPTED);

        Participant participant = new Participant();
        participant.setStudent(student);
        participant.setCourse(course);
        participant.setInvitationStatus(InvitationStatus.ACCEPTED);

        course.setStudents(new HashSet<>(Set.of(creatorParticipant, participant)));

        when(userService.getLoggedUser()).thenReturn(creator);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(participantRepository.findByStudent_IdAndCourse("student", course))
                .thenReturn(Optional.of(participant));

        mockMvc.perform(delete("/course/{courseId}/students/{userId}", courseId, "student")
                        .with(jwt().jwt(jwt -> jwt.subject("creator"))))
                .andExpect(status().isOk());

        verify(courseRepository).findById(courseId);
    }

    @Test
    void archiveCourse_archivesCourse() throws Exception {
        UUID courseId = UUID.randomUUID();
        User creator = TestUsers.user("creator");
        Course course = TestCourses.course(courseId, "Math", creator);

        Participant participant = new Participant();
        participant.setStudent(creator);
        participant.setCourse(course);
        participant.setInvitationStatus(InvitationStatus.ACCEPTED);
        course.setStudents(new HashSet<>(Set.of(participant)));

        when(userService.getLoggedUser()).thenReturn(creator);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        mockMvc.perform(delete("/course/{courseId}", courseId)
                        .with(jwt().jwt(jwt -> jwt.subject("creator"))))
                .andExpect(status().isOk());

        verify(courseRepository).findById(courseId);
    }

    @Test
    void getStudentArchivedGroups_returnsArchivedCourses() throws Exception {
        User creator = TestUsers.user("creator");
        User student = TestUsers.user("test-user");
        Course archivedCourse = TestCourses.course(UUID.randomUUID(), "Old Course", creator);

        when(userService.getLoggedUser()).thenReturn(student);

        when(courseRepository.findDistinctByStudents_Student_IdAndStudents_InvitationStatus(
                "test-user", InvitationStatus.ARCHIVED))
                .thenReturn(Set.of(archivedCourse));

        mockMvc.perform(get("/course/courses/archived")
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Old Course"));
    }

    @Test
    void getStudentArchivedGroups_returnsEmptyWhenNoArchived() throws Exception {
        User student = TestUsers.user("test-user");

        when(userService.getLoggedUser()).thenReturn(student);

        when(courseRepository.findDistinctByStudents_Student_IdAndStudents_InvitationStatus(
                "test-user", InvitationStatus.ARCHIVED))
                .thenReturn(Set.of());

        mockMvc.perform(get("/course/courses/archived")
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void unarchiveCourse_restoresCourse() throws Exception {
        UUID courseId = UUID.randomUUID();
        User creator = TestUsers.user("creator");
        Course course = TestCourses.course(courseId, "Math", creator);

        Participant participant = new Participant();
        participant.setStudent(creator);
        participant.setCourse(course);
        participant.setInvitationStatus(InvitationStatus.ARCHIVED);
        course.setStudents(new HashSet<>(Set.of(participant)));

        when(userService.getLoggedUser()).thenReturn(creator);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        mockMvc.perform(put("/course/{courseId}/restore", courseId)
                        .with(jwt().jwt(jwt -> jwt.subject("creator"))))
                .andExpect(status().isOk());

        verify(courseRepository).findById(courseId);
    }

    @Test
    void getStudentsInGroup_returnsEmptyWhenNoneEnrolled() throws Exception {
        UUID courseId = UUID.randomUUID();
        User creator = TestUsers.user("creator");
        Course course = TestCourses.course(courseId, "Empty Course", creator);

        Participant creatorParticipant = new Participant();
        creatorParticipant.setStudent(creator);
        creatorParticipant.setCourse(course);
        creatorParticipant.setInvitationStatus(InvitationStatus.ACCEPTED);

        course.setStudents(new HashSet<>(Set.of(creatorParticipant)));

        when(userService.getLoggedUser()).thenReturn(creator);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(participantRepository.findByCourseAndInvitationStatus(course, InvitationStatus.ACCEPTED))
                .thenReturn(Set.of());

        mockMvc.perform(get("/course/{courseId}/students", courseId)
                        .with(jwt().jwt(jwt -> jwt.subject("creator"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createCourse_createsMultipleCourses() throws Exception {
        User creator = TestUsers.user("creator");

        Course course1 = TestCourses.course(UUID.randomUUID(), "Chemistry", creator);
        Course course2 = TestCourses.course(UUID.randomUUID(), "Biology", creator);

        when(userService.getLoggedUser()).thenReturn(creator);
        when(courseGenerator.create(any(), any())).thenReturn(course1).thenReturn(course2);
        when(courseRepository.save(any())).thenReturn(course1).thenReturn(course2);
        when(lessonGenerator.generateLessons(any())).thenReturn(new HashSet<>());

        // Create first course
        mockMvc.perform(post("/course")
                        .with(jwt().jwt(jwt -> jwt.subject("creator")))
                        .contentType("application/json")
                        .content("""
                        {
                          "name": "Chemistry"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Chemistry"));

        // Create second course
        mockMvc.perform(post("/course")
                        .with(jwt().jwt(jwt -> jwt.subject("creator")))
                        .contentType("application/json")
                        .content("""
                        {
                          "name": "Biology"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Biology"));

        verify(courseRepository, times(2)).save(any());
    }

    @Test
    void joinCourse_withInvalidCode() throws Exception {
        User student = TestUsers.user("student");

        when(userService.getLoggedUser()).thenReturn(student);
        when(courseRepository.findByCourseCode("INVALID")).thenReturn(Optional.empty());

        mockMvc.perform(post("/course/join")
                        .with(jwt().jwt(jwt -> jwt.subject("student")))
                        .contentType("application/json")
                        .content("""
                        {
                          "groupCode": "INVALID"
                        }
                        """))
                .andExpect(status().isNotFound());
    }

    @Test
    void getStudentsInGroup_returnsStudentsWithDifferentStatuses() throws Exception {
        UUID courseId = UUID.randomUUID();

        User creator = TestUsers.user("creator");
        User student1 = TestUsers.user("s1");
        User student2 = TestUsers.user("s2");
        Course course = TestCourses.course(courseId, "Advanced Math", creator);

        Participant creatorParticipant = new Participant();
        creatorParticipant.setStudent(creator);
        creatorParticipant.setCourse(course);
        creatorParticipant.setInvitationStatus(InvitationStatus.ACCEPTED);

        Participant acceptedParticipant = new Participant();
        acceptedParticipant.setStudent(student1);
        acceptedParticipant.setCourse(course);
        acceptedParticipant.setInvitationStatus(InvitationStatus.ACCEPTED);

        Participant waitingParticipant = new Participant();
        waitingParticipant.setStudent(student2);
        waitingParticipant.setCourse(course);
        waitingParticipant.setInvitationStatus(InvitationStatus.WAITING);

        course.setStudents(new HashSet<>(Set.of(creatorParticipant, acceptedParticipant, waitingParticipant)));

        when(userService.getLoggedUser()).thenReturn(creator);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(participantRepository.findByCourseAndInvitationStatus(course, InvitationStatus.ACCEPTED))
                .thenReturn(Set.of(acceptedParticipant));

        mockMvc.perform(get("/course/{courseId}/students", courseId)
                        .with(jwt().jwt(jwt -> jwt.subject("creator"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[*].id").value(hasItem("s1")));
    }

    @Test
    void deleteStudentFromGroup_removesMultipleStudents() throws Exception {
        UUID courseId = UUID.randomUUID();

        User creator = TestUsers.user("creator");
        User student1 = TestUsers.user("student1");
        User student2 = TestUsers.user("student2");
        Course course = TestCourses.course(courseId, "Math", creator);

        Participant creatorParticipant = new Participant();
        creatorParticipant.setStudent(creator);
        creatorParticipant.setCourse(course);
        creatorParticipant.setInvitationStatus(InvitationStatus.ACCEPTED);

        Participant participant1 = new Participant();
        participant1.setStudent(student1);
        participant1.setCourse(course);
        participant1.setInvitationStatus(InvitationStatus.ACCEPTED);

        Participant participant2 = new Participant();
        participant2.setStudent(student2);
        participant2.setCourse(course);
        participant2.setInvitationStatus(InvitationStatus.ACCEPTED);

        course.setStudents(new HashSet<>(Set.of(creatorParticipant, participant1, participant2)));

        when(userService.getLoggedUser()).thenReturn(creator);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(participantRepository.findByStudent_IdAndCourse("student1", course))
                .thenReturn(Optional.of(participant1));
        when(participantRepository.findByStudent_IdAndCourse("student2", course))
                .thenReturn(Optional.of(participant2));

        mockMvc.perform(delete("/course/{courseId}/students/{userId}", courseId, "student1")
                        .with(jwt().jwt(jwt -> jwt.subject("creator"))))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/course/{courseId}/students/{userId}", courseId, "student2")
                        .with(jwt().jwt(jwt -> jwt.subject("creator"))))
                .andExpect(status().isOk());

        verify(courseRepository, times(2)).findById(courseId);
    }

}
