package com.medicarelk.app.models;

import com.google.firebase.database.Exclude;

/**
 * Model representing a Patient Medical History Record in users/{uid}/medicalHistory/{recordId}.
 */
public class MedicalRecord {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_RESOLVED = "RESOLVED";
    public static final String STATUS_HISTORICAL = "HISTORICAL";

    private String recordId;
    private String userId;
    private String conditionName;
    private String description;
    private String diagnosisDate; // Format: YYYY-MM-DD
    private String status; // ACTIVE, RESOLVED, HISTORICAL
    private String notes;
    private long createdAt;
    private long updatedAt;

    public MedicalRecord() {
        this.status = STATUS_ACTIVE;
    }

    public MedicalRecord(String recordId, String userId, String conditionName,
                         String description, String diagnosisDate, String status, String notes) {
        this.recordId = recordId;
        this.userId = userId;
        this.conditionName = conditionName;
        this.description = description;
        this.diagnosisDate = diagnosisDate;
        this.status = status != null ? status : STATUS_ACTIVE;
        this.notes = notes;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getConditionName() {
        return conditionName != null && !conditionName.trim().isEmpty() ? conditionName : "Medical Condition";
    }

    public void setConditionName(String conditionName) {
        this.conditionName = conditionName;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDiagnosisDate() {
        return diagnosisDate != null ? diagnosisDate : "";
    }

    public void setDiagnosisDate(String diagnosisDate) {
        this.diagnosisDate = diagnosisDate;
    }

    public String getStatus() {
        return status != null ? status : STATUS_ACTIVE;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes != null ? notes : "";
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
}