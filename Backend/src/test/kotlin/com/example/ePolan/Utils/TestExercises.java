package com.example.ePolan.Utils;

import com.example.ePolan.Model.Entities.Exercise;
import com.example.ePolan.Model.Entities.Lesson;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TestExercises {
    public static Exercise exercise(UUID uuid, int exNum, Lesson lesson){
        Exercise res = new Exercise();
        res.setId(uuid);
        res.setExerciseNumber(exNum);
        res.setLesson(lesson);
        if(lesson.getLessonExercises() == null) lesson.setLessonExercises(new HashSet<>(Set.of(res)));
        else lesson.getLessonExercises().add(res);
        return res;
    }
}
