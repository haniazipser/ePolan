package com.example.ePolan.Services;

import com.example.ePolan.Model.Dtos.CourseDto;
import com.example.ePolan.Services.messagesender.EmailMessageSender;
import com.example.ePolan.Services.messagesender.GroupInvitationMessage;
import com.example.ePolan.Services.messagesender.Message;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseApplicationService {
    private final CourseService groupClassService;
    private final EmailMessageSender emailSender;

    public void addStudentToGroup( String email, UUID courseId) {

        groupClassService.addStudentToGroup(email, courseId);
        CourseDto group = groupClassService.getGroupInfo(courseId);

        Message message = new GroupInvitationMessage(
                emailSender,
                group,
                courseId
        );

        try {
            message.send(email);
        } catch (Exception e) {
            System.out.println("Error sending invitation");
        }
    }
}
