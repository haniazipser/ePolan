package com.example.ePolan.Services;

import com.example.ePolan.Model.Dtos.CourseDto;
import com.example.ePolan.Services.messagesender.EmailMessageSender;
import com.example.ePolan.Services.messagesender.GroupInvitationMessage;
import com.example.ePolan.Services.messagesender.Message;
import com.example.ePolan.Services.messagesender.MessageFactory;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CourseApplicationService {
    private final CourseService courseService;
    private final MessageFactory messageFactory;

    public void addStudentToGroup( String email, UUID courseId) {

        courseService.addStudentToGroup(email, courseId);
        CourseDto group = courseService.getGroupInfo(courseId);

        GroupInvitationMessage message = messageFactory.createGroupInvitationMessage(group, courseId);

        try {
            message.send(email);
        } catch (Exception e) {
            log.error("Error sending invitation: {}", e);
        }
    }
}
