package com.example.ePolan;


import com.example.ePolan.Model.Dtos.ExerciseDto;
import com.example.ePolan.Model.Dtos.LessonDescriptionDto;
import com.example.ePolan.Model.Dtos.LessonDto;
import com.example.ePolan.Model.Entities.Course;
import com.example.ePolan.Model.Entities.Exercise;
import com.example.ePolan.Model.Entities.Lesson;
import com.example.ePolan.Model.Entities.Participant;
import com.example.ePolan.Model.Entities.User;
import com.example.ePolan.Repositories.CourseRepository;
import com.example.ePolan.Repositories.ExerciseRepository;
import com.example.ePolan.Repositories.LessonRepository;
import com.example.ePolan.Services.LessonService;
import com.example.ePolan.Services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class LessonServiceTest {
    @Mock

    private LessonRepository lessonRepository;
    @Mock
    private ExerciseRepository exerciseRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private UserService userService;
    @InjectMocks
    private LessonService lessonService;

    @BeforeEach
    void setUp() {

    }

    @Test
    void testAddNewLessonSuccess() {
        UUID courseId = UUID.randomUUID();
        Instant date = Instant.now().plusSeconds(3600);
        Course course = new Course();
        course.setId(courseId);
        course.setName("test");

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(i -> i.getArguments()[0]);

        LessonDto result = lessonService.addNewLesson(courseId, date);

        assertNotNull(result);
        assertEquals("test", result.getCourseName());
        verify(lessonRepository, times(1)).save(any(Lesson.class));
    }

    @Test
    void testAddNewLessonCourseNotFound() {
        UUID courseId = UUID.randomUUID();
        Instant date = Instant.now();

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> lessonService.addNewLesson(courseId, date));
    }

    @Test
    void testDeleteLessonSuccess() {
        UUID lessonId = UUID.randomUUID();
        Lesson lesson = new Lesson();
        lesson.setClassDate(Instant.now().plusSeconds(3600));

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        lessonService.deleteLesson(lessonId);

        verify(lessonRepository, times(1)).delete(lesson);
    }

    @Test
    void testDeleteLessonNotFound() {
        UUID lessonId = UUID.randomUUID();
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> lessonService.deleteLesson(lessonId));
    }

    @Test
    void testDeleteLessonFromPast() {
        UUID lessonId = UUID.randomUUID();
        Lesson lesson = new Lesson();
        lesson.setClassDate(Instant.now().minusSeconds(3600));
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        assertThrows(ResponseStatusException.class,
                () -> lessonService.deleteLesson(lessonId));
    }

    @Test
    void testGetLessonsForCourseSuccess() {
        UUID courseId = UUID.randomUUID();
        User user = new User();
        user.setId("1234");
        Course course = new Course();
        course.setId(courseId);

        Participant participant = new Participant();
        participant.setStudent(user);
        participant.setCourse(course);
        course.setStudents(new HashSet<>(Collections.singleton(participant)));


        when(userService.getLoggedUser()).thenReturn(user);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));


        Lesson lesson1 = new Lesson();
        lesson1.setClassDate(Instant.now().plusSeconds(1000));
        Lesson lesson2 = new Lesson();
        lesson2.setClassDate(Instant.now().plusSeconds(2000));

        lesson1.setCourse(course);
        lesson2.setCourse(course);

        when(lessonRepository.findByCourse(course)).thenReturn(Arrays.asList(lesson2, lesson1));

        List<LessonDto> lessons = lessonService.getLessonsForCourse(courseId);

        assertEquals(2, lessons.size());
        assertTrue(lessons.get(0).getClassDate().isBefore(lessons.get(1).getClassDate()));
    }

    @Test
    void testUpdateExercisesForLessonSuccess() {
        UUID lessonId = UUID.randomUUID();
        Lesson lesson = new Lesson();
        lesson.setId(lessonId);

        LessonDto lessonDto = new LessonDto();
        lessonDto.setId(lessonId);
        ExerciseDto exDto = new ExerciseDto();
        exDto.setExerciseNumber(1);
        exDto.setSubpoint("a");
        lessonDto.setExercises(Collections.singletonList(exDto));

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(exerciseRepository.findByLesson(lesson)).thenReturn(Collections.emptySet());
        when(exerciseRepository.save(any(Exercise.class))).thenAnswer(i -> i.getArguments()[0]);

        lessonService.updateExercisesForLesson(lessonDto);

        verify(exerciseRepository, times(1)).save(any(Exercise.class));
    }

//    @Test
//    void testUpdateExercisesForLessonWithDeclarations() {
//        UUID lessonId = UUID.randomUUID();
//        Lesson lesson = new Lesson();
//        lesson.setId(lessonId);
//        Exercise exercise = new Exercise();
//        exercise.setDeclarations(Collections.singleton(mock(Exercise.class)));
//
//        LessonDto lessonDto = new LessonDto();
//        lessonDto.setId(lessonId);
//        ExerciseDto exDto = new ExerciseDto();
//        lessonDto.setExercises(Collections.singletonList(exDto));
//
//        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
//        when(exerciseRepository.findByLesson(lesson)).thenReturn(Collections.singleton(exercise));
//
//        assertThrows(ResponseStatusException.class,
//                () -> lessonService.updateExercisesForLesson(lessonDto));
//    }

    @Test
    void testGetLessonInfoSuccess() {
        UUID lessonId = UUID.randomUUID();
        Lesson lesson = new Lesson();
        lesson.setId(lessonId);

        Course course = new Course();
        lesson.setCourse(course);
        lesson.setClassDate(Instant.now().plusSeconds(3600));

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        LessonDescriptionDto dto = lessonService.getLessonInfo(lessonId);
        assertNotNull(dto);
        assertEquals(lessonId, dto.getId());
    }

    @Test
    void testGetNextLessons() {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate tomorrow = LocalDate.now(zone).plusDays(1);
        Instant startOfDay = tomorrow.atStartOfDay(zone).toInstant();
        Instant endOfDay = tomorrow.plusDays(1).atStartOfDay(zone).toInstant();

        List<UUID> ids = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        when(lessonRepository.findLessonIdsByClassDateBetween(startOfDay, endOfDay)).thenReturn(ids);

        List<UUID> result = lessonService.getNextLessons();
        assertEquals(ids, result);
    }
}
