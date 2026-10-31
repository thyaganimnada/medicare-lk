package com.medicarelk.app.models;

import com.google.firebase.database.Exclude;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Model representing a Health Screening Package in healthPackages collection.
 */
public class HealthPackage {
    private String packageId;
    private String name;
    private String description;
    private double price;
    private List<String> includedTests;
    private String duration;
    private String eligibility;
    private List<String> availableLocations;
    private String imageUrl;
    private boolean isActive;
    private long createdAt;

    public HealthPackage() {
        this.isActive = true;
        this.includedTests = new ArrayList<>();
        this.availableLocations = new ArrayList<>();
    }

    public HealthPackage(String packageId, String name, String description, double price,
                         List<String> includedTests, String duration, String eligibility,
                         List<String> availableLocations, String imageUrl) {
        this.packageId = packageId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.includedTests = includedTests != null ? includedTests : new ArrayList<>();
        this.duration = duration;
        this.eligibility = eligibility;
        this.availableLocations = availableLocations != null ? availableLocations : new ArrayList<>();
        this.imageUrl = imageUrl;
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getName() {
        return name != null && !name.trim().isEmpty() ? name : "Health Screening Package";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description != null ? description : "Comprehensive health screening package.";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Exclude
    @com.google.firebase.firestore.Exclude
    public String getFormattedPrice() {
        return String.format(Locale.US, "LKR %,.0f", price > 0 ? price : 12500.0);
    }

    public List<String> getIncludedTests() {
        return includedTests != null ? includedTests : new ArrayList<>();
    }

    public void setIncludedTests(List<String> includedTests) {
        this.includedTests = includedTests;
    }

    @Exclude
    @com.google.firebase.firestore.Exclude
    public String getIncludedCountText() {
        int count = includedTests != null ? includedTests.size() : 0;
        return count + " Tests Included";
    }

    public String getDuration() {
        return duration != null ? duration : "2 - 3 Hours";
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getEligibility() {
        return eligibility != null ? eligibility : "Recommended for Adults (18+)";
    }

    public void setEligibility(String eligibility) {
        this.eligibility = eligibility;
    }

    public List<String> getAvailableLocations() {
        return availableLocations != null ? availableLocations : new ArrayList<>();
    }

    public void setAvailableLocations(List<String> availableLocations) {
        this.availableLocations = availableLocations;
    }

    @Exclude
    @com.google.firebase.firestore.Exclude
    public String getLocationsText() {
        if (availableLocations == null || availableLocations.isEmpty()) {
            return "MediCare Hospital Colombo";
        }
        return String.join(", ", availableLocations);
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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