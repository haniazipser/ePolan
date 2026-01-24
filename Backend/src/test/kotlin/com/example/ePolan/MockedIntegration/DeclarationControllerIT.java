package com.example.ePolan.MockedIntegration;

import com.example.ePolan.Model.Entities.*;
import com.example.ePolan.Repositories.DeclarationRepository;
import com.example.ePolan.Repositories.ExerciseRepository;
import com.example.ePolan.Services.DeclarationService;
import com.example.ePolan.Services.ExerciseApplicationService;
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
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("null")
class DeclarationControllerIT {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    DeclarationRepository declarationRepository;

    @MockBean
    ExerciseRepository exerciseRepository;

    @MockBean
    PointService pointService;

    @MockBean
    UserService userService;

    @MockBean
    ExerciseApplicationService exerciseApplicationService;


    @Test
    void getStudentDeclarations_returnsDeclarations() throws Exception {
        User user = TestUsers.user("test-user");
        User creator = TestUsers.user("test-creator");

        Course course1 = TestCourses.course(UUID.randomUUID(), "Algorytmy i Dzikie Rytmy", creator);
        Course course2 = TestCourses.course(UUID.randomUUID(), "Rachunek Podobieństwa i Satyryka", creator);

        Lesson lesson11 = TestLessons.lesson(UUID.randomUUID(), course1, Instant.now().plus(1, ChronoUnit.DAYS));
        Lesson lesson21 = TestLessons.lesson(UUID.randomUUID(), course2, Instant.now().plus(3, ChronoUnit.DAYS));
        Lesson lesson22 = TestLessons.lesson(UUID.randomUUID(), course2, Instant.now().plus(4, ChronoUnit.DAYS));
        Lesson lesson23 = TestLessons.lesson(UUID.randomUUID(), course2, Instant.now().plus(5, ChronoUnit.DAYS));

        Exercise exercise11 = TestExercises.exercise(UUID.randomUUID(), 1, lesson11);
        Exercise exercise12 = TestExercises.exercise(UUID.randomUUID(), 2, lesson11);
        Exercise exercise13 = TestExercises.exercise(UUID.randomUUID(), 3, lesson11);
        Exercise exercise14 = TestExercises.exercise(UUID.randomUUID(), 4, lesson11);

        Exercise exercise21 = TestExercises.exercise(UUID.randomUUID(), 1, lesson21);
        Exercise exercise22 = TestExercises.exercise(UUID.randomUUID(), 2, lesson21);
        Exercise exercise23 = TestExercises.exercise(UUID.randomUUID(), 3, lesson21);
        Exercise exercise24 = TestExercises.exercise(UUID.randomUUID(), 4, lesson21);

        Exercise exercise31 = TestExercises.exercise(UUID.randomUUID(), 1, lesson22);
        Exercise exercise32 = TestExercises.exercise(UUID.randomUUID(), 2, lesson22);
        Exercise exercise33 = TestExercises.exercise(UUID.randomUUID(), 3, lesson22);
        Exercise exercise34 = TestExercises.exercise(UUID.randomUUID(), 4, lesson22);

        Exercise exercise41 = TestExercises.exercise(UUID.randomUUID(), 1, lesson23);
        Exercise exercise42 = TestExercises.exercise(UUID.randomUUID(), 2, lesson23);
        Exercise exercise43 = TestExercises.exercise(UUID.randomUUID(), 3, lesson23);
        Exercise exercise44 = TestExercises.exercise(UUID.randomUUID(), 4, lesson23);

        ExerciseDeclaration declaration1 = TestDeclarations.exerciseDeclaration(UUID.randomUUID(), user, exercise11);
        ExerciseDeclaration declaration2 = TestDeclarations.exerciseDeclaration(UUID.randomUUID(), user, exercise12);
        ExerciseDeclaration declaration3 = TestDeclarations.exerciseDeclaration(UUID.randomUUID(), user, exercise13);
        ExerciseDeclaration declaration4 = TestDeclarations.exerciseDeclaration(UUID.randomUUID(), user, exercise14);

        ExerciseDeclaration declaration5 = TestDeclarations.exerciseDeclaration(UUID.randomUUID(), user, exercise32);
        ExerciseDeclaration declaration6 = TestDeclarations.exerciseDeclaration(UUID.randomUUID(), user, exercise33);
        ExerciseDeclaration declaration7 = TestDeclarations.exerciseDeclaration(UUID.randomUUID(), user, exercise42);

        when(userService.getLoggedUser()).thenReturn(user);
        when(declarationRepository.findByStudent(user))
                .thenReturn(Set.of(declaration1, declaration2, declaration3, declaration4, declaration5, declaration6, declaration7));

        mockMvc.perform(get("/declaration")
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7));
    }

    @Test
    void getStudentDeclarations_empty() throws Exception {
        User user = TestUsers.user("test-user");

        when(userService.getLoggedUser()).thenReturn(user);
        when(declarationRepository.findByStudent(user))
                .thenReturn(Set.of());

        mockMvc.perform(get("/declaration")
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getStudentDeclarationsInCourse_returnsDeclarations() throws Exception {
        UUID courseId = UUID.randomUUID();

        User user = TestUsers.user("test-user");
        User creator = TestUsers.user("test-creator");

        Course course = TestCourses.course(courseId, "Algebra", creator);

        Lesson lesson1 = TestLessons.lesson(UUID.randomUUID(), course, Instant.now().plus(1, ChronoUnit.DAYS));
        Lesson lesson2 = TestLessons.lesson(UUID.randomUUID(), course, Instant.now().plus(2, ChronoUnit.DAYS));

        Exercise exercise1 = TestExercises.exercise(UUID.randomUUID(), 1, lesson1);
        Exercise exercise2 = TestExercises.exercise(UUID.randomUUID(), 2, lesson1);
        Exercise exercise3 = TestExercises.exercise(UUID.randomUUID(), 1, lesson2);

        ExerciseDeclaration declaration1 = TestDeclarations.exerciseDeclaration(UUID.randomUUID(), user, exercise1);
        ExerciseDeclaration declaration2 = TestDeclarations.exerciseDeclaration(UUID.randomUUID(), user, exercise2);
        ExerciseDeclaration declaration3 = TestDeclarations.exerciseDeclaration(UUID.randomUUID(), user, exercise3);

        when(userService.getLoggedUser()).thenReturn(user);
        when(declarationRepository.findByStudentAndExercise_Lesson_Course_Id(user, courseId))
                .thenReturn(Set.of(declaration1, declaration2, declaration3));

        mockMvc.perform(get("/declaration/course/{courseId}", courseId)
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void getStudentDeclarationsInCourse_empty() throws Exception {
        UUID courseId = UUID.randomUUID();
        User user = TestUsers.user("test-user");

        when(userService.getLoggedUser()).thenReturn(user);
        when(declarationRepository.findByStudentAndExercise_Lesson_Course_Id(user, courseId))
                .thenReturn(Set.of());

        mockMvc.perform(get("/declaration/course/{courseId}", courseId)
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getStudentDeclarationsForLesson_returnsDeclarations() throws Exception {
        UUID lessonId = UUID.randomUUID();

        User user = TestUsers.user("test-user");
        User creator = TestUsers.user("test-creator");

        Course course = TestCourses.course(UUID.randomUUID(), "Algebra", creator);
        Lesson lesson = TestLessons.lesson(lessonId, course, Instant.now().plus(1, ChronoUnit.DAYS));

        Exercise exercise1 = TestExercises.exercise(UUID.randomUUID(), 1, lesson);
        Exercise exercise2 = TestExercises.exercise(UUID.randomUUID(), 2, lesson);
        Exercise exercise3 = TestExercises.exercise(UUID.randomUUID(), 3, lesson);

        ExerciseDeclaration declaration1 = TestDeclarations.exerciseDeclaration(UUID.randomUUID(), user, exercise1);
        ExerciseDeclaration declaration2 = TestDeclarations.exerciseDeclaration(UUID.randomUUID(), user, exercise2);
        ExerciseDeclaration declaration3 = TestDeclarations.exerciseDeclaration(UUID.randomUUID(), user, exercise3);

        when(userService.getLoggedUser()).thenReturn(user);
        when(declarationRepository.findByStudentAndExercise_Lesson_Id(user, lessonId))
                .thenReturn(Set.of(declaration1, declaration2, declaration3));

        mockMvc.perform(get("/declaration/lesson/{lessonId}", lessonId)
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void getStudentDeclarationsForLesson_empty() throws Exception {
        UUID lessonId = UUID.randomUUID();
        User user = TestUsers.user("test-user");

        when(userService.getLoggedUser()).thenReturn(user);
        when(declarationRepository.findByStudentAndExercise_Lesson_Id(user, lessonId))
                .thenReturn(Set.of());

        mockMvc.perform(get("/declaration/lesson/{lessonId}", lessonId)
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void declareExercise_success() throws Exception {
        UUID exerciseId = UUID.randomUUID();

        User user = TestUsers.user("test-user");
        User creator = TestUsers.user("test-creator");

        Course course = TestCourses.course(UUID.randomUUID(), "Algebra", creator);
        Participant participant = new Participant();
        participant.setStudent(user);
        participant.setCourse(course);
        participant.setInvitationStatus(InvitationStatus.ACCEPTED);
        course.setStudents(new HashSet<>(Set.of(participant)));

        Lesson lesson = TestLessons.lesson(UUID.randomUUID(), course, Instant.now().plus(14, ChronoUnit.DAYS));
        Exercise exercise = TestExercises.exercise(exerciseId, 1, lesson);

        when(userService.getLoggedUser()).thenReturn(user);
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise));

        mockMvc.perform(post("/declaration/{exerciseId}", exerciseId)
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isOk());

        verify(exerciseRepository).findById(exerciseId);
    }

    @Test
    void declareExercise_exerciseNotFound() throws Exception {
        UUID exerciseId = UUID.randomUUID();
        User user = TestUsers.user("test-user");

        when(userService.getLoggedUser()).thenReturn(user);
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.empty());

        mockMvc.perform(post("/declaration/{exerciseId}", exerciseId)
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void declareExercise_pastLesson() throws Exception {
        UUID exerciseId = UUID.randomUUID();

        User user = TestUsers.user("test-user");
        User creator = TestUsers.user("test-creator");

        Course course = TestCourses.course(UUID.randomUUID(), "Algebra", creator);
        Participant participant = new Participant();
        participant.setStudent(user);
        participant.setCourse(course);
        participant.setInvitationStatus(InvitationStatus.ACCEPTED);
        course.setStudents(new HashSet<>(Set.of(participant)));

        Lesson lesson = TestLessons.lesson(UUID.randomUUID(), course, Instant.now().minus(2, ChronoUnit.DAYS));
        Exercise exercise = TestExercises.exercise(exerciseId, 1, lesson);

        when(userService.getLoggedUser()).thenReturn(user);
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise));

        mockMvc.perform(post("/declaration/{exerciseId}", exerciseId)
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void declareExercise_notCourseMemeber() throws Exception {
        UUID exerciseId = UUID.randomUUID();

        User user = TestUsers.user("test-user");
        User creator = TestUsers.user("test-creator");

        Course course = TestCourses.course(UUID.randomUUID(), "Algebra", creator);
        course.setStudents(new HashSet<>()); // empty set so user is not a member

        Lesson lesson = TestLessons.lesson(UUID.randomUUID(), course, Instant.now().plus(14, ChronoUnit.DAYS));
        Exercise exercise = TestExercises.exercise(exerciseId, 1, lesson);

        when(userService.getLoggedUser()).thenReturn(user);
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise));

        mockMvc.perform(post("/declaration/{exerciseId}", exerciseId)
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteDeclaration_success() throws Exception {
        UUID declarationId = UUID.randomUUID();

        User user = TestUsers.user("test-user");
        User creator = TestUsers.user("test-creator");

        Course course = TestCourses.course(UUID.randomUUID(), "Algebra", creator);
        Lesson lesson = TestLessons.lesson(UUID.randomUUID(), course, Instant.now().plus(14, ChronoUnit.DAYS));
        Exercise exercise = TestExercises.exercise(UUID.randomUUID(), 1, lesson);

        ExerciseDeclaration declaration = TestDeclarations.exerciseDeclaration(declarationId, user, exercise);
        declaration.setDeclarationStatus(DeclarationStatus.WAITING);

        when(declarationRepository.findById(declarationId)).thenReturn(Optional.of(declaration));

        mockMvc.perform(delete("/declaration/{declarationId}", declarationId)
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isOk());

        verify(declarationRepository).delete(declaration);
    }

    @Test
    void deleteDeclaration_notFound() throws Exception {
        UUID declarationId = UUID.randomUUID();

        when(declarationRepository.findById(declarationId)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/declaration/{declarationId}", declarationId)
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteDeclaration_alreadyApproved() throws Exception {
        UUID declarationId = UUID.randomUUID();

        User user = TestUsers.user("test-user");
        User creator = TestUsers.user("test-creator");

        Course course = TestCourses.course(UUID.randomUUID(), "Algebra", creator);
        Lesson lesson = TestLessons.lesson(UUID.randomUUID(), course, Instant.now().plus(14, ChronoUnit.DAYS));
        Exercise exercise = TestExercises.exercise(UUID.randomUUID(), 1, lesson);

        ExerciseDeclaration declaration = TestDeclarations.exerciseDeclaration(declarationId, user, exercise);
        declaration.setDeclarationStatus(DeclarationStatus.APPROVED);

        when(declarationRepository.findById(declarationId)).thenReturn(Optional.of(declaration));

        mockMvc.perform(delete("/declaration/{declarationId}", declarationId)
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isBadRequest());
    }
}
