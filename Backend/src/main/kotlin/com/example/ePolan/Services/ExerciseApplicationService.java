package com.example.ePolan.Services;

import com.example.ePolan.Model.Dtos.ExerciseWithPointsDto;
import com.example.ePolan.Model.Dtos.LessonDescriptionDto;
import com.example.ePolan.Model.Entities.Exercise;
import com.example.ePolan.Model.Entities.Lesson;
import com.example.ePolan.Services.filegenerator.DocumentFormat;
import com.example.ePolan.Services.filegenerator.DocumentService;
import com.example.ePolan.Services.messagesender.AdminErrorMessage;
import com.example.ePolan.Services.messagesender.EmailMessageSender;
import com.example.ePolan.Services.messagesender.LessonListMessage;
import com.example.ePolan.Services.messagesender.Message;
import com.itextpdf.text.DocumentException;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
@Transactional
@RequiredArgsConstructor
public class ExerciseApplicationService {
    @Value("${app.admin-email}")
    private String adminEmail;

    private final EmailMessageSender emailSender;
    private final DocumentService documentService;
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
        String filename = null;
        try {
            filename = documentService.createDocument(DocumentFormat.PDF, exercises1, exercises2, lesson);
            Message emailMessage = new LessonListMessage(emailSender, lesson, filename);
            emailMessage.send(lesson.getInstructor());
        } catch (MessagingException e ) {
            System.err.println("Error sending an student: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (Exception e){
            System.err.println("Error generating document: " + e.getMessage());
            throw new RuntimeException(e);
        }finally {
            if (filename != null) {
                try {
                    Files.deleteIfExists(Path.of(filename));
                } catch (IOException e) {
                    System.err.println("Error deleting temp file: " + filename);
                    e.printStackTrace();
                }
            }
        }

    }

    @Recover
    public void recover(RuntimeException  e, Lesson lesson, List<Exercise> exercises1, List<Exercise> exercises2) {
        System.err.println("Error sending list: " + e.getMessage());
        try{
            Message message = new AdminErrorMessage(emailSender, e, lesson);
            message.send(adminEmail);
        }catch(Exception e1){
            System.err.println("Error notifying admin : " + e1.getMessage());
        }
    }
}
