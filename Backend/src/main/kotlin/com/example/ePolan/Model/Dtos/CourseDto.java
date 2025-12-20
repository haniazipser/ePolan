package com.example.ePolan.Model.Dtos;

import com.example.ePolan.Model.Entities.Course;
import com.example.ePolan.Model.Entities.LessonTime;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter @Setter
public class CourseDto {
    private UUID id;
    private String name;
    private String  instructor;
    private UserDto creator;
    private List<LessonTime> lessonTimes;
    private List<LessonDto> lessons;
    private Instant startDate;
    private Instant endDate;
    private Integer frequency;
    private String courseCode;
    public CourseDto(Course course){
        this.id = course.getId();
        this.name = course.getName();
        this.instructor = course.getInstructor();
        this.creator = new UserDto( course.getCreator());
        this.lessonTimes = course.getLessonTimes().stream().toList();
        if (course.getLessons() != null) {
            this.lessons = course.getLessons().stream().map(s -> new LessonDto(s)).sorted(Comparator.comparing(LessonDto::getClassDate)).collect(Collectors.toList());
        }
        this.endDate = course.getEndDate();
        this.startDate = course.getStartDate();
        this.frequency = course.getFrequency();
        this.courseCode = course.getCourseCode();
    }


}
