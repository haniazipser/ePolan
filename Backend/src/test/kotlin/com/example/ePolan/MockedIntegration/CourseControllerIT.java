package com.example.ePolan.MockedIntegration;

import com.example.ePolan.Model.Entities.Course;
import com.example.ePolan.Model.Entities.InvitationStatus;
import com.example.ePolan.Model.Entities.Participant;
import com.example.ePolan.Model.Entities.User;
import com.example.ePolan.Repositories.CourseRepository;
import com.example.ePolan.Repositories.UserRepository;
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
class CourseControllerIT {

    @MockBean
    CourseRepository courseRepository;

    @MockBean
    UserRepository userRepository;


    @Autowired
    MockMvc mockMvc;

    @Test
    void getStudentGroups_returnsCourses() throws Exception {

        User user = TestUsers.user("test-user");
        User creator = TestUsers.user("test-creator");
        Course course = TestCourses.course(UUID.randomUUID(),"Algebra", creator);
        course.setInstructor("Cooke");

        when(courseRepository.findDistinctByStudents_Student_IdAndStudents_InvitationStatus("test-user", InvitationStatus.ACCEPTED))
                .thenReturn(Set.of(course));

        when(userRepository.findById("test-user"))
                .thenReturn(Optional.of(user));

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

        when(userRepository.findById("test-user"))
                .thenReturn(Optional.of(student));

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

        when(userRepository.findById("test-user"))
                .thenReturn(Optional.of(student));

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
    void getStudentGroups_returnsNotFound_whenStudentMissing() throws Exception {
        when(userRepository.findById("test-user"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/course")
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getStudentsInGroup_returnsStudents() throws Exception {
        UUID courseId = UUID.randomUUID();

        User creator = TestUsers.user("creator");
        User student1 = TestUsers.user("s1");
        User student2 = TestUsers.user("s2");
        Course course = TestCourses.course(courseId, "Algebra", creator);

        Participant participant1 = new Participant();
        participant1.setStudent(student1);
        participant1.setCourse(course);
        participant1.setInvitationStatus(InvitationStatus.ACCEPTED);

        Participant participant2 = new Participant();
        participant2.setStudent(student2);
        participant2.setCourse(course);
        participant2.setInvitationStatus(InvitationStatus.ACCEPTED);


        course.setStudents(new HashSet<>(Set.of(participant1)));
        course.getStudents().add(participant2);

        when(courseRepository.findById(courseId))
                .thenReturn(Optional.of(course));

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

        when(userRepository.findById("creator"))
                .thenReturn(Optional.of(creator));

        when(courseRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

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

        when(userRepository.findById("student"))
                .thenReturn(Optional.of(student));

        when(courseRepository.findByCourseCode("JOIN123"))
                .thenReturn(Optional.of(course));

        mockMvc.perform(post("/course/join")
                        .with(jwt().jwt(jwt -> jwt.subject("student")))
                        .contentType("application/json")
                        .content("""
                        {
                          "groupCode": "JOIN123"
                        }
                        """))
                .andExpect(status().isOk());

        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void inviteStudent_sendsInvitation() throws Exception {
        UUID courseId = UUID.randomUUID();
        User creator = TestUsers.user("creator");

        when(userRepository.findById("creator"))
                .thenReturn(Optional.of(creator));

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

        Participant participant = new Participant();
        participant.setStudent(student);
        participant.setCourse(course);
        participant.setInvitationStatus(InvitationStatus.ACCEPTED);

        course.setStudents(new HashSet<>(Set.of(participant)));

        when(courseRepository.findById(courseId))
                .thenReturn(Optional.of(course));

        mockMvc.perform(delete("/course/{courseId}/students/{userId}", courseId, "student")
                        .with(jwt().jwt(jwt -> jwt.subject("creator"))))
                .andExpect(status().isOk());

        verify(courseRepository).save(any(Course.class));
    }

}
