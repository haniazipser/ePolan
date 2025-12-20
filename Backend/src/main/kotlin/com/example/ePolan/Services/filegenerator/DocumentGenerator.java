package com.example.ePolan.Services.filegenerator;

import com.example.ePolan.Model.Dtos.ExerciseWithPointsDto;
import com.example.ePolan.Model.Dtos.LessonDescriptionDto;

import java.util.List;

public interface DocumentGenerator {
    String generateDocument(List<ExerciseWithPointsDto> byExercise,
                            List<ExerciseWithPointsDto> byPoints,
                            LessonDescriptionDto lesson) throws Exception;

    String getFileExtension();
}
