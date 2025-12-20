package com.example.ePolan.Services.messagesender;

import com.example.ePolan.Model.Entities.Lesson;

public class AdminErrorMessage extends Message {

    private final RuntimeException exception;
    private final Lesson lesson;

    public AdminErrorMessage(
            MessageSender sender,
            RuntimeException exception,
            Lesson lesson
    ) {
        super(sender);
        this.exception = exception;
        this.lesson = lesson;
    }

    @Override
    public void send(String recipient) throws Exception {
        String subject = "Error sending exercise list";

        String body = """
            An error occurred while sending the exercise list.

            Lesson ID: %s
            Course: %s
            Date: %s

            Error message:
            %s
            """.formatted(
                lesson.getId(),
                lesson.getCourse().getName(),
                lesson.getClassDate(),
                exception.getMessage()
        );

        sender.send(recipient, subject, body, null);
    }
}