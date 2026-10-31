package com.medicarelk.app.repositories;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.medicarelk.app.models.Patient;

/**
 * Repository handling patient profile queries and updates.
 */
public class ProfileRepository {

    public interface OnProfileLoadedListener {
        void onSuccess(Patient patient);
        void onError(String errorMessage);
    }

    public interface OnProfileUpdatedListener {
        void onSuccess();
        void onError(String errorMessage);
    }

    private final FirebaseFirestore firestore;

    public ProfileRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Retrieves patient profile from Realtime Database with Firestore fallback.
     */
    public void getProfile(String uid, OnProfileLoadedListener listener) {
        if (uid == null || uid.trim().isEmpty()) {
            listener.onError("Unauthenticated request.");
            return;
        }

        FirebaseDatabase.getInstance().getReference("Users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Patient patient = snapshot.getValue(Patient.class);
                            if (patient != null) {
                                patient.setUid(uid);
                                listener.onSuccess(patient);
                                return;
                            }
                        }

                        // Try Firestore
                        firestore.collection("users").document(uid).get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                                        Patient p = task.getResult().toObject(Patient.class);
                                        if (p != null) {
                                            p.setUid(uid);
                                            listener.onSuccess(p);
                                            return;
                                        }
                                    }

                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    String email = user != null && user.getEmail() != null ? user.getEmail() : "";
                                    String name = email.contains("@") ? email.split("@")[0] : "Patient";
                                    listener.onSuccess(new Patient(uid, name, email, "", "", "PATIENT"));
                                })
                                .addOnFailureListener(e -> {
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    String email = user != null && user.getEmail() != null ? user.getEmail() : "";
                                    String name = email.contains("@") ? email.split("@")[0] : "Patient";
                                    listener.onSuccess(new Patient(uid, name, email, "", "", "PATIENT"));
                                });
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }

    /**
     * Updates patient profile fields across Realtime Database and Firestore.
     */
    public void updateProfile(Patient patient, OnProfileUpdatedListener listener) {
        if (patient == null || patient.getUid() == null || patient.getUid().trim().isEmpty()) {
            listener.onError("Invalid profile data.");
            return;
        }

        patient.setUpdatedAt(System.currentTimeMillis());

        // 1. Save to Realtime Database
        FirebaseDatabase.getInstance().getReference("Users")
                .child(patient.getUid()).setValue(patient)
                .addOnSuccessListener(aVoid -> {
                    // 2. Mirror save to Firestore
                    firestore.collection("users").document(patient.getUid()).set(patient);
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> listener.onError("Profile update failed: " + e.getMessage()));
    }
}