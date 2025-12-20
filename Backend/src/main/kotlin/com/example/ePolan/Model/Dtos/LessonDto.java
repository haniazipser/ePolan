package com.example.ePolan.Model.Dtos;

import com.example.ePolan.Model.Entities.Lesson;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter @Setter
public class LessonDto {
    private UUID id;
    private Instant classDate;
    private String courseName;
    private List<ExerciseDto> exercises;
    private LessonStatus status;
    public LessonDto(Lesson lesson){
        this.id = lesson.getId();
        this.classDate = lesson.getClassDate();
        this.courseName = lesson.getCourse().getName();
        if (lesson.getLessonExercises()!= null) {
            this.exercises = lesson.getLessonExercises().stream().map(e -> new ExerciseDto(e)).sorted(Comparator.comparing(ExerciseDto::getId)).collect(Collectors.toList());
        }
        this.status = lesson.getLessonStatus();
    }

    public LessonDto(){};
}
