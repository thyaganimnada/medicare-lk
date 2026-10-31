package com.medicarelk.app.models;

import com.google.firebase.database.Exclude;
import java.util.Locale;

/**
 * Model representing Doctor information stored in Cloud Firestore doctors/{doctorId}.
 */
public class Doctor {
    private String doctorId;
    private String fullName;
    private String specialization;
    private String subSpecialization;
    private String hospitalId;
    private String hospitalName;
    private String location;
    private String address;
    private double consultationFee;
    private boolean onlineConsultationAvailable;
    private boolean inPersonConsultationAvailable;
    private String profileImageUrl;
    private int yearsOfExperience;
    private String qualification;
    private String languages;
    private String bio;
    private String availabilityStatus;
    private double rating;
    private boolean isActive;
    private long createdAt;

    public Doctor() {
        // Required empty constructor for Firestore
        this.isActive = true;
    }

    public Doctor(String doctorId, String fullName, String specialization, String hospitalName,
                  String location, double consultationFee, boolean onlineConsultationAvailable,
                  boolean inPersonConsultationAvailable, String availabilityStatus) {
        this.doctorId = doctorId;
        this.fullName = fullName;
        this.specialization = specialization;
        this.hospitalName = hospitalName;
        this.location = location;
        this.consultationFee = consultationFee;
        this.onlineConsultationAvailable = onlineConsultationAvailable;
        this.inPersonConsultationAvailable = inPersonConsultationAvailable;
        this.availabilityStatus = availabilityStatus;
        this.isActive = true;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getFullName() {
        return fullName != null && !fullName.trim().isEmpty() ? fullName : "Doctor";
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSpecialization() {
        return specialization != null ? specialization : "General Practitioner";
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getSubSpecialization() {
        return subSpecialization != null ? subSpecialization : "";
    }

    public void setSubSpecialization(String subSpecialization) {
        this.subSpecialization = subSpecialization;
    }

    public String getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(String hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getHospitalName() {
        return hospitalName != null ? hospitalName : "MediCare Hospital";
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getLocation() {
        return location != null ? location : "Colombo";
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAddress() {
        return address != null ? address : "Colombo, Sri Lanka";
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(double consultationFee) {
        this.consultationFee = consultationFee;
    }

    @Exclude
    @com.google.firebase.firestore.Exclude
    public String getFormattedFee() {
        return String.format(Locale.US, "LKR %,.0f", consultationFee > 0 ? consultationFee : 3500.0);
    }

    public boolean isOnlineConsultationAvailable() {
        return onlineConsultationAvailable;
    }

    public void setOnlineConsultationAvailable(boolean onlineConsultationAvailable) {
        this.onlineConsultationAvailable = onlineConsultationAvailable;
    }

    public boolean isInPersonConsultationAvailable() {
        return inPersonConsultationAvailable;
    }

    public void setInPersonConsultationAvailable(boolean inPersonConsultationAvailable) {
        this.inPersonConsultationAvailable = inPersonConsultationAvailable;
    }

    @Exclude
    @com.google.firebase.firestore.Exclude
    public String getConsultationTypesText() {
        if (onlineConsultationAvailable && inPersonConsultationAvailable) {
            return "In-person & Online";
        } else if (onlineConsultationAvailable) {
            return "Online Video";
        } else {
            return "In-person Visit";
        }
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public int getYearsOfExperience() {
        return yearsOfExperience > 0 ? yearsOfExperience : 5;
    }

    public void setYearsOfExperience(int yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public String getQualification() {
        return qualification != null ? qualification : "MBBS, MD";
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getLanguages() {
        return languages != null ? languages : "English, Sinhala, Tamil";
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public String getBio() {
        return bio != null ? bio : "Experienced medical specialist dedicated to patient care.";
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus != null ? availabilityStatus : "Available Today";
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public double getRating() {
        return rating > 0 ? rating : 4.8;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}