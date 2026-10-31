package com.medicarelk.app.models;

import com.google.firebase.database.Exclude;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Model representing Patient Profile information in users/{uid}.
 */
public class Patient {
    private String uid;
    private String fullName;
    private String email;
    private String phone;
    private String profileImageUrl;
    private String role;
    private String dateOfBirth;
    private String gender;
    private String address;
    private String emergencyContactName;
    private String emergencyContactRelationship;
    private String emergencyContactPhone;
    private String bloodGroup;
    private String height;
    private String weight;
    private long createdAt;
    private long updatedAt;

    public Patient() {
        this.role = "PATIENT";
    }

    public Patient(String uid, String fullName, String email, String phone,
                   String profileImageUrl, String role) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.profileImageUrl = profileImageUrl;
        this.role = role != null ? role : "PATIENT";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFullName() {
        return fullName != null && !fullName.trim().isEmpty() ? fullName : "Patient";
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email != null ? email : "";
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone != null ? phone : "";
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getRole() {
        return role != null ? role : "PATIENT";
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDateOfBirth() {
        return dateOfBirth != null ? dateOfBirth : "Not set";
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender != null ? gender : "Male";
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address != null ? address : "Not set";
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmergencyContactName() {
        return emergencyContactName != null ? emergencyContactName : "Not set";
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactRelationship() {
        return emergencyContactRelationship != null ? emergencyContactRelationship : "Family";
    }

    public void setEmergencyContactRelationship(String emergencyContactRelationship) {
        this.emergencyContactRelationship = emergencyContactRelationship;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone != null ? emergencyContactPhone : "";
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public String getEmergencyContact() {
        return getEmergencyContactName();
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContactName = emergencyContact;
    }

    public String getBloodGroup() {
        return bloodGroup != null ? bloodGroup : "B+";
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getHeight() {
        return height != null ? height : "5ft 10in";
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight != null ? weight : "76 Kg";
    }

    public void setWeight(String weight) {
        this.weight = weight;
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
    public String getFormattedAge() {
        if (dateOfBirth == null || dateOfBirth.trim().isEmpty() || dateOfBirth.equalsIgnoreCase("Not set")) {
            return "25 Yrs";
        }
        String[] formats = {"yyyy-MM-dd", "dd/MM/yyyy", "MM/dd/yyyy", "dd-MM-yyyy", "yyyy/MM/dd"};
        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
                sdf.setLenient(false);
                Date dobDate = sdf.parse(dateOfBirth.trim());
                if (dobDate != null) {
                    Calendar dob = Calendar.getInstance();
                    dob.setTime(dobDate);

                    Calendar today = Calendar.getInstance();
                    int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
                    if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                        age--;
                    }
                    return age > 0 ? age + " Yrs" : "1 Yr";
                }
            } catch (Exception ignored) {
            }
        }
        return "25 Yrs";
    }

    @Exclude
    @com.google.firebase.firestore.Exclude
    public boolean isPatientRole() {
        return role == null || role.trim().isEmpty() || role.equalsIgnoreCase("PATIENT");
    }
}