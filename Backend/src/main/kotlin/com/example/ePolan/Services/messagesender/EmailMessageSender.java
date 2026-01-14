package com.example.ePolan.Services.messagesender;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;

@Service
@RequiredArgsConstructor
public class EmailMessageSender implements MessageSender {

    private final JavaMailSender mailSender;

    @Override
    public void send(String recipient, String subject, String body, File attachment) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(recipient);
        helper.setSubject(subject);
        helper.setText(buildHtmlContent(subject, body), true);

        if (attachment != null) {
            if (attachment.exists()) {
                helper.addAttachment(attachment.getName(), attachment);
            } else {
                throw new FileNotFoundException("Attachment not found: " + attachment.getAbsolutePath());
            }
        }

        mailSender.send(message);
    }

    private String buildHtmlContent(String title, String body) {
        return """
            <html>
            <head>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        background-color: #f4f4f4;
                        padding: 20px;
                    }
                    .container {
                        max-width: 600px;
                        margin: auto;
                        background: #fff;
                        padding: 20px;
                        border-radius: 8px;
                        box-shadow: 0 2px 5px rgba(0,0,0,0.1);
                    }
                    .header {
                        font-size: 20px;
                        font-weight: bold;
                        margin-bottom: 20px;
                        color: #333;
                    }
                    .content {
                        font-size: 16px;
                        line-height: 1.5;
                        color: #444;
                    }
                    .footer {
                        margin-top: 30px;
                        font-size: 12px;
                        color: #999;
                        text-align: center;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">%s</div>
                    <div class="content">%s</div>
                    <div class="footer">This is an automated message. Please do not reply.</div>
                </div>
            </body>
            </html>
            """.formatted(title, body.replace("\n", "<br>"));
    }
}
