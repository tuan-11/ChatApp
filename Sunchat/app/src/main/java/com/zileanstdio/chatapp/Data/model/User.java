package com.zileanstdio.chatapp.Data.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class User implements Serializable {
    private String userName;
    private boolean onlineStatus;
    private String phoneNumber;
    private String gender;
    private String birthDate;
    private String avatarImageUrl;
    private Date createdAt;

    private List<String> conversationList;

    public User(String userName, boolean onlineStatus, String phoneNumber, String gender, String birthDate, String avatarImageUrl, Date createdAt, List<String> conversationList) {
        this.userName = userName;
        this.onlineStatus = onlineStatus;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.birthDate = birthDate;
        this.avatarImageUrl = avatarImageUrl;
        this.createdAt = createdAt;
        this.conversationList = conversationList;
    }

    public List<String> getConversationList() {
        return conversationList;
    }

    public void setConversationList(List<String> conversationList) {
        this.conversationList = conversationList;
    }


    public User() {}


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(boolean onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getAvatarImageUrl() {
        return avatarImageUrl;
    }

    public void setAvatarImageUrl(String avatarImageUrl) {
        this.avatarImageUrl = avatarImageUrl;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }


    @NonNull
    @Override
    public String toString() {
        return "User{" +
                ", userName='" + userName + '\'' +
                ", onlineStatus=" + onlineStatus +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", gender='" + gender + '\'' +
                ", birthDate='" + birthDate + '\'' +
                ", avatarImageUrl='" + avatarImageUrl + '\'' +
                ", createdAt=" + createdAt +
                ", conversationList=" + conversationList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return onlineStatus == user.onlineStatus && Objects.equals(userName, user.userName) && Objects.equals(phoneNumber, user.phoneNumber) && Objects.equals(gender, user.gender) && Objects.equals(birthDate, user.birthDate) && Objects.equals(avatarImageUrl, user.avatarImageUrl) && Objects.equals(createdAt, user.createdAt) && Objects.equals(conversationList, user.conversationList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, onlineStatus, phoneNumber, gender, birthDate, avatarImageUrl, createdAt, conversationList);
    }
}
