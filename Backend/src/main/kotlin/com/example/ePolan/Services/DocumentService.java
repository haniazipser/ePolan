package com.example.ePolan.Services;

import com.example.ePolan.Model.Dtos.ExerciseWithPointsDto;
import com.example.ePolan.Model.Dtos.LessonDescriptionDto;
import com.example.ePolan.Services.filegenerator.DocumentFormat;
import com.example.ePolan.Services.filegenerator.DocumentGenerator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final Map<DocumentFormat, DocumentGenerator> generators;

    public String createDocument(DocumentFormat format,
                                 List<ExerciseWithPointsDto> byExercise,
                                 List<ExerciseWithPointsDto> byPoints,
                                 LessonDescriptionDto lesson) throws Exception {

        DocumentGenerator generator = generators.get(format);
        if (generator == null) {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }

        return generator.generateDocument(byExercise, byPoints, lesson);
    }
}