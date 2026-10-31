package com.medicarelk.app.models;

/**
 * Model representing Doctor Availability Slots in Cloud Firestore doctorAvailability/{availabilityId}.
 */
public class DoctorAvailability {
    private String availabilityId;
    private String doctorId;
    private String date;
    private String startTime;
    private String endTime;
    private boolean isAvailable;
    private String consultationType;

    public DoctorAvailability() {
        // Required empty constructor
    }

    public DoctorAvailability(String availabilityId, String doctorId, String date,
                              String startTime, String endTime, boolean isAvailable,
                              String consultationType) {
        this.availabilityId = availabilityId;
        this.doctorId = doctorId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isAvailable = isAvailable;
        this.consultationType = consultationType;
    }

    public String getAvailabilityId() {
        return availabilityId;
    }

    public void setAvailabilityId(String availabilityId) {
        this.availabilityId = availabilityId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public String getConsultationType() {
        return consultationType;
    }

    public void setConsultationType(String consultationType) {
        this.consultationType = consultationType;
    }
}