package com.zileanstdio.chatapp.Data.model;

import androidx.annotation.NonNull;

import java.util.Objects;

public class MessageWrapper {
    
    private String documentId;
    private Message message;

    public MessageWrapper() {
    }

    public MessageWrapper(String documentId, Message message) {
        this.documentId = documentId;
        this.message = message;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    @NonNull
    @Override
    public String toString() {
        return "MessageWrapper{" +
                "documentId='" + documentId + '\'' +
                ", message=" + message +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageWrapper that = (MessageWrapper) o;
        return Objects.equals(documentId, that.documentId) && message.equals(that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentId, message);
    }
}
