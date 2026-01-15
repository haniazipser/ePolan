package com.example.ePolan.Utils;

import com.example.ePolan.Model.Dtos.DeclarationDto;
import com.example.ePolan.Model.Dtos.DeclarationShortDto;
import com.example.ePolan.Model.Entities.DeclarationStatus;
import com.example.ePolan.Model.Entities.Exercise;
import com.example.ePolan.Model.Entities.ExerciseDeclaration;
import com.example.ePolan.Model.Entities.User;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TestDeclarations {
    public static ExerciseDeclaration exerciseDeclaration(UUID uuid, User student, Exercise exercise){
        ExerciseDeclaration res = new ExerciseDeclaration();
        res.setId(uuid);
        res.setExercise(exercise);
        res.setStudent(student);
        if(exercise.getDeclarations() == null)exercise.setDeclarations(new HashSet<>(Set.of(res)));
        else exercise.getDeclarations().add(res);
        return res;
    }
}
