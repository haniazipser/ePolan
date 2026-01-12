package com.example.ePolan;

import com.example.ePolan.Model.Dtos.ExerciseWithPointsDto;
import com.example.ePolan.Model.Dtos.LessonDescriptionDto;
import com.example.ePolan.Services.ExerciseApplicationService;
import com.example.ePolan.Services.ExerciseService;
import com.example.ePolan.Services.LessonService;
import com.example.ePolan.Services.filegenerator.DocumentFormat;
import com.example.ePolan.Services.filegenerator.DocumentService;
import com.example.ePolan.Services.messagesender.EmailMessageSender;
import com.itextpdf.text.DocumentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseApplicationServiceTest {

    @Mock
    private EmailMessageSender emailSender;

    @Mock
    private DocumentService documentService;

    @Mock
    private ExerciseService exerciseService;

    @Mock
    private LessonService lessonService;

    @InjectMocks
    private ExerciseApplicationService service;

    private UUID lessonId;

    @Test
    void exportListToPdf_sendsEmailAndDeletesFile() throws Exception {
        lessonId = UUID.randomUUID();

        ExerciseWithPointsDto ex1 = dto(1, "b", 5);
        ExerciseWithPointsDto ex2 = dto(1, "a", 3);
        ExerciseWithPointsDto ex3 = dto(1, null, 10);

        Set<ExerciseWithPointsDto> exercises = Set.of(ex1, ex2, ex3);

        LessonDescriptionDto lesson = new LessonDescriptionDto();
        lesson.setInstructor("instructor@test.com");
        lesson.setClassDate(Instant.now());

        when(exerciseService.getList(lessonId)).thenReturn(exercises);
        when(lessonService.getLessonInfo(lessonId)).thenReturn(lesson);
        when(documentService.createDocument(
                eq(DocumentFormat.PDF),
                anyList(),
                anyList(),
                eq(lesson)
        )).thenReturn("test.pdf");

        service.exportListToPdf(lessonId);

        verify(documentService).createDocument(
                eq(DocumentFormat.PDF),
                anyList(),
                anyList(),
                eq(lesson)
        );


        verify(emailSender, atLeastOnce()).send(any(), any(), any(), any());


        assertThat(Files.exists(Path.of("test.pdf"))).isFalse();
    }

    @Test
    void exportListToPdf_sortsExercisesCorrectly() throws Exception {
        lessonId = UUID.randomUUID();

        ExerciseWithPointsDto ex1 = dto(1, "b", 5);
        ExerciseWithPointsDto ex2 = dto(1, "a", 3);
        ExerciseWithPointsDto ex3 = dto(1, null, 10);

        when(exerciseService.getList(lessonId))
                .thenReturn(Set.of(ex1, ex2, ex3));

        LessonDescriptionDto lesson = new LessonDescriptionDto();
        lesson.setInstructor("i@test.com");
        lesson.setClassDate(Instant.now());

        when(lessonService.getLessonInfo(lessonId)).thenReturn(lesson);
        when(documentService.createDocument(any(), any(), any(), any()))
                .thenReturn("test.pdf");


        service.exportListToPdf(lessonId);

        verify(documentService).createDocument(
                eq(DocumentFormat.PDF),
                anyList(),
                anyList(),
                eq(lesson)
        );

    }


    @Test
    void exportListToPdf_whenDocumentFails_throwsRuntimeException() throws Exception {
        lessonId = UUID.randomUUID();

        when(exerciseService.getList(lessonId))
                .thenReturn(Set.of(dto(1, null, 1)));

        LessonDescriptionDto lesson = new LessonDescriptionDto();
        lesson.setInstructor("i@test.com");
        lesson.setClassDate(Instant.now());

        when(lessonService.getLessonInfo(lessonId)).thenReturn(lesson);

        when(documentService.createDocument(any(), any(), any(), any()))
                .thenThrow(new DocumentException("boom"));

        assertThatThrownBy(() -> service.exportListToPdf(lessonId))
                .isInstanceOf(RuntimeException.class);
    }

    private ExerciseWithPointsDto dto(int number, String subpoint, double points) {
        ExerciseWithPointsDto dto = new ExerciseWithPointsDto();
        dto.setExerciseNumber(number);
        dto.setSubpoint(subpoint);
        dto.setApprovedStudentsPoints(points);
        return dto;
    }
}



