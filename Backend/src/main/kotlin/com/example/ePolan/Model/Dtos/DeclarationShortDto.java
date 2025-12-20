package com.example.ePolan.Model.Dtos;

import com.example.ePolan.Model.Entities.ExerciseDeclaration;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
public class DeclarationShortDto {
    private UserDto student;
    private UUID exerciseId;
    private Double pointsInCourse;

    public DeclarationShortDto(ExerciseDeclaration exerciseDeclaration, Double points){
        this.student = new UserDto(exerciseDeclaration.getStudent());
        this.exerciseId = exerciseDeclaration.getExercise().getId();
        this.pointsInCourse = points;
    }
}
