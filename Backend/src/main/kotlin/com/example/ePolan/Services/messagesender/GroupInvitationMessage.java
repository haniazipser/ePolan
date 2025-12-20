package com.example.ePolan.Services.messagesender;

import com.example.ePolan.Model.Dtos.CourseDto;

import java.util.UUID;

public class GroupInvitationMessage extends Message {

    private final CourseDto course;
    private final UUID courseId;

    public GroupInvitationMessage(
            MessageSender sender,
            CourseDto course,
            UUID courseId
    ) {
        super(sender);
        this.course = course;
        this.courseId = courseId;
    }

    @Override
    public void send(String recipient) throws Exception {
        String subject = "New group alert";

        String link = "tutti://group/" + courseId;

        String body = """
            You have been added to <b>%s</b> course!

            If you accept the invitation, use this code:

            %s

            in your <b>ePolan</b> application to join the team.

            Link: %s
            """.formatted(
                course.getName(),
                course.getCourseCode(),
                link
        );

        sender.send(recipient, subject, body, null);
    }
}