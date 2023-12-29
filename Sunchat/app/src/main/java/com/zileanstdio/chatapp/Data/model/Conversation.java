package com.zileanstdio.chatapp.Data.model;

import androidx.annotation.NonNull;

import com.zileanstdio.chatapp.Utils.Constants;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Conversation implements Serializable {
    private List<String> userJoined;
    private Date createdAt;
    private String lastMessage;
    private String typeMessage;
    private String lastSender;
    private Date lastUpdated;


    public enum Type {
        TEXT(Constants.KEY_TYPE_TEXT),
        RECORD(Constants.KEY_TYPE_RECORD),
        CALL(Constants.KEY_TYPE_CALL),
        VIDEO_CALL(Constants.KEY_TYPE_VIDEO_CALL);

        public final String label;

        private Type(String label) {
            this.label = label;
        }
    }

    public Conversation() {
    }

    public static final Conversation TEXT;
    public static final Conversation RECORD;
    public static final Conversation CALL;
    public static final Conversation VIDEO_CALL;


    static {
        TEXT = new Conversation(Type.TEXT.label);
        RECORD = new Conversation(Type.RECORD.label);
        CALL = new Conversation(Type.CALL.label);
        VIDEO_CALL = new Conversation(Type.VIDEO_CALL.label);
    }

    public Conversation(List<String> userJoined, Date createdAt, String lastMessage, String typeMessage, String lastSender, Date lastUpdated) {
        this.userJoined = userJoined;
        this.createdAt = createdAt;
        this.lastMessage = lastMessage;
        this.typeMessage = typeMessage;
        this.lastSender = lastSender;
        this.lastUpdated = lastUpdated;
    }

    public Conversation(String typeMessage) {
        this.typeMessage = typeMessage;
    }

    public List<String> getUserJoined() {
        return userJoined;
    }

    public void setUserJoined(List<String> userJoined) {
        this.userJoined = userJoined;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getTypeMessage() {
        return typeMessage;
    }

    public void setTypeMessage(String typeMessage) {
        this.typeMessage = typeMessage;
    }

    public String getLastSender() {
        return lastSender;
    }

    public void setLastSender(String lastSender) {
        this.lastSender = lastSender;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @NonNull
    @Override
    public String toString() {
        return "Conversation{" +
                "userJoined=" + userJoined +
                ", createdAt=" + createdAt +
                ", lastMessage='" + lastMessage + '\'' +
                ", typeMessage='" + typeMessage + '\'' +
                ", lastSender='" + lastSender + '\'' +
                ", lastUpdated=" + lastUpdated +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conversation that = (Conversation) o;
        return Objects.equals(userJoined, that.userJoined) && Objects.equals(createdAt, that.createdAt) && Objects.equals(lastMessage, that.lastMessage) && Objects.equals(typeMessage, that.typeMessage) && Objects.equals(lastSender, that.lastSender) && Objects.equals(lastUpdated, that.lastUpdated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userJoined, createdAt, lastMessage, typeMessage, lastSender, lastUpdated);
    }
}
