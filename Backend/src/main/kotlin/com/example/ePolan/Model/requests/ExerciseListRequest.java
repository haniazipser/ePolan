package com.example.ePolan.Model.requests;

import com.example.ePolan.Model.Dtos.ExerciseDto;

import java.util.List;

public record ExerciseListRequest(List<ExerciseRequest> exercises) {
}
