package com.zileanstdio.chatapp.Data.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class Contact implements Serializable {
    private String numberPhone;
    private String contactName;
    private int relationship;
    private Date modifiedAt;

    public Contact() {
    }

    public Contact(String numberPhone, String contactName, int relationship, Date modifiedAt) {
        this.numberPhone = numberPhone;
        this.contactName = contactName;
        this.relationship = relationship;
        this.modifiedAt = modifiedAt;
    }

    public String getNumberPhone() {
        return numberPhone;
    }

    public void setNumberPhone(String numberPhone) {
        this.numberPhone = numberPhone;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public int getRelationship() {
        return relationship;
    }

    public void setRelationship(int relationship) {
        this.relationship = relationship;
    }

    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "numberPhone='" + numberPhone + '\'' +
                ", contactName='" + contactName + '\'' +
                ", relationship=" + relationship +
                ", modifiedAt=" + modifiedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return relationship == contact.relationship && Objects.equals(numberPhone, contact.numberPhone) && Objects.equals(contactName, contact.contactName) && Objects.equals(modifiedAt, contact.modifiedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numberPhone, contactName, relationship, modifiedAt);
    }
}
