package com.medicarelk.app.models;

import com.google.firebase.database.Exclude;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Model representing a Patient Appointment in Realtime Database & Cloud Firestore appointments/{appointmentId}.
 * Contains snapshot fields so historical records remain accurate even if doctor details change.
 */
public class Appointment {

    // Status Constants
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_NO_SHOW = "NO_SHOW";

    private String appointmentId;
    private String patientId;
    private String patientNameSnapshot;
    private String doctorId;
    private String doctorNameSnapshot;
    private String specializationSnapshot;
    private String hospitalNameSnapshot;
    private String locationSnapshot;
    private String doctorProfileImageUrlSnapshot;
    private String appointmentDate; // Format: YYYY-MM-DD
    private String appointmentTime; // Format: HH:mm AM/PM
    private long appointmentTimestamp; // Timestamp in milliseconds for sorting/validation
    private String consultationType; // "In-person" or "Online"
    private double consultationFee;
    private String status; // PENDING, CONFIRMED, CANCELLED, COMPLETED, NO_SHOW
    private long createdAt;
    private long updatedAt;

    public Appointment() {
        this.status = STATUS_CONFIRMED;
    }

    public Appointment(String appointmentId, String patientId, String patientNameSnapshot,
                       String doctorId, String doctorNameSnapshot, String specializationSnapshot,
                       String hospitalNameSnapshot, String locationSnapshot, String appointmentDate,
                       String appointmentTime, long appointmentTimestamp, String consultationType,
                       double consultationFee, String status) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.patientNameSnapshot = patientNameSnapshot;
        this.doctorId = doctorId;
        this.doctorNameSnapshot = doctorNameSnapshot;
        this.specializationSnapshot = specializationSnapshot;
        this.hospitalNameSnapshot = hospitalNameSnapshot;
        this.locationSnapshot = locationSnapshot;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.appointmentTimestamp = appointmentTimestamp;
        this.consultationType = consultationType;
        this.consultationFee = consultationFee;
        this.status = status != null ? status : STATUS_CONFIRMED;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPatientNameSnapshot() {
        return patientNameSnapshot != null ? patientNameSnapshot : "Patient";
    }

    public void setPatientNameSnapshot(String patientNameSnapshot) {
        this.patientNameSnapshot = patientNameSnapshot;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorNameSnapshot() {
        return doctorNameSnapshot != null ? doctorNameSnapshot : "Doctor";
    }

    public void setDoctorNameSnapshot(String doctorNameSnapshot) {
        this.doctorNameSnapshot = doctorNameSnapshot;
    }

    public String getSpecializationSnapshot() {
        return specializationSnapshot != null ? specializationSnapshot : "General Medicine";
    }

    public void setSpecializationSnapshot(String specializationSnapshot) {
        this.specializationSnapshot = specializationSnapshot;
    }

    public String getHospitalNameSnapshot() {
        return hospitalNameSnapshot != null ? hospitalNameSnapshot : "MediCare Hospital";
    }

    public void setHospitalNameSnapshot(String hospitalNameSnapshot) {
        this.hospitalNameSnapshot = hospitalNameSnapshot;
    }

    public String getLocationSnapshot() {
        return locationSnapshot != null ? locationSnapshot : "Colombo";
    }

    public void setLocationSnapshot(String locationSnapshot) {
        this.locationSnapshot = locationSnapshot;
    }

    public String getDoctorProfileImageUrlSnapshot() {
        return doctorProfileImageUrlSnapshot != null ? doctorProfileImageUrlSnapshot : "";
    }

    public void setDoctorProfileImageUrlSnapshot(String doctorProfileImageUrlSnapshot) {
        this.doctorProfileImageUrlSnapshot = doctorProfileImageUrlSnapshot;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public long getAppointmentTimestamp() {
        return appointmentTimestamp;
    }

    public void setAppointmentTimestamp(long appointmentTimestamp) {
        this.appointmentTimestamp = appointmentTimestamp;
    }

    public String getConsultationType() {
        return consultationType != null ? consultationType : "In-person";
    }

    public void setConsultationType(String consultationType) {
        this.consultationType = consultationType;
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

    public String getStatus() {
        return status != null ? status : STATUS_CONFIRMED;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Returns true if the appointment date/time is in the past.
     */
    @Exclude
    @com.google.firebase.firestore.Exclude
    public boolean isPast() {
        long now = System.currentTimeMillis();
        if (appointmentTimestamp > 0) {
            return appointmentTimestamp < now;
        }
        if (appointmentDate != null && !appointmentDate.trim().isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date apptDate = sdf.parse(appointmentDate.trim());
                if (apptDate != null) {
                    Calendar apptCal = Calendar.getInstance();
                    apptCal.setTime(apptDate);
                    apptCal.set(Calendar.HOUR_OF_DAY, 23);
                    apptCal.set(Calendar.MINUTE, 59);
                    apptCal.set(Calendar.SECOND, 59);
                    return apptCal.getTimeInMillis() < now;
                }
            } catch (Exception ignored) {}
        }
        return false;
    }

    /**
     * Returns the effective status. If date/time has passed, status resolves to COMPLETED (Closed).
     */
    @Exclude
    @com.google.firebase.firestore.Exclude
    public String getEffectiveStatus() {
        if (STATUS_CANCELLED.equalsIgnoreCase(status)) {
            return STATUS_CANCELLED;
        }
        if (STATUS_COMPLETED.equalsIgnoreCase(status) || STATUS_NO_SHOW.equalsIgnoreCase(status) || isPast()) {
            return STATUS_COMPLETED;
        }
        return status != null ? status : STATUS_CONFIRMED;
    }

    @Exclude
    @com.google.firebase.firestore.Exclude
    public boolean isUpcoming() {
        if (STATUS_CANCELLED.equalsIgnoreCase(status) ||
            STATUS_COMPLETED.equalsIgnoreCase(status) ||
            STATUS_NO_SHOW.equalsIgnoreCase(status)) {
            return false;
        }
        return !isPast();
    }

    @Exclude
    @com.google.firebase.firestore.Exclude
    public boolean isCancellable() {
        return isUpcoming();
    }

    @Exclude
    @com.google.firebase.firestore.Exclude
    public boolean isReschedulable() {
        return isUpcoming();
    }
}