package com.medicarelk.app.models;

/**
 * Data model for User information in MediCare LK.
 * Prepared for future Firebase Authentication & Firestore integration.
 */
public class User {
    private String uid;
    private String fullName;
    private String email;
    private String phone;
    private String dateOfBirth;
    private String gender;
    private String address;
    private String emergencyContact;

    public User() {
        // Default constructor required for Firebase Firestore
    }

    public User(String fullName, String email, String phone, String dateOfBirth,
                String gender, String address, String emergencyContact) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.address = address;
        this.emergencyContact = emergencyContact;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }
}