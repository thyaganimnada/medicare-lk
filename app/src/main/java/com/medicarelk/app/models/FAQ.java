package com.medicarelk.app.models;

import com.google.firebase.database.Exclude;

/**
 * Model representing a Frequently Asked Question in faqs/{faqId} or FAQs/{faqId}.
 */
public class FAQ {

    private String faqId;
    private String question;
    private String answer;
    private String category;
    private String keywords;
    private int displayOrder;
    private boolean isActive;
    private long createdAt;
    private long updatedAt;

    private boolean isExpanded;

    public FAQ() {
        this.isActive = true;
        this.isExpanded = false;
    }

    public FAQ(String faqId, String question, String answer, String category, String keywords) {
        this.faqId = faqId;
        this.question = question;
        this.answer = answer;
        this.category = category != null ? category : "General";
        this.keywords = keywords != null ? keywords : "";
        this.displayOrder = 1;
        this.isActive = true;
        this.isExpanded = false;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public String getFaqId() {
        return faqId;
    }

    public void setFaqId(String faqId) {
        this.faqId = faqId;
    }

    public String getQuestion() {
        return question != null && !question.trim().isEmpty() ? question : "Frequently Asked Question";
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer != null ? answer : "";
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getCategory() {
        return category != null ? category : "General";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getKeywords() {
        return keywords != null ? keywords : "";
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
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

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Exclude
    @com.google.firebase.firestore.Exclude
    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
}