package com.example.ePolan;

import com.example.ePolan.Model.Entities.Course;
import com.example.ePolan.Model.Entities.Lesson;
import com.example.ePolan.Model.Entities.LessonTime;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class LessonGenerator {

    public Set<Lesson> generateLessons(Course course) {
        Set<Lesson> lessons = new HashSet<>();
        Instant endDate = course.getEndDate();

        for (LessonTime lessonTime : course.getLessonTimes()) {
            Instant next = course.getFirstLesson(lessonTime);

            while (next.isBefore(endDate)) {
                Lesson lesson = new Lesson();
                lesson.setClassDate(next);
                lesson.setCourse(course);
                lesson.setLessonExercises(Collections.emptySet());
                lessons.add(lesson);

                next = next.plus(7L * course.getFrequency(), ChronoUnit.DAYS);
            }
        }
        return lessons;
    }
}