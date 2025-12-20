package com.example.ePolan.Model.Dtos;

import com.example.ePolan.Model.Entities.DeclarationStatus;
import com.example.ePolan.Model.Entities.ExerciseDeclaration;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter
public class DeclarationDto {
    private UUID id;
    private Instant declarationDate;

    private DeclarationStatus declarationStatus;

    private ExerciseDto exercise;

    private UserDto student;

    public DeclarationDto(ExerciseDeclaration declaration){
        this.id = declaration.getId();
        this.declarationDate = declaration.getDeclarationDate();
        this.declarationStatus = declaration.getDeclarationStatus();
        this.exercise = new ExerciseDto(declaration.getExercise());
        this.student = new UserDto(declaration.getStudent());
    }
}
