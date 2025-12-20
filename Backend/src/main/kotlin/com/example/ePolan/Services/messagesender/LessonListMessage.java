package com.example.ePolan.Services.messagesender;

import com.example.ePolan.Model.Dtos.LessonDescriptionDto;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LessonListMessage extends Message {

    private final LessonDescriptionDto lesson;
    private final String filename;

    public LessonListMessage(MessageSender sender, LessonDescriptionDto lesson, String filename) {
        super(sender);
        this.lesson = lesson;
        this.filename = filename;
    }

    @Override
    public void send(String recipient) throws Exception {
        LocalDate date = LocalDate.from(lesson.getClassDate());
        String formattedDate = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        String subject = "Exercise List for " + lesson.getCourseName() + " – " + formattedDate;
        String body = "Dear Professor,\nYou'll find the exercise list for the class scheduled on "
                + formattedDate + " attached.\nKind regards, ePolan Team";
        File attachment = new File(filename);
        sender.send(recipient, subject, body, attachment);
    }
}