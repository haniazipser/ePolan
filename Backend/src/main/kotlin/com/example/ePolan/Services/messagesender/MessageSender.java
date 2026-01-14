package com.example.ePolan.Services.messagesender;

import java.io.File;

public interface MessageSender {
    void send(String recipient, String title, String body, File attachment) throws Exception;
}