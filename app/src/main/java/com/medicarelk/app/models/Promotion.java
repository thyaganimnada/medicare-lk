package com.medicarelk.app.models;

import com.google.firebase.database.Exclude;

/**
 * Model representing a Healthcare Promotion in promotions/{promotionId}.
 */
public class Promotion {

    private String promotionId;
    private String title;
    private String description;
    private String shortDescription;
    private String imageUrl;
    private String hospitalName;
    private String hospitalId;
    private String promotionType; // Health Check-up, Vaccination Drive, Laboratory Offer, Seasonal Campaign, Preventive Screening, Wellness Program
    private String startDate;
    private String endDate;
    private String termsAndConditions;
    private String contactNumber;
    private boolean isActive;
    private long createdAt;

    public Promotion() {
        this.isActive = true;
    }

    public Promotion(String promotionId, String title, String shortDescription, String description,
                     String imageUrl, String hospitalName, String promotionType, String startDate,
                     String endDate, String termsAndConditions, String contactNumber) {
        this.promotionId = promotionId;
        this.title = title;
        this.shortDescription = shortDescription;
        this.description = description;
        this.imageUrl = imageUrl;
        this.hospitalName = hospitalName;
        this.promotionType = promotionType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.termsAndConditions = termsAndConditions;
        this.contactNumber = contactNumber;
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
    }

    public String getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(String promotionId) {
        this.promotionId = promotionId;
    }

    public String getTitle() {
        return title != null && !title.trim().isEmpty() ? title : "Special Health Offer";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription != null ? shortDescription : "";
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getHospitalName() {
        return hospitalName != null ? hospitalName : "MediCare Partner Hospital";
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(String hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getPromotionType() {
        return promotionType != null ? promotionType : "Seasonal Campaign";
    }

    public void setPromotionType(String promotionType) {
        this.promotionType = promotionType;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getTermsAndConditions() {
        return termsAndConditions != null ? termsAndConditions : "Standard terms and conditions apply.";
    }

    public void setTermsAndConditions(String termsAndConditions) {
        this.termsAndConditions = termsAndConditions;
    }

    public String getContactNumber() {
        return contactNumber != null ? contactNumber : "+94 11 268 8888";
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
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