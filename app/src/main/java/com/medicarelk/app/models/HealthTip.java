package com.medicarelk.app.models;

import com.google.firebase.database.Exclude;

/**
 * Model representing Health Tips in healthTips/{healthTipId}.
 */
public class HealthTip {
    private String tipId;
    private String category;
    private String title;
    private String summary;
    private String content;
    private String imageUrl;
    private String author;
    private long publishedAt;
    private boolean isActive;

    public HealthTip() {
        this.isActive = true;
    }

    public HealthTip(String tipId, String category, String title, String summary) {
        this.tipId = tipId;
        this.category = category;
        this.title = title;
        this.summary = summary;
        this.content = summary;
        this.author = "MediCare Health Editorial Board";
        this.isActive = true;
        this.publishedAt = System.currentTimeMillis();
    }

    public HealthTip(String tipId, String category, String title, String summary, String content,
                     String imageUrl, String author) {
        this.tipId = tipId;
        this.category = category;
        this.title = title;
        this.summary = summary;
        this.content = content != null ? content : summary;
        this.imageUrl = imageUrl;
        this.author = author != null ? author : "MediCare Medical Advisory";
        this.isActive = true;
        this.publishedAt = System.currentTimeMillis();
    }

    // Compatibility getters for legacy callers
    @Exclude
    @com.google.firebase.firestore.Exclude
    public String getId() {
        return getTipId();
    }

    @Exclude
    @com.google.firebase.firestore.Exclude
    public String getDescription() {
        return getSummary();
    }

    public String getTipId() {
        return tipId != null ? tipId : "";
    }

    public void setTipId(String tipId) {
        this.tipId = tipId;
    }

    public String getCategory() {
        return category != null ? category : "General Wellness";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title != null && !title.trim().isEmpty() ? title : "Health Tip";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary != null ? summary : "";
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content != null && !content.trim().isEmpty() ? content : getSummary();
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAuthor() {
        return author != null ? author : "MediCare Health Board";
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(long publishedAt) {
        this.publishedAt = publishedAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}