package com.example.ePolan;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.ePolan.Model.Dtos.PointDto;
import com.example.ePolan.Model.Entities.Course;
import com.example.ePolan.Model.Entities.Point;
import com.example.ePolan.Model.Entities.Lesson;
import com.example.ePolan.Model.Entities.User;
import com.example.ePolan.Repositories.PointRepository;
import com.example.ePolan.Repositories.LessonRepository;
import com.example.ePolan.Services.PointService;
import com.example.ePolan.Services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.web.server.ResponseStatusException;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class PointServiceTest {

    @Mock
    private PointRepository pointRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private PointService pointService;

    private User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId("user1");
    }

    @Test
    void getUsersActivity_ReturnsPointDtos() {
        when(userService.getLoggedUser()).thenReturn(user);

        Point p1 = new Point();
        p1.setStudent(user);
        p1.setActivityValue(1.0);
        Point p2 = new Point();
        p2.setStudent(user);
        p2.setActivityValue(2.0);
        Course course = new Course();
        course.setName("Test Course");

        Lesson lesson1 = new Lesson();
        lesson1.setClassDate(Instant.now());
        lesson1.setCourse(course);

        Lesson lesson2 = new Lesson();
        lesson2.setClassDate(Instant.now().plusSeconds(10));
        lesson2.setCourse(course);

        p1.setLesson(lesson1);
        p2.setLesson(lesson2);

        when(pointRepository.findByStudent(user)).thenReturn(Set.of(p1, p2));

        List<PointDto> result = pointService.getUsersActivity();

        assertThat(result)
                .hasSize(2)
                .extracting(PointDto::getActivityValue)
                .containsExactlyInAnyOrder(1.0, 2.0);
    }

    @Test
    void getLoggedUserActivityInCourse_DelegatesToGetUsersActivityInCourse() {
        UUID courseId = UUID.randomUUID();
        when(userService.getLoggedUser()).thenReturn(user);

        Point point = new Point();
        point.setActivityValue(3.0);
        Lesson lesson = new Lesson();
        lesson.setClassDate(new Date().toInstant());
        Course course =new Course();
        lesson.setCourse(course);
        point.setLesson(lesson);
        point.setStudent(user);

        when(pointRepository.findByStudentAndLesson_Course_Id(user, courseId))
                .thenReturn(Set.of(point));

        List<PointDto> result = pointService.getLoggedUserActivityInCourse(courseId);

        assertThat(result)
                .hasSize(1)
                .extracting(PointDto::getActivityValue)
                .containsExactlyInAnyOrder(3.0);
    }

    @Test
    void addStudentActivity_CreatesNewPointIfNotExist() {
        UUID lessonId = UUID.randomUUID();
        Double value = 5.0;

        when(userService.getLoggedUser()).thenReturn(user);
        when(pointRepository.findByLesson_IdAndStudent(lessonId, user)).thenReturn(Optional.empty());

        Lesson lesson = new Lesson();
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        pointService.addStudentActivity(lessonId, value);

        ArgumentCaptor<Point> captor = ArgumentCaptor.forClass(Point.class);
        verify(pointRepository).save(captor.capture());

        Point saved = captor.getValue();
        assertEquals(user, saved.getStudent());
        assertEquals(value, saved.getActivityValue());
        assertEquals(lesson, saved.getLesson());
    }

    @Test
    void addStudentActivity_UpdatesExistingPoint() {
        UUID lessonId = UUID.randomUUID();
        Double value = 10.0;

        when(userService.getLoggedUser()).thenReturn(user);

        Point existing = new Point();
        existing.setActivityValue(3.0);
        when(pointRepository.findByLesson_IdAndStudent(lessonId, user)).thenReturn(Optional.of(existing));

        pointService.addStudentActivity(lessonId, value);

        assertEquals(value, existing.getActivityValue());
        verify(pointRepository).save(existing);
    }

    @Test
    void addStudentActivity_LessonNotFound_ThrowsException() {
        UUID lessonId = UUID.randomUUID();
        Double value = 2.0;

        when(userService.getLoggedUser()).thenReturn(user);
        when(pointRepository.findByLesson_IdAndStudent(lessonId, user)).thenReturn(Optional.empty());
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> pointService.addStudentActivity(lessonId, value));

        assertEquals("404 NOT_FOUND \"Lesson not found\"", ex.getMessage());
    }
}
