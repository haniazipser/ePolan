package com.example.ePolan.Services.messagesender;

public abstract class Message {
    protected final MessageSender sender;

    public Message(MessageSender sender) {
        this.sender = sender;
    }

    public abstract void send(String recipient) throws Exception;


}
