package com.example.ePolan.MockedIntegration;

import com.example.ePolan.Model.Entities.*;
import com.example.ePolan.Model.requests.ExerciseListRequest;
import com.example.ePolan.Model.requests.ExerciseRequest;
import com.example.ePolan.Repositories.CourseRepository;
import com.example.ePolan.Repositories.ExerciseRepository;
import com.example.ePolan.Repositories.LessonRepository;
import com.example.ePolan.Services.ExerciseService;
import com.example.ePolan.Services.LessonService;
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

import static org.hamcrest.CoreMatchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("null")
class LessonControllerIT {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    LessonRepository lessonRepository;

    @MockBean
    ExerciseRepository exerciseRepository;

    @MockBean
    CourseRepository courseRepository;

    @MockBean
    UserService userService;

    @Test
    void getExercisesForLesson_returnsExercises() throws Exception {
        UUID lessonId = UUID.randomUUID();

        User creator = TestUsers.user("creator");
        Course course = TestCourses.course(UUID.randomUUID(), "Algebra", creator);
        Lesson lesson = TestLessons.lesson(lessonId, course, Instant.now().plus(1, ChronoUnit.DAYS));

        Exercise exercise1 = TestExercises.exercise(UUID.randomUUID(), 1, lesson);
        Exercise exercise2 = TestExercises.exercise(UUID.randomUUID(), 2, lesson);
        Exercise exercise3 = TestExercises.exercise(UUID.randomUUID(), 3, lesson);

        when(exerciseRepository.findByLesson_Id(lessonId))
                .thenReturn(Set.of(exercise1, exercise2, exercise3));

        mockMvc.perform(get("/lesson/{lessonId}/exercises", lessonId)
                        .with(jwt().jwt(jwt -> jwt.subject("user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void getExercisesForLesson_empty() throws Exception {
        UUID lessonId = UUID.randomUUID();

        when(exerciseRepository.findByLesson_Id(lessonId))
                .thenReturn(Set.of());

        mockMvc.perform(get("/lesson/{lessonId}/exercises", lessonId)
                        .with(jwt().jwt(jwt -> jwt.subject("user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getLessonsForCourse_returnsLessons() throws Exception {
        UUID courseId = UUID.randomUUID();

        User user = TestUsers.user("test-user");
        User creator = TestUsers.user("creator");

        Course course = TestCourses.course(courseId, "Algebra", creator);
        Participant participant = new Participant();
        participant.setStudent(user);
        participant.setCourse(course);
        participant.setInvitationStatus(InvitationStatus.ACCEPTED);
        course.setStudents(new HashSet<>(Set.of(participant)));

        Lesson lesson1 = TestLessons.lesson(UUID.randomUUID(), course, Instant.now().plus(1, ChronoUnit.DAYS));
        Lesson lesson2 = TestLessons.lesson(UUID.randomUUID(), course, Instant.now().plus(2, ChronoUnit.DAYS));
        Lesson lesson3 = TestLessons.lesson(UUID.randomUUID(), course, Instant.now().plus(3, ChronoUnit.DAYS));

        when(userService.getLoggedUser()).thenReturn(user);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(lessonRepository.findByCourse(course))
                .thenReturn(List.of(lesson1, lesson2, lesson3));

        mockMvc.perform(get("/lesson/{courseId}/lessons", courseId)
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void getLessonsForCourse_empty() throws Exception {
        UUID courseId = UUID.randomUUID();

        User user = TestUsers.user("test-user");
        User creator = TestUsers.user("creator");

        Course course = TestCourses.course(courseId, "Algebra", creator);
        Participant participant = new Participant();
        participant.setStudent(user);
        participant.setCourse(course);
        participant.setInvitationStatus(InvitationStatus.ACCEPTED);
        course.setStudents(new HashSet<>(Set.of(participant)));

        when(userService.getLoggedUser()).thenReturn(user);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(lessonRepository.findByCourse(course))
                .thenReturn(List.of());

        mockMvc.perform(get("/lesson/{courseId}/lessons", courseId)
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getLessonsForCourse_courseNotFound() throws Exception {
        UUID courseId = UUID.randomUUID();

        User user = TestUsers.user("test-user");

        when(userService.getLoggedUser()).thenReturn(user);
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/lesson/{courseId}/lessons", courseId)
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getLessonsForCourse_userNotMember() throws Exception {
        UUID courseId = UUID.randomUUID();

        User user = TestUsers.user("test-user");
        User creator = TestUsers.user("creator");

        Course course = TestCourses.course(courseId, "Algebra", creator);
        course.setStudents(new HashSet<>()); // user is not a member

        when(userService.getLoggedUser()).thenReturn(user);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        mockMvc.perform(get("/lesson/{courseId}/lessons", courseId)
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteLesson_success() throws Exception {
        UUID lessonId = UUID.randomUUID();

        User creator = TestUsers.user("creator");
        Course course = TestCourses.course(UUID.randomUUID(), "Algebra", creator);
        Lesson lesson = TestLessons.lesson(lessonId, course, Instant.now().plus(1, ChronoUnit.DAYS));

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        mockMvc.perform(delete("/lesson/{lessonId}", lessonId)
                        .with(jwt().jwt(jwt -> jwt.subject("user"))))
                .andExpect(status().isOk());

        verify(lessonRepository).delete(lesson);
    }

    @Test
    void deleteLesson_notFound() throws Exception {
        UUID lessonId = UUID.randomUUID();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/lesson/{lessonId}", lessonId)
                        .with(jwt().jwt(jwt -> jwt.subject("user"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteLesson_pastLesson() throws Exception {
        UUID lessonId = UUID.randomUUID();

        User creator = TestUsers.user("creator");
        Course course = TestCourses.course(UUID.randomUUID(), "Algebra", creator);
        Lesson lesson = TestLessons.lesson(lessonId, course, Instant.now().minus(2, ChronoUnit.DAYS));

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        mockMvc.perform(delete("/lesson/{lessonId}", lessonId)
                        .with(jwt().jwt(jwt -> jwt.subject("user"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateExercisesForLesson_success() throws Exception {
        UUID lessonId = UUID.randomUUID();

        User creator = TestUsers.user("creator");
        Course course = TestCourses.course(UUID.randomUUID(), "Algebra", creator);
        Lesson lesson = TestLessons.lesson(lessonId, course, Instant.now().plus(1, ChronoUnit.DAYS));

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(exerciseRepository.findByLesson(lesson)).thenReturn(Set.of());

        mockMvc.perform(put("/lesson/{lessonId}", lessonId)
                        .with(jwt().jwt(jwt -> jwt.subject("creator")))
                        .contentType("application/json")
                        .content("""
                        {
                          "exercises": [
                            {"exerciseNumber": 1, "subpoint": null},
                            {"exerciseNumber": 2, "subpoint": null}
                          ]
                        }
                        """))
                .andExpect(status().isOk());

        verify(exerciseRepository, times(2)).save(any());
    }

    @Test
    void updateExercisesForLesson_lessonNotFound() throws Exception {
        UUID lessonId = UUID.randomUUID();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        mockMvc.perform(put("/lesson/{lessonId}", lessonId)
                        .with(jwt().jwt(jwt -> jwt.subject("creator")))
                        .contentType("application/json")
                        .content("""
                        {
                          "exercises": [
                            {"exerciseNumber": 1, "subpoint": null}
                          ]
                        }
                        """))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateExercisesForLesson_existingDeclarations() throws Exception {
        UUID lessonId = UUID.randomUUID();

        User creator = TestUsers.user("creator");
        User student = TestUsers.user("student");
        Course course = TestCourses.course(UUID.randomUUID(), "Algebra", creator);
        Lesson lesson = TestLessons.lesson(lessonId, course, Instant.now().plus(1, ChronoUnit.DAYS));

        Exercise existingExercise = TestExercises.exercise(UUID.randomUUID(), 1, lesson);
        ExerciseDeclaration declaration = TestDeclarations.exerciseDeclaration(UUID.randomUUID(), student, existingExercise);
        existingExercise.setDeclarations(new HashSet<>(Set.of(declaration)));

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(exerciseRepository.findByLesson(lesson)).thenReturn(Set.of(existingExercise));

        mockMvc.perform(put("/lesson/{lessonId}", lessonId)
                        .with(jwt().jwt(jwt -> jwt.subject("creator")))
                        .contentType("application/json")
                        .content("""
                        {
                          "exercises": [
                            {"exerciseNumber": 1, "subpoint": null}
                          ]
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addLesson_success() throws Exception {
        UUID courseId = UUID.randomUUID();
        Instant lessonDate = Instant.now().plus(5, ChronoUnit.DAYS);

        User creator = TestUsers.user("creator");
        Course course = TestCourses.course(courseId, "Algebra", creator);

        Lesson newLesson = TestLessons.lesson(UUID.randomUUID(), course, lessonDate);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(lessonRepository.save(any())).thenReturn(newLesson);

        mockMvc.perform(put("/lesson/{courseId}/{date}/addLesson", courseId, lessonDate)
                        .with(jwt().jwt(jwt -> jwt.subject("creator"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void addLesson_courseNotFound() throws Exception {
        UUID courseId = UUID.randomUUID();
        Instant lessonDate = Instant.now().plus(5, ChronoUnit.DAYS);

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        mockMvc.perform(put("/lesson/{courseId}/{date}/addLesson", courseId, lessonDate)
                        .with(jwt().jwt(jwt -> jwt.subject("creator"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateExercisesForLesson_multipleExercises() throws Exception {
        UUID lessonId = UUID.randomUUID();

        User creator = TestUsers.user("creator");
        Course course = TestCourses.course(UUID.randomUUID(), "Algebra", creator);
        Lesson lesson = TestLessons.lesson(lessonId, course, Instant.now().plus(1, ChronoUnit.DAYS));

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(exerciseRepository.findByLesson(lesson)).thenReturn(Set.of());

        mockMvc.perform(put("/lesson/{lessonId}", lessonId)
                        .with(jwt().jwt(jwt -> jwt.subject("creator")))
                        .contentType("application/json")
                        .content("""
                        {
                          "exercises": [
                            {"exerciseNumber": 1, "subpoint": "a"},
                            {"exerciseNumber": 1, "subpoint": "b"},
                            {"exerciseNumber": 2, "subpoint": null},
                            {"exerciseNumber": 3, "subpoint": "a"}
                          ]
                        }
                        """))
                .andExpect(status().isOk());

        verify(exerciseRepository, times(4)).save(any());
    }

    @Test
    void getExercisesForLesson_sorted() throws Exception {
        UUID lessonId = UUID.randomUUID();

        User creator = TestUsers.user("creator");
        Course course = TestCourses.course(UUID.randomUUID(), "Algebra", creator);
        Lesson lesson = TestLessons.lesson(lessonId, course, Instant.now().plus(1, ChronoUnit.DAYS));

        Exercise exercise1 = TestExercises.exercise(UUID.randomUUID(), 1, lesson);
        Exercise exercise2 = TestExercises.exercise(UUID.randomUUID(), 2, lesson);
        Exercise exercise3 = TestExercises.exercise(UUID.randomUUID(), 1, lesson);

        when(exerciseRepository.findByLesson_Id(lessonId))
                .thenReturn(Set.of(exercise3, exercise1, exercise2));

        mockMvc.perform(get("/lesson/{lessonId}/exercises", lessonId)
                        .with(jwt().jwt(jwt -> jwt.subject("user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }
}
