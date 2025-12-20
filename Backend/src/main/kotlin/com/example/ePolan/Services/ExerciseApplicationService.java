package com.example.ePolan.Services;

import com.example.Projekt_IO.Model.Dtos.*;
import com.example.ePolan.Model.Dtos.ExerciseWithPointsDto;
import com.example.ePolan.Model.Dtos.LessonDescriptionDto;
import com.example.ePolan.Model.Entities.Exercise;
import com.example.ePolan.Model.Entities.Lesson;
import com.itextpdf.text.DocumentException;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class ExerciseApplicationService {
    private final EmailService emailService;
    private final FileService fileService;
    private final ExerciseService exerciseService;
    private final LessonService lessonService;
    @Retryable(retryFor = {MessagingException.class, FileNotFoundException.class, DocumentException.class})
    public void exportListToPdf(UUID lessonId){
        List<ExerciseWithPointsDto> exercises1 = exerciseService.getList(lessonId)
                .stream().sorted(Comparator.comparing(ExerciseWithPointsDto::getExerciseNumber).thenComparing(ExerciseWithPointsDto::getSubpoint))
                .collect(Collectors.toList());

        List<ExerciseWithPointsDto> exercises2 = exerciseService.getList(lessonId)
                .stream().sorted(Comparator.comparing(ExerciseWithPointsDto::getApprovedStudentsPoints))
                .collect(Collectors.toList());

        LessonDescriptionDto lesson = lessonService.getLessonInfo(lessonId);
        String name = null;
        try {
            name = fileService.createPDFList(exercises1, exercises2, lesson);
            emailService.sendMessageWithList(lesson.getInstructor(), lesson, name);
        } catch (MessagingException | FileNotFoundException | DocumentException e) {
            System.err.println("Error generating pdf or sending an email: " + e.getMessage());
            throw new RuntimeException(e);
        }finally {
            if (name != null) {
                try {
                    Files.deleteIfExists(Path.of(name));
                } catch (IOException e) {
                    System.err.println("Error deleting temp file: " + name);
                    e.printStackTrace();
                }
            }
        }

    }

    @Recover
    public void recover(RuntimeException  e, Lesson lesson, List<Exercise> exercises1, List<Exercise> exercises2) {
        System.err.println("Error sending list: " + e.getMessage());
        try{
            emailService.reportErrorToAdmin(e,lesson);
        }catch(MessagingException e1){
            System.err.println("Error notifying admin : " + e1.getMessage());
        }
    }
}
