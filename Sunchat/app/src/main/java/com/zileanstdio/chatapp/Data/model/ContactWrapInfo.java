package com.zileanstdio.chatapp.Data.model;

import java.io.Serializable;
import java.util.Objects;

public class ContactWrapInfo implements Serializable {
    private Contact contact;
    private User user;

    public ContactWrapInfo() {
    }

    public ContactWrapInfo(Contact contact, User user) {
        this.contact = contact;
        this.user = user;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "ContactWrapInfo{" +
                "contact=" + contact +
                ", user=" + user +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContactWrapInfo info = (ContactWrapInfo) o;
        return Objects.equals(contact, info.contact) && Objects.equals(user, info.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contact, user);
    }
}
