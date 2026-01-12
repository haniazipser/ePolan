package com.example.ePolan;

import com.example.ePolan.Model.Dtos.ExerciseWithPointsDto;
import com.example.ePolan.Model.Dtos.LessonDescriptionDto;
import com.example.ePolan.Services.filegenerator.DocumentFormat;
import com.example.ePolan.Services.filegenerator.DocumentGenerator;
import com.example.ePolan.Services.filegenerator.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentServiceTest {

    @Mock
    private DocumentGenerator pdfGenerator;

    private DocumentService documentService;

    private LessonDescriptionDto lesson;
    private List<ExerciseWithPointsDto> byExercise;
    private List<ExerciseWithPointsDto> byPoints;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        documentService = new DocumentService(Map.of(DocumentFormat.PDF, pdfGenerator));

        lesson = new LessonDescriptionDto();
        byExercise = List.of();
        byPoints = List.of();
    }

    @Test
    void testCreateDocument_delegatesToPdfGenerator() throws Exception {

        when(pdfGenerator.generateDocument(byExercise, byPoints, lesson)).thenReturn("document.pdf");

        String result = documentService.createDocument(DocumentFormat.PDF, byExercise, byPoints, lesson);


        assertEquals("document.pdf", result);


        verify(pdfGenerator).generateDocument(byExercise, byPoints, lesson);
    }

    @Test
    void testCreateDocument_unsupportedFormat_throwsException() {

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                documentService.createDocument(DocumentFormat.CSV, byExercise, byPoints, lesson)
        );

        assertTrue(ex.getMessage().contains("Unsupported format"));
    }
}
