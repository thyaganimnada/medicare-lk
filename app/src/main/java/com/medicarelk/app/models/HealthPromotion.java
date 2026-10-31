package com.medicarelk.app.models;

/**
 * Model representing Health Promotions and wellness campaigns.
 */
public class HealthPromotion {
    private String id;
    private String title;
    private String subtitle;
    private String description;

    public HealthPromotion() {}

    public HealthPromotion(String id, String title, String subtitle, String description) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getDescription() {
        return description;
    }
}