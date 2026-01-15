package com.example.ePolan.MockedIntegration;

import com.example.ePolan.Model.Dtos.DeclarationDto;
import com.example.ePolan.Model.Dtos.DeclarationShortDto;
import com.example.ePolan.Model.Entities.*;
import com.example.ePolan.Repositories.DeclarationRepository;
import com.example.ePolan.Repositories.UserRepository;
import com.example.ePolan.Services.DeclarationService;
import com.example.ePolan.Services.ExerciseApplicationService;
import com.example.ePolan.Utils.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DeclarationControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    DeclarationService declarationService;

    @MockBean
    DeclarationRepository declarationRepository;

    @MockBean
    UserRepository userRepository;


    @Test
    void getStudentDeclarations_returnsDeclarations() throws Exception {
        final User user = TestUsers.user("test-user");
        User creator = TestUsers.user("test-creator");

        Course course1 = TestCourses.course(UUID.randomUUID(), "Algorytmy i Dzikie Rytmy", creator );
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

        when(userRepository.findById("test-user"))
                .thenReturn(Optional.of(user));
        when(declarationRepository.findByStudent(user))
                .thenReturn(Set.of(declaration1, declaration2, declaration3, declaration4, declaration5, declaration6, declaration7));


        mockMvc.perform(get("/declaration")
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7));
    }

    @Test
    void getStudentDeclarations_pastLessons() throws Exception {
        final User user = TestUsers.user("test-user");
        User creator = TestUsers.user("test-creator");

        Course course1 = TestCourses.course(UUID.randomUUID(), "Algorytmy i Dzikie Rytmy", creator );
        Course course2 = TestCourses.course(UUID.randomUUID(), "Rachunek Podobieństwa i Satyryka", creator);

        Lesson lesson11 = TestLessons.lesson(UUID.randomUUID(), course1, Instant.now().plus(-1, ChronoUnit.DAYS));
        Lesson lesson21 = TestLessons.lesson(UUID.randomUUID(), course2, Instant.now().plus(-3, ChronoUnit.DAYS));
        Lesson lesson22 = TestLessons.lesson(UUID.randomUUID(), course2, Instant.now().plus(-4, ChronoUnit.DAYS));
        Lesson lesson23 = TestLessons.lesson(UUID.randomUUID(), course2, Instant.now().plus(-5, ChronoUnit.DAYS));

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

        when(userRepository.findById("test-user"))
                .thenReturn(Optional.of(user));
        when(declarationRepository.findByStudent(user))
                .thenReturn(Set.of(declaration1, declaration2, declaration3, declaration4, declaration5, declaration6, declaration7));


        mockMvc.perform(get("/declaration")
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7));
    }

    @Test
    void getStudentDeclarations_funkyExerciseNumbers() throws Exception {
        final User user = TestUsers.user("test-user");
        User creator = TestUsers.user("test-creator");

        Course course1 = TestCourses.course(UUID.randomUUID(), "Algorytmy i Dzikie Rytmy", creator );

        Lesson lesson11 = TestLessons.lesson(UUID.randomUUID(), course1, Instant.now().plus(-1, ChronoUnit.DAYS));

        Exercise exercise11 = TestExercises.exercise(UUID.randomUUID(), -1, lesson11);
        Exercise exercise12 = TestExercises.exercise(UUID.randomUUID(), 200000, lesson11);
        Exercise exercise13 = TestExercises.exercise(UUID.randomUUID(), Integer.MAX_VALUE, lesson11);
        Exercise exercise14 = TestExercises.exercise(UUID.randomUUID(), Integer.MIN_VALUE, lesson11);

        ExerciseDeclaration declaration1 = TestDeclarations.exerciseDeclaration(UUID.randomUUID(), user, exercise11);
        ExerciseDeclaration declaration2 = TestDeclarations.exerciseDeclaration(UUID.randomUUID(), user, exercise12);
        ExerciseDeclaration declaration3 = TestDeclarations.exerciseDeclaration(UUID.randomUUID(), user, exercise13);
        ExerciseDeclaration declaration4 = TestDeclarations.exerciseDeclaration(UUID.randomUUID(), user, exercise14);

        when(userRepository.findById("test-user"))
                .thenReturn(Optional.of(user));
        when(declarationRepository.findByStudent(user))
                .thenReturn(Set.of(declaration1, declaration2, declaration3, declaration4));


        mockMvc.perform(get("/declaration")
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void getStudentDeclarations_NothingDeclared() throws Exception {
        final User user = TestUsers.user("test-user");
        User creator = TestUsers.user("test-creator");

        when(userRepository.findById("test-user"))
                .thenReturn(Optional.of(user));
        when(declarationRepository.findByStudent(user))
                .thenReturn(Set.of());


        mockMvc.perform(get("/declaration")
                        .with(jwt().jwt(jwt -> jwt.subject("test-user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
