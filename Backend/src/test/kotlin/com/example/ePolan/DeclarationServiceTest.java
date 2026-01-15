package com.example.ePolan;

import com.example.ePolan.Model.Dtos.DeclarationDto;
import com.example.ePolan.Model.Dtos.DeclarationShortDto;
import com.example.ePolan.Model.Dtos.PointDto;
import com.example.ePolan.Model.Entities.Course;
import com.example.ePolan.Model.Entities.DeclarationStatus;
import com.example.ePolan.Model.Entities.Exercise;
import com.example.ePolan.Model.Entities.ExerciseDeclaration;
import com.example.ePolan.Model.Entities.Lesson;
import com.example.ePolan.Model.Entities.User;
import com.example.ePolan.Repositories.DeclarationRepository;
import com.example.ePolan.Repositories.ExerciseRepository;
import com.example.ePolan.Services.DeclarationService;
import com.example.ePolan.Services.PointService;
import com.example.ePolan.Services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;



@ExtendWith(MockitoExtension.class)
class DeclarationServiceTest {

    @Mock
    DeclarationRepository declarationRepository;
    @Mock
    ExerciseRepository exerciseRepository;
    @Mock
    PointService pointService;
    @Mock
    UserService userService;

    @InjectMocks
    DeclarationService service;

    UUID exerciseId;
    UUID lessonId;
    UUID courseId;
    UUID declarationId;

    User user;

    @Test
    void declareExercise_whenExerciseNotFound_throws404() {
        exerciseId = UUID.randomUUID();
        when(userService.getLoggedUser()).thenReturn(new User());
        when(exerciseRepository.findById(exerciseId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.declareExercise(exerciseId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Exercise not found");

        verify(declarationRepository, never()).save(any());
    }

    @Test
    void declareExercise_whenLessonInPast_throws400() {
        UUID exerciseId = UUID.randomUUID();
        User user = new User();

        Lesson lesson = mock(Lesson.class);
        Exercise exercise = mock(Exercise.class);

        when(lesson.getClassDate()).thenReturn(Instant.now().minusSeconds(3600)); // ← POPRAWKA
        when(exercise.getLesson()).thenReturn(lesson);

        when(userService.getLoggedUser()).thenReturn(user);
        when(exerciseRepository.findById(exerciseId))
                .thenReturn(Optional.of(exercise));

        assertThatThrownBy(() -> service.declareExercise(exerciseId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("past lessons");
    }

    @Test
    void declareExercise_whenNotCourseMember_throws403() {
        UUID exerciseId = UUID.randomUUID();
        User user = new User();

        Course course = mock(Course.class);
        Lesson lesson = mock(Lesson.class);
        Exercise exercise = mock(Exercise.class);

        when(lesson.getClassDate()).thenReturn(Instant.now().plusSeconds(86400));
        when(lesson.getCourse()).thenReturn(course);
        when(course.isStudentAMemeber(any(User.class))).thenReturn(false); // ← ZMIANA
        when(exercise.getLesson()).thenReturn(lesson);

        when(userService.getLoggedUser()).thenReturn(user);
        when(exerciseRepository.findById(exerciseId))
                .thenReturn(Optional.of(exercise));

        assertThatThrownBy(() -> service.declareExercise(exerciseId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(rse.getReason()).contains("not a member");
                });
    }

    @Test
    void declareExercise_whenLessThan12Hours_throws400() {
        exerciseId = UUID.randomUUID();
        user = new User();

        Exercise exercise = mockExercise(
                Instant.now().plusSeconds(3600),
                true,
                5
        );

        when(userService.getLoggedUser()).thenReturn(user);
        when(exerciseRepository.findById(exerciseId))
                .thenReturn(Optional.of(exercise));

        assertThatThrownBy(() -> service.declareExercise(exerciseId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("no longer declare");

        verify(declarationRepository, never()).save(any());
    }

    @Test
    void declareExercise_whenValid_savesDeclaration() {
        exerciseId = UUID.randomUUID();
        user = new User();

        Exercise exercise = mockExercise(
                Instant.now().plusSeconds(3600),
                true,
                24
        );

        when(userService.getLoggedUser()).thenReturn(user);
        when(exerciseRepository.findById(exerciseId))
                .thenReturn(Optional.of(exercise));

        service.declareExercise(exerciseId);

        ArgumentCaptor<ExerciseDeclaration> captor =
                ArgumentCaptor.forClass(ExerciseDeclaration.class);

        verify(declarationRepository).save(captor.capture());

        ExerciseDeclaration saved = captor.getValue();
        assertThat(saved.getExercise()).isEqualTo(exercise);
        assertThat(saved.getStudent()).isEqualTo(user);
        assertThat(saved.getDeclarationStatus())
                .isEqualTo(DeclarationStatus.WAITING);
    }

    @Test
    void getAllDeclarationsForLesson_calculatesActivitySum() {
        lessonId = UUID.randomUUID();

        PointDto point1 = new PointDto();
        point1.setActivityValue(2.0);

        PointDto point2 = new PointDto();
        point2.setActivityValue(3.5);

        ExerciseDeclaration d = declarationWithCourse();

        when(declarationRepository.findByExercise_Lesson_Id(lessonId))
                .thenReturn(Set.of(d));

        when(pointService.getUsersActivityInCourse(any(), any()))
                .thenReturn(List.of(
                        point1, point2)
                );

        List<DeclarationShortDto> result =
                service.getAllDeclarationsForLesson(lessonId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPointsInCourse()).isEqualTo(5.5);
    }

    @Test
    void rejectDeclarationsForExercise_setsStatusRejected() {
        ExerciseDeclaration d1 = declaration(UUID.randomUUID());
        ExerciseDeclaration d2 = declaration(UUID.randomUUID());

        when(declarationRepository.findByExercise_Id(exerciseId))
                .thenReturn(Set.of(d1, d2));

        service.rejectDeclarationsForExercise(exerciseId);

        assertThat(d1.getDeclarationStatus())
                .isEqualTo(DeclarationStatus.REJECTED);

        verify(declarationRepository, times(2)).save(any());
    }

    @Test
    void deleteDeclaration_whenApproved_throws400() {
        declarationId = UUID.randomUUID();

        ExerciseDeclaration d = declaration(UUID.randomUUID());
        d.setDeclarationStatus(DeclarationStatus.APPROVED);

        when(declarationRepository.findById(declarationId))
                .thenReturn(Optional.of(d));

        assertThatThrownBy(() -> service.deleteDeclaration(declarationId))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void deleteDeclaration_whenValid_deletes() {
        declarationId = UUID.randomUUID();

        ExerciseDeclaration d = declaration(UUID.randomUUID());

        when(declarationRepository.findById(declarationId))
                .thenReturn(Optional.of(d));

        service.deleteDeclaration(declarationId);

        verify(declarationRepository).delete(d);
    }

    private Exercise mockExercise(
            Instant classDate,
            boolean isMember,
            long hoursToLesson
    ) {
        Course course = mock(Course.class);
        when(course.isStudentAMemeber(any())).thenReturn(isMember);

        Lesson lesson = mock(Lesson.class);
        when(lesson.getClassDate()).thenReturn(classDate);
        when(lesson.getCourse()).thenReturn(course);
        when(lesson.getHoursToLesson()).thenReturn(hoursToLesson);

        Exercise exercise = mock(Exercise.class);
        when(exercise.getLesson()).thenReturn(lesson);

        return exercise;
    }

    private ExerciseDeclaration declaration(UUID id) {
        Exercise exercise =new Exercise();
        exercise.setId(UUID.randomUUID());
        ExerciseDeclaration d = new ExerciseDeclaration();
        d.setId(id);
        d.setDeclarationStatus(DeclarationStatus.WAITING);
        d.setExercise(exercise);
        return d;
    }

    private ExerciseDeclaration declarationWithLesson(UUID id) {
        User user = new User();
        Course course = new Course();
        Lesson lesson = new Lesson();
        Exercise exercise =new Exercise();
        exercise.setId(UUID.randomUUID());
        exercise.setLesson(lesson);
        lesson.setCourse(course);
        ExerciseDeclaration d = new ExerciseDeclaration();
        d.setId(id);
        d.setDeclarationStatus(DeclarationStatus.WAITING);
        d.setExercise(exercise);
        d.setStudent(user);
        return d;
    }

    private ExerciseDeclaration declarationWithCourse() {
        Course course = mock(Course.class);
        when(course.getId()).thenReturn(UUID.randomUUID());

        Lesson lesson = mock(Lesson.class);
        when(lesson.getCourse()).thenReturn(course);

        Exercise exercise = mock(Exercise.class);
        when(exercise.getLesson()).thenReturn(lesson);

        ExerciseDeclaration d = new ExerciseDeclaration();
        d.setExercise(exercise);
        d.setStudent(new User());

        return d;
    }

    private PointDto point(double value) {
        PointDto p = mock(PointDto.class);
        when(p.getActivityValue()).thenReturn(value);
        return p;
    }
}






