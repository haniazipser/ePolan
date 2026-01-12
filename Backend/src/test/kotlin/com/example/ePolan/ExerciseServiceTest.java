package com.example.ePolan;

import com.example.ePolan.Model.Dtos.ExerciseDto;
import com.example.ePolan.Model.Dtos.ExerciseWithPointsDto;
import com.example.ePolan.Model.Dtos.PointDto;
import com.example.ePolan.Model.Entities.Course;
import com.example.ePolan.Model.Entities.Exercise;
import com.example.ePolan.Model.Entities.Lesson;
import com.example.ePolan.Model.Entities.User;
import com.example.ePolan.Repositories.ExerciseRepository;
import com.example.ePolan.Services.ExerciseService;
import com.example.ePolan.Services.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExerciseServiceTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private PointService pointService;

    @InjectMocks
    private ExerciseService exerciseService;

    private UUID lessonId;
    private UUID courseId;
    private String studentId;
    private User student;

    @BeforeEach
    void setup() {
        lessonId = UUID.randomUUID();
        courseId = UUID.randomUUID();
        studentId = UUID.randomUUID().toString();
        student = new User();
        student.setId(studentId);

    }

    @Test
    void getExercisesForLesson_ReturnsEmptyList_WhenNoExercises() {
        when(exerciseRepository.findByLesson_Id(lessonId)).thenReturn(Set.of());

        List<ExerciseDto> result = exerciseService.getExercisesForLesson(lessonId);

        assertThat(result).isEmpty();
        verify(exerciseRepository).findByLesson_Id(lessonId);
    }

    @Test
    void getExercisesForLesson_ReturnsSortedByNumber() {
        Exercise ex1 = createExercise(3, null);
        Exercise ex2 = createExercise(1, null);
        Exercise ex3 = createExercise(2, null);

        when(exerciseRepository.findByLesson_Id(lessonId)).thenReturn(Set.of(ex1, ex2, ex3));

        List<ExerciseDto> result = exerciseService.getExercisesForLesson(lessonId);

        assertThat(result)
                .hasSize(3)
                .extracting(ExerciseDto::getExerciseNumber)
                .containsExactly(1, 2, 3);
        verify(exerciseRepository).findByLesson_Id(lessonId);
    }

    @Test
    void getExercisesForLesson_SortsByNumberThenSubpoint() {
        Exercise ex1 = createExercise(1, "b");
        Exercise ex2 = createExercise(1, "a");
        Exercise ex3 = createExercise(1, null);
        Exercise ex4 = createExercise(2, "a");

        when(exerciseRepository.findByLesson_Id(lessonId)).thenReturn(Set.of(ex1, ex2, ex3, ex4));

        List<ExerciseDto> result = exerciseService.getExercisesForLesson(lessonId);

        assertThat(result).hasSize(4);
        assertThat(result.get(0).getExerciseNumber()).isEqualTo(1);
        assertThat(result.get(0).getSubpoint()).isNull();
        assertThat(result.get(1).getExerciseNumber()).isEqualTo(1);
        assertThat(result.get(1).getSubpoint()).isEqualTo("a");
        assertThat(result.get(2).getExerciseNumber()).isEqualTo(1);
        assertThat(result.get(2).getSubpoint()).isEqualTo("b");
        assertThat(result.get(3).getExerciseNumber()).isEqualTo(2);
        assertThat(result.get(3).getSubpoint()).isEqualTo("a");
    }

    @Test
    void getExercisesForLesson_HandlesNullSubpointsFirst() {
        Exercise ex1 = createExercise(1, "c");
        Exercise ex2 = createExercise(1, null);
        Exercise ex3 = createExercise(1, "a");

        when(exerciseRepository.findByLesson_Id(lessonId)).thenReturn(Set.of(ex1, ex2, ex3));

        List<ExerciseDto> result = exerciseService.getExercisesForLesson(lessonId);

        assertThat(result)
                .hasSize(3)
                .extracting(ExerciseDto::getSubpoint)
                .containsExactly(null, "a", "c");
    }


    @Test
    void getList_ReturnsEmptySet_WhenNoExercises() {
        when(exerciseRepository.findByLesson_Id(lessonId)).thenReturn(Set.of());

        Set<ExerciseWithPointsDto> result = exerciseService.getList(lessonId);

        assertThat(result).isEmpty();
        verify(exerciseRepository).findByLesson_Id(lessonId);
    }

    @Test
    void getList_CalculatesSumOfPoints() {
        Exercise exercise = createExerciseWithApprovedStudent(1, null, student);
        List<PointDto> points = List.of(
                createPointDto(5.0),
                createPointDto(3.0),
                createPointDto(2.0)
        );

        when(exerciseRepository.findByLesson_Id(lessonId)).thenReturn(Set.of(exercise));
        when(pointService.getUsersActivityInCourse(student, courseId)).thenReturn(points);

        Set<ExerciseWithPointsDto> result = exerciseService.getList(lessonId);

        assertThat(result).hasSize(1);
        ExerciseWithPointsDto dto = result.iterator().next();
        assertThat(dto.getApprovedStudentsPoints()).isEqualTo(10.0);
        verify(pointService).getUsersActivityInCourse(student, courseId);
    }

    @Test
    void getList_HandlesZeroPoints() {
        Exercise exercise = createExerciseWithApprovedStudent(1, null, student);

        when(exerciseRepository.findByLesson_Id(lessonId)).thenReturn(Set.of(exercise));
        when(pointService.getUsersActivityInCourse(student, courseId)).thenReturn(List.of());

        Set<ExerciseWithPointsDto> result = exerciseService.getList(lessonId);

        assertThat(result).hasSize(1);
        ExerciseWithPointsDto dto = result.iterator().next();
        assertThat(dto.getApprovedStudentsPoints()).isZero();
    }

    @Test
    void getList_HandlesMultipleExercisesWithDifferentStudents() {
        String student1Id = UUID.randomUUID().toString();
        String student2Id = UUID.randomUUID().toString();

        User student1 = new User();
        student1.setId(student1Id);
        User student2 = new User();
        student2.setId(student2Id);

        Exercise ex1 = createExerciseWithApprovedStudent(1, null, student1);
        Exercise ex2 = createExerciseWithApprovedStudent(2, null, student2);

        List<PointDto> points1 = List.of(createPointDto(5.0), createPointDto(3.0));
        List<PointDto> points2 = List.of(createPointDto(7.0));

        when(exerciseRepository.findByLesson_Id(lessonId)).thenReturn(Set.of(ex1, ex2));
        when(pointService.getUsersActivityInCourse(student1, courseId)).thenReturn(points1);
        when(pointService.getUsersActivityInCourse(student2, courseId)).thenReturn(points2);

        Set<ExerciseWithPointsDto> result = exerciseService.getList(lessonId);

        assertThat(result).hasSize(2);
        verify(pointService).getUsersActivityInCourse(student1, courseId);
        verify(pointService).getUsersActivityInCourse(student2, courseId);

        Map<Integer, Double> pointsByExercise = result.stream()
                .collect(HashMap::new,
                        (map, dto) -> map.put(dto.getExerciseNumber(), dto.getApprovedStudentsPoints()),
                        HashMap::putAll);

        assertThat(pointsByExercise.get(1)).isEqualTo(8.0);
        assertThat(pointsByExercise.get(2)).isEqualTo(7.0);
    }

    private Exercise createExercise(Integer number, String subpoint) {
        Exercise exercise = new Exercise();
        exercise.setId(UUID.randomUUID());
        exercise.setExerciseNumber(number);
        exercise.setSubpoint(subpoint);

        Lesson lesson = new Lesson();
        lesson.setId(lessonId);
        exercise.setLesson(lesson);

        Course course = new Course();
        course.setId(courseId);
        exercise.getLesson().setCourse(course);

        return exercise;
    }

    private Exercise createExerciseWithApprovedStudent(Integer number, String subpoint, User student) {
        Exercise exercise = createExercise(number, subpoint);
        exercise.setApprovedStudent(student);

        return exercise;
    }

    private PointDto createPointDto(Double value) {
        PointDto dto = new PointDto();
        dto.setActivityValue(value);
        return dto;
    }
}