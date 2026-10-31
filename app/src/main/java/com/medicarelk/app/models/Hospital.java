package com.medicarelk.app.models;

import com.google.firebase.database.Exclude;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a Partnered Hospital or Clinic in hospitals collection.
 */
public class Hospital {
    private String hospitalId;
    private String name;
    private String address;
    private String city;
    private String location;
    private String contactNumber;
    private String openingHours;
    private List<String> services;
    private boolean emergencyAvailable;
    private String imageUrl;
    private boolean isActive;
    private long createdAt;

    public Hospital() {
        this.isActive = true;
        this.emergencyAvailable = true;
        this.services = new ArrayList<>();
    }

    public Hospital(String hospitalId, String name, String address, String city,
                    String location, String contactNumber, String openingHours,
                    List<String> services, boolean emergencyAvailable, String imageUrl) {
        this.hospitalId = hospitalId;
        this.name = name;
        this.address = address;
        this.city = city;
        this.location = location;
        this.contactNumber = contactNumber;
        this.openingHours = openingHours;
        this.services = services != null ? services : new ArrayList<>();
        this.emergencyAvailable = emergencyAvailable;
        this.imageUrl = imageUrl;
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
    }

    public String getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(String hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getName() {
        return name != null && !name.trim().isEmpty() ? name : "MediCare Partner Hospital";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address != null ? address : "Colombo, Sri Lanka";
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city != null ? city : "Colombo";
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLocation() {
        return location != null ? location : "Western Province";
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getContactNumber() {
        return contactNumber != null ? contactNumber : "+94 11 268 8888";
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getOpeningHours() {
        return openingHours != null ? openingHours : "24/7 Open";
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public List<String> getServices() {
        return services != null ? services : new ArrayList<>();
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    @Exclude
    @com.google.firebase.firestore.Exclude
    public String getServicesText() {
        if (services == null || services.isEmpty()) {
            return "General Medicine, Emergency, Surgery, Lab Tests";
        }
        return String.join(" • ", services);
    }

    public boolean isEmergencyAvailable() {
        return emergencyAvailable;
    }

    public void setEmergencyAvailable(boolean emergencyAvailable) {
        this.emergencyAvailable = emergencyAvailable;
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