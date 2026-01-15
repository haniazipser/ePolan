package com.example.ePolan.Utils;

import com.example.ePolan.Model.Entities.Course;
import com.example.ePolan.Model.Entities.User;

import java.util.List;
import java.util.UUID;

public class TestCourses {

    public static Course course(String name, User creator) {
        Course c = new Course();
        c.setId(UUID.randomUUID());
        c.setName(name);
        c.setInstructor("Cooke");
        c.setCreator(creator);
        if(creator.getCreatedCourses() == null) creator.setCreatedCourses(List.of(c));
        else creator.getCreatedCourses().add(c);
        return c;
    }
}
