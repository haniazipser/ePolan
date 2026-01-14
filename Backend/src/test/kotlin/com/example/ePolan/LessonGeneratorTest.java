package com.example.ePolan;

import com.example.ePolan.Model.Entities.Course;
import com.example.ePolan.Model.Entities.Lesson;
import com.example.ePolan.Model.Entities.LessonTime;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LessonGeneratorTest {

    @Test
    void testGenerateLessons() {//blad tutaj

        Course course = new Course();
        course.setFrequency(1);

        LessonTime lessonTime = new LessonTime();
        lessonTime.setDayOfWeek(DayOfWeek.WEDNESDAY);
        course.setLessonTimes(Set.of(lessonTime));

        course.setStartDate(Instant.parse("2026-01-07T10:00:00Z"));
        course.setEndDate(Instant.parse("2026-02-01T00:00:00Z"));

        LessonGenerator generator = new LessonGenerator();

        Set<Lesson> lessons = generator.generateLessons(course);


        assertEquals(3, lessons.size());

        for (Lesson lesson : lessons) {
            assertTrue(lesson.getClassDate().isBefore(course.getEndDate()));
            assertEquals(course, lesson.getCourse());
            assertNotNull(lesson.getLessonExercises());
        }
    }
}
