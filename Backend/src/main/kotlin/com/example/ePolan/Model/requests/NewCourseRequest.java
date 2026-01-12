package com.example.ePolan.Model.requests;

import com.example.ePolan.Model.Entities.LessonTime;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class NewCourseRequest {
    private UUID id;
    private String name;
    @Email
    private String  instructor;
    private Set<LessonTime> lessonTimes;
    private Set<String> students;
    private Instant endDate;
    private Instant startDate;
    private Integer frequency;
    public NewCourseRequest(){}
}
