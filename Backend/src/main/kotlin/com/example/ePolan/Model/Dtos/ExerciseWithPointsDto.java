package com.example.ePolan.Model.Dtos;

import com.example.ePolan.Model.Entities.Exercise;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;
@Getter @Setter
@NoArgsConstructor
public class ExerciseWithPointsDto {
    private UUID id;
    private Integer exerciseNumber;
    private String subpoint;
    private UserDto approvedStudent;
    private Double approvedStudentsPoints;
    public ExerciseWithPointsDto(Exercise exercise, Double points){
        this.id=exercise.getId();
        this.exerciseNumber = exercise.getExerciseNumber();
        this.subpoint = exercise.getSubpoint();
        this.approvedStudent = new UserDto(exercise.getApprovedStudent());
        this.approvedStudentsPoints = points;
    }
}
