package com.medicarelk.app.repositories;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.medicarelk.app.models.Patient;

/**
 * Repository using Firebase Realtime Database for patient profile operations.
 * Bypasses Firestore API disabled issues seamlessly.
 */
public class PatientRepository {

    public interface OnPatientLoadedListener {
        void onSuccess(Patient patient);
        void onError(String errorMessage);
    }

    /**
     * Retrieves basic patient profile from Firebase Realtime Database Users/{uid}.
     */
    public void getPatientProfile(String uid, OnPatientLoadedListener listener) {
        if (uid == null || uid.trim().isEmpty()) {
            listener.onError("Invalid user ID.");
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
                        // Default fallback patient
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        String email = currentUser != null && currentUser.getEmail() != null ? currentUser.getEmail() : "";
                        String name = email.contains("@") ? email.split("@")[0] : "Patient";
                        listener.onSuccess(new Patient(uid, name, email, "", "", "PATIENT"));
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        String email = currentUser != null && currentUser.getEmail() != null ? currentUser.getEmail() : "";
                        String name = email.contains("@") ? email.split("@")[0] : "Patient";
                        listener.onSuccess(new Patient(uid, name, email, "", "", "PATIENT"));
                    }
                });
    }
}