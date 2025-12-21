package com.example.ePolan;

import com.example.ePolan.Model.Dtos.NewCourseDto;
import com.example.ePolan.Model.Entities.Course;
import com.example.ePolan.Model.Entities.User;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CourseGenerator {
    public Course create(NewCourseDto dto, User creator) {
        Course course = new Course();
        course.setCreator(creator);
        course.setLessonTimes(dto.getLessonTimes());
        course.setName(dto.getName());
        course.setInstructor(dto.getInstructor());
        course.setStartDate(dto.getStartDate());
        course.setEndDate(dto.getEndDate());
        course.setFrequency(dto.getFrequency());
        course.setCourseCode(UUID.randomUUID().toString());
        return course;
    }
}