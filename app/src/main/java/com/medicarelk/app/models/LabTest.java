package com.medicarelk.app.models;

import com.google.firebase.database.Exclude;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Model representing a Diagnostic Lab Test in labTests collection.
 */
public class LabTest {
    private String labTestId;
    private String name;
    private String description;
    private String category;
    private double price;
    private String resultTime;
    private String preparationInstructions;
    private List<String> availableLocations;
    private String imageUrl;
    private boolean isActive;
    private long createdAt;

    public LabTest() {
        this.isActive = true;
        this.availableLocations = new ArrayList<>();
    }

    public LabTest(String labTestId, String name, String description, String category,
                   double price, String resultTime, String preparationInstructions,
                   List<String> availableLocations) {
        this.labTestId = labTestId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.resultTime = resultTime;
        this.preparationInstructions = preparationInstructions;
        this.availableLocations = availableLocations != null ? availableLocations : new ArrayList<>();
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
    }

    public String getLabTestId() {
        return labTestId;
    }

    public void setLabTestId(String labTestId) {
        this.labTestId = labTestId;
    }

    public String getName() {
        return name != null && !name.trim().isEmpty() ? name : "Diagnostic Test";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description != null ? description : "Standard medical laboratory diagnostic test.";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category != null ? category : "General Screening";
    }

    public void setCategory(String category) {
        this.category = category;
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
        return String.format(Locale.US, "LKR %,.0f", price > 0 ? price : 1500.0);
    }

    public String getResultTime() {
        return resultTime != null ? resultTime : "Same Day (24 hrs)";
    }

    public void setResultTime(String resultTime) {
        this.resultTime = resultTime;
    }

    public String getPreparationInstructions() {
        return preparationInstructions != null && !preparationInstructions.trim().isEmpty()
                ? preparationInstructions : "No special preparation required.";
    }

    public void setPreparationInstructions(String preparationInstructions) {
        this.preparationInstructions = preparationInstructions;
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getImageUrl() {
        return imageUrl != null ? imageUrl : "";
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}