package com.example.ePolan.MockedIntegration;

import com.example.ePolan.Model.Dtos.CourseDto;
import com.example.ePolan.Model.Entities.Course;
import com.example.ePolan.Model.Entities.InvitationStatus;
import com.example.ePolan.Model.Entities.User;
import com.example.ePolan.Repositories.CourseRepository;
import com.example.ePolan.Repositories.UserRepository;
import com.example.ePolan.Utils.TestCourses;
import com.example.ePolan.Utils.TestUsers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
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

}
