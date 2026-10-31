package com.medicarelk.app.models;

import com.google.firebase.database.Exclude;

/**
 * Model representing a Patient Support Request in supportRequests/{requestId}.
 */
public class SupportRequest {

    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_RESOLVED = "RESOLVED";
    public static final String STATUS_CLOSED = "CLOSED";

    public static final String PRIORITY_LOW = "LOW";
    public static final String PRIORITY_NORMAL = "NORMAL";
    public static final String PRIORITY_HIGH = "HIGH";

    private String requestId;
    private String userId;
    private String subject;
    private String category;
    private String description;
    private String priority;
    private String status;
    private String appointmentId;
    private long createdAt;
    private long updatedAt;
    private long lastResponseAt;

    public SupportRequest() {
        this.status = STATUS_OPEN;
        this.priority = PRIORITY_NORMAL;
    }

    public SupportRequest(String requestId, String userId, String subject,
                          String category, String description, String appointmentId) {
        this.requestId = requestId;
        this.userId = userId;
        this.subject = subject;
        this.category = category != null ? category : "General";
        this.description = description;
        this.priority = PRIORITY_NORMAL;
        this.status = STATUS_OPEN;
        this.appointmentId = appointmentId != null ? appointmentId : "";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.lastResponseAt = System.currentTimeMillis();
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSubject() {
        return subject != null && !subject.trim().isEmpty() ? subject : "Support Request";
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getCategory() {
        return category != null ? category : "General";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority != null ? priority : PRIORITY_NORMAL;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status != null ? status : STATUS_OPEN;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
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

    public long getLastResponseAt() {
        return lastResponseAt;
    }

    public void setLastResponseAt(long lastResponseAt) {
        this.lastResponseAt = lastResponseAt;
    }

    @Exclude
    @com.google.firebase.firestore.Exclude
    public String getFormattedStatus() {
        if (STATUS_OPEN.equalsIgnoreCase(status)) return "OPEN";
        if (STATUS_IN_PROGRESS.equalsIgnoreCase(status)) return "IN PROGRESS";
        if (STATUS_RESOLVED.equalsIgnoreCase(status)) return "RESOLVED";
        if (STATUS_CLOSED.equalsIgnoreCase(status)) return "CLOSED";
        return status;
    }
}