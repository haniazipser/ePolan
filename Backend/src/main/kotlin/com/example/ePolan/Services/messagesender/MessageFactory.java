package com.example.ePolan.Services.messagesender;


import com.example.ePolan.Model.Dtos.CourseDto;
import com.example.ePolan.Model.Dtos.LessonDescriptionDto;
import com.example.ePolan.Model.Entities.Lesson;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.UUID;
@Component
public class MessageFactory {

    private final MessageSender sender;

    public MessageFactory(MessageSender sender) {
        this.sender = sender;
    }

    public LessonListMessage createLessonListMessage(LessonDescriptionDto lesson, String filename) {
        LessonListMessage message = new LessonListMessage(sender, lesson, filename);
        return message;
    }

    public GroupInvitationMessage createGroupInvitationMessage(CourseDto course, UUID courseId) {
        GroupInvitationMessage message = new GroupInvitationMessage(sender, course, courseId);
        return message;
    }

    public AdminErrorMessage createAdminErrorMessage(RuntimeException exception, Lesson lesson) {
        AdminErrorMessage message = new AdminErrorMessage(sender, exception, lesson);
        return message;
    }
}
