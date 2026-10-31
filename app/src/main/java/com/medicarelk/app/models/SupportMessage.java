package com.medicarelk.app.models;

/**
 * Model representing a message/reply inside supportRequests/{requestId}/messages/{messageId}.
 */
public class SupportMessage {

    public static final String SENDER_PATIENT = "PATIENT";
    public static final String SENDER_STAFF = "STAFF";

    private String messageId;
    private String senderType; // PATIENT, STAFF
    private String senderId;
    private String message;
    private long createdAt;

    public SupportMessage() {
        this.senderType = SENDER_PATIENT;
    }

    public SupportMessage(String messageId, String senderType, String senderId, String message) {
        this.messageId = messageId;
        this.senderType = senderType != null ? senderType : SENDER_PATIENT;
        this.senderId = senderId;
        this.message = message;
        this.createdAt = System.currentTimeMillis();
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderType() {
        return senderType != null ? senderType : SENDER_PATIENT;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessage() {
        return message != null ? message : "";
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}