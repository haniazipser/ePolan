package com.example.ePolan.MockedIntegration;

import com.example.ePolan.Model.Entities.*;
import com.example.ePolan.Repositories.LessonRepository;
import com.example.ePolan.Repositories.PointRepository;
import com.example.ePolan.Services.PointService;
import com.example.ePolan.Services.UserService;
import com.example.ePolan.Utils.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("null")
class PointControllerIT {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PointRepository pointRepository;

    @MockBean
    LessonRepository lessonRepository;

    @MockBean
    UserService userService;

    @Test
    void getUsersActivity_returnsActivity() throws Exception {
        User user = TestUsers.user("test-user");
        User creator = TestUsers.user("creator");

        Course course1 = TestCourses.course(UUID.randomUUID(), "Algebra", creator);
        Course course2 = TestCourses.course(UUID.randomUUID(), "Geometry", creator);

        Lesson lesson1 = TestLessons.lesson(UUID.randomUUID(), course1, Instant.now().plus(1, ChronoUnit.DAYS));
        Lesson lesson2 = TestLessons.lesson(UUID.randomUUID(), course2, Instant.now().plus(2, ChronoUnit.DAYS));

        Point point1 = new Point();
        point1.setStudent(user);
        point1.setLesson(lesson1);
        point1.setActivityValue(10.0);

        Point point2 = new Point();
        point2.setStudent(user);
        point2.setLesson(lesson2);
        point2.setActivityValue(15.0);

        when(userService.getLoggedUser()).thenReturn(user);
        when(pointRepository.findByStudent(user))
                .thenReturn(Set.of(point1, point2));

        mockMvc.perform(get("/points")
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getUsersActivity_empty() throws Exception {
        User user = TestUsers.user("test-user");

        when(userService.getLoggedUser()).thenReturn(user);
        when(pointRepository.findByStudent(user))
                .thenReturn(Set.of());

        mockMvc.perform(get("/points")
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getUsersActivityInCourse_returnsActivity() throws Exception {
        UUID courseId = UUID.randomUUID();

        User user = TestUsers.user("test-user");
        User creator = TestUsers.user("creator");

        Course course = TestCourses.course(courseId, "Algebra", creator);

        Lesson lesson1 = TestLessons.lesson(UUID.randomUUID(), course, Instant.now().plus(1, ChronoUnit.DAYS));
        Lesson lesson2 = TestLessons.lesson(UUID.randomUUID(), course, Instant.now().plus(2, ChronoUnit.DAYS));
        Lesson lesson3 = TestLessons.lesson(UUID.randomUUID(), course, Instant.now().plus(3, ChronoUnit.DAYS));

        Point point1 = new Point();
        point1.setStudent(user);
        point1.setLesson(lesson1);
        point1.setActivityValue(10.0);

        Point point2 = new Point();
        point2.setStudent(user);
        point2.setLesson(lesson2);
        point2.setActivityValue(15.0);

        Point point3 = new Point();
        point3.setStudent(user);
        point3.setLesson(lesson3);
        point3.setActivityValue(20.0);

        when(userService.getLoggedUser()).thenReturn(user);
        when(pointRepository.findByStudentAndLesson_Course_Id(user, courseId))
                .thenReturn(Set.of(point1, point2, point3));

        mockMvc.perform(get("/points/{courseId}", courseId)
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void getUsersActivityInCourse_empty() throws Exception {
        UUID courseId = UUID.randomUUID();

        User user = TestUsers.user("test-user");

        when(userService.getLoggedUser()).thenReturn(user);
        when(pointRepository.findByStudentAndLesson_Course_Id(user, courseId))
                .thenReturn(Set.of());

        mockMvc.perform(get("/points/{courseId}", courseId)
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void addStudentActivity_newActivity() throws Exception {
        UUID lessonId = UUID.randomUUID();

        User user = TestUsers.user("test-user");
        User creator = TestUsers.user("creator");

        Course course = TestCourses.course(UUID.randomUUID(), "Algebra", creator);
        Lesson lesson = TestLessons.lesson(lessonId, course, Instant.now().plus(1, ChronoUnit.DAYS));

        when(userService.getLoggedUser()).thenReturn(user);
        when(pointRepository.findByLesson_IdAndStudent(lessonId, user))
                .thenReturn(Optional.empty());
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        mockMvc.perform(post("/points/{lessonId}", lessonId)
                        .with(jwt().jwt(jwt -> jwt.subject("test-user")))
                        .contentType("application/json")
                        .content("""
                        {
                          "value": 12.5
                        }
                        """))
                .andExpect(status().isOk());

        verify(pointRepository).save(any());
    }

    @Test
    void addStudentActivity_updateExistingActivity() throws Exception {
        UUID lessonId = UUID.randomUUID();

        User user = TestUsers.user("test-user");
        User creator = TestUsers.user("creator");

        Course course = TestCourses.course(UUID.randomUUID(), "Algebra", creator);
        Lesson lesson = TestLessons.lesson(lessonId, course, Instant.now().plus(1, ChronoUnit.DAYS));

        Point existingPoint = new Point();
        existingPoint.setStudent(user);
        existingPoint.setLesson(lesson);
        existingPoint.setActivityValue(5.0);

        when(userService.getLoggedUser()).thenReturn(user);
        when(pointRepository.findByLesson_IdAndStudent(lessonId, user))
                .thenReturn(Optional.of(existingPoint));

        mockMvc.perform(post("/points/{lessonId}", lessonId)
                        .with(jwt().jwt(jwt -> jwt.subject("test-user")))
                        .contentType("application/json")
                        .content("""
                        {
                          "value": 10.5
                        }
                        """))
                .andExpect(status().isOk());

        verify(pointRepository).save(existingPoint);
    }

    @Test
    void addStudentActivity_lessonNotFound() throws Exception {
        UUID lessonId = UUID.randomUUID();

        User user = TestUsers.user("test-user");

        when(userService.getLoggedUser()).thenReturn(user);
        when(pointRepository.findByLesson_IdAndStudent(lessonId, user))
                .thenReturn(Optional.empty());
        when(lessonRepository.findById(lessonId))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/points/{lessonId}", lessonId)
                        .with(jwt().jwt(jwt -> jwt.subject("test-user")))
                        .contentType("application/json")
                        .content("""
                        {
                          "value": 12.5
                        }
                        """))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUsersActivity_multipleUsers() throws Exception {
        User user = TestUsers.user("test-user");
        User creator = TestUsers.user("creator");

        Course course = TestCourses.course(UUID.randomUUID(), "Algebra", creator);

        Lesson lesson1 = TestLessons.lesson(UUID.randomUUID(), course, Instant.now().plus(1, ChronoUnit.DAYS));
        Lesson lesson2 = TestLessons.lesson(UUID.randomUUID(), course, Instant.now().plus(2, ChronoUnit.DAYS));
        Lesson lesson3 = TestLessons.lesson(UUID.randomUUID(), course, Instant.now().plus(3, ChronoUnit.DAYS));
        Lesson lesson4 = TestLessons.lesson(UUID.randomUUID(), course, Instant.now().plus(4, ChronoUnit.DAYS));

        Point point1 = new Point();
        point1.setStudent(user);
        point1.setLesson(lesson1);
        point1.setActivityValue(10.0);

        Point point2 = new Point();
        point2.setStudent(user);
        point2.setLesson(lesson2);
        point2.setActivityValue(15.0);

        Point point3 = new Point();
        point3.setStudent(user);
        point3.setLesson(lesson3);
        point3.setActivityValue(20.0);

        Point point4 = new Point();
        point4.setStudent(user);
        point4.setLesson(lesson4);
        point4.setActivityValue(25.0);

        when(userService.getLoggedUser()).thenReturn(user);
        when(pointRepository.findByStudent(user))
                .thenReturn(Set.of(point1, point2, point3, point4));

        mockMvc.perform(get("/points")
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void addStudentActivity_withZeroValue() throws Exception {
        UUID lessonId = UUID.randomUUID();

        User user = TestUsers.user("test-user");
        User creator = TestUsers.user("creator");

        Course course = TestCourses.course(UUID.randomUUID(), "Algebra", creator);
        Lesson lesson = TestLessons.lesson(lessonId, course, Instant.now().plus(1, ChronoUnit.DAYS));

        when(userService.getLoggedUser()).thenReturn(user);
        when(pointRepository.findByLesson_IdAndStudent(lessonId, user))
                .thenReturn(Optional.empty());
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        mockMvc.perform(post("/points/{lessonId}", lessonId)
                        .with(jwt().jwt(jwt -> jwt.subject("test-user")))
                        .contentType("application/json")
                        .content("""
                        {
                          "value": 0.0
                        }
                        """))
                .andExpect(status().isOk());

        verify(pointRepository).save(any());
    }

    @Test
    void addStudentActivity_withNegativeValue() throws Exception {
        UUID lessonId = UUID.randomUUID();

        User user = TestUsers.user("test-user");
        User creator = TestUsers.user("creator");

        Course course = TestCourses.course(UUID.randomUUID(), "Algebra", creator);
        Lesson lesson = TestLessons.lesson(lessonId, course, Instant.now().plus(1, ChronoUnit.DAYS));

        when(userService.getLoggedUser()).thenReturn(user);
        when(pointRepository.findByLesson_IdAndStudent(lessonId, user))
                .thenReturn(Optional.empty());
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        mockMvc.perform(post("/points/{lessonId}", lessonId)
                        .with(jwt().jwt(jwt -> jwt.subject("test-user")))
                        .contentType("application/json")
                        .content("""
                        {
                          "value": -5.0
                        }
                        """))
                .andExpect(status().isOk());

        verify(pointRepository).save(any());
    }
}
