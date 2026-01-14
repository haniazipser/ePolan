package com.example.ePolan.Model.Entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "keycloak_user")
public class User {
    @Id
    private String id;

    private String firstName;
    private String lastName;
    private String email;

    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Course> createdCourses;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Point> activity;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExerciseDeclaration> declarations;

    @OneToMany(mappedBy = "approvedStudent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Exercise> approvedExercises;
}