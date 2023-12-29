package com.zileanstdio.chatapp.Data.model;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.Objects;

public class Message {
    private String sender;
    private String type;
    private String message;
    private Date sendAt;

    public Message() {
    }

    public Message(String sender, String type, String message, Date sendAt) {
        this.sender = sender;
        this.type = type;
        this.message = message;
        this.sendAt = sendAt;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getSendAt() {
        return sendAt;
    }

    public void setSendAt(Date sendAt) {
        this.sendAt = sendAt;
    }

    @NonNull
    @Override
    public String toString() {
        return "Message{" +
                "sender='" + sender + '\'' +
                ", type='" + type + '\'' +
                ", message='" + message + '\'' +
                ", sendAt=" + sendAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message1 = (Message) o;
        return Objects.equals(sender, message1.sender) && Objects.equals(type, message1.type) && Objects.equals(message, message1.message) && Objects.equals(sendAt, message1.sendAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, type, message, sendAt);
    }
}
