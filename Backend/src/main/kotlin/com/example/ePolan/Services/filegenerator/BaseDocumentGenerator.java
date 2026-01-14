package com.example.ePolan.Services.filegenerator;

import com.example.ePolan.Model.Dtos.ExerciseWithPointsDto;
import com.example.ePolan.Model.Dtos.LessonDescriptionDto;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

abstract class BaseDocumentGenerator implements DocumentGenerator {

    @Override
    public final String generateDocument(List<ExerciseWithPointsDto> byExercise,
                                         List<ExerciseWithPointsDto> byPoints,
                                         LessonDescriptionDto lesson) throws Exception {

        String filename = createFilename(lesson);

        initializeDocument(filename);

        addHeader(lesson);
        addByPointsSection(byPoints);
        startNewPage();
        addHeader(lesson);
        addByExercisesSection(byExercise);

        closeDocument();

        return filename;
    }

    protected abstract void initializeDocument(String filename) throws Exception;
    protected abstract void addHeader(LessonDescriptionDto lesson) throws Exception;
    protected abstract void addByPointsSection(List<ExerciseWithPointsDto> exercises) throws Exception;
    protected abstract void addByExercisesSection(List<ExerciseWithPointsDto> exercises) throws Exception;
    protected abstract void startNewPage() throws Exception;
    protected abstract void closeDocument() throws Exception;

    protected String createFilename(LessonDescriptionDto lesson) {
        LocalDate date = LocalDate.from(lesson.getClassDate().atZone(ZoneId.systemDefault()));
        String courseName = lesson.getCourseName().replace(' ', '_');
        return "List_" + courseName + "_" + date + "." + getFileExtension();
    }
}