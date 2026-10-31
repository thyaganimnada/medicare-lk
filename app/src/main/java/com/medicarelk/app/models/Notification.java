package com.medicarelk.app.models;

import com.google.firebase.database.Exclude;
import java.util.Locale;

/**
 * Model representing a Notification in notifications/{notificationId} or Notifications/{notificationId}.
 */
public class Notification {

    // Notification Type Constants
    public static final String TYPE_APPOINTMENT_CONFIRMED = "APPOINTMENT_CONFIRMED";
    public static final String TYPE_APPOINTMENT_REMINDER = "APPOINTMENT_REMINDER";
    public static final String TYPE_APPOINTMENT_RESCHEDULED = "APPOINTMENT_RESCHEDULED";
    public static final String TYPE_APPOINTMENT_CANCELLED = "APPOINTMENT_CANCELLED";
    public static final String TYPE_HEALTH_PROMOTION = "HEALTH_PROMOTION";
    public static final String TYPE_SEASONAL_HEALTH_ALERT = "SEASONAL_HEALTH_ALERT";
    public static final String TYPE_HEALTH_TIP = "HEALTH_TIP";
    public static final String TYPE_SYSTEM = "SYSTEM";

    private String notificationId;
    private String userId;
    private String type;
    private String title;
    private String message;
    private String appointmentId;
    private String promotionId;
    private String healthTipId;
    private long createdAt;
    private boolean isRead;
    private String priority; // HIGH, NORMAL, LOW
    private String deepLinkType;

    public Notification() {
        this.isRead = false;
        this.priority = "NORMAL";
        this.createdAt = System.currentTimeMillis();
    }

    public Notification(String notificationId, String userId, String type, String title,
                        String message, String appointmentId, String promotionId, String healthTipId) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.type = type != null ? type : TYPE_SYSTEM;
        this.title = title;
        this.message = message;
        this.appointmentId = appointmentId;
        this.promotionId = promotionId;
        this.healthTipId = healthTipId;
        this.createdAt = System.currentTimeMillis();
        this.isRead = false;
        this.priority = "NORMAL";
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type != null ? type : TYPE_SYSTEM;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title != null ? title : "MediCare Notification";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message != null ? message : "";
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(String promotionId) {
        this.promotionId = promotionId;
    }

    public String getHealthTipId() {
        return healthTipId;
    }

    public void setHealthTipId(String healthTipId) {
        this.healthTipId = healthTipId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getPriority() {
        return priority != null ? priority : "NORMAL";
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDeepLinkType() {
        return deepLinkType;
    }

    public void setDeepLinkType(String deepLinkType) {
        this.deepLinkType = deepLinkType;
    }
}