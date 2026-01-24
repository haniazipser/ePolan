package com.example.ePolan.Utils;

import com.example.ePolan.Model.Entities.Course;
import com.example.ePolan.Model.Entities.Exercise;
import com.example.ePolan.Model.Entities.Lesson;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TestLessons {
    public static Lesson lesson(UUID uuid, Course course, Instant classDate){
        Lesson res = new Lesson();
        res.setId(uuid);
        res.setLessonExercises(null);
        res.setClassDate(classDate);
        res.setCourse(course);
        if(course.getLessons() == null) { course.setLessons(new HashSet<>(Set.of(res)));}
        else course.getLessons().add(res);
        return res;
    }
}
