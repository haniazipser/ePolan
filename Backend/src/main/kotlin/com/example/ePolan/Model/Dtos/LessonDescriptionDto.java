package com.example.ePolan.Model.Dtos;

import com.example.ePolan.Model.Entities.Lesson;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor
public class LessonDescriptionDto {
    private UUID id;
    private Instant classDate;
    private String courseName;
    private LessonStatus status;
    private String instructor;
    public LessonDescriptionDto(Lesson lesson){
        this.id = lesson.getId();
        this.classDate = lesson.getClassDate();
        this.courseName = lesson.getCourse().getName();
        this.status = lesson.getLessonStatus();
        this.instructor = lesson.getCourse().getInstructor();
    }
}
