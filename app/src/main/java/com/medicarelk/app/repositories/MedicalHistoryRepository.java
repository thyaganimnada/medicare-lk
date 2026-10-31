package com.medicarelk.app.repositories;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.medicarelk.app.models.MedicalRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository managing sensitive patient medical records.
 * Enforces strict user isolation under Users/{uid}/MedicalHistory node.
 */
public class MedicalHistoryRepository {

    public interface OnMedicalRecordsLoadedListener {
        void onSuccess(List<MedicalRecord> records);
        void onError(String errorMessage);
    }

    public interface OnMedicalRecordUpdatedListener {
        void onSuccess();
        void onError(String errorMessage);
    }

    /**
     * Retrieves medical records belonging ONLY to the authenticated patient.
     */
    public void getMedicalRecords(String userId, OnMedicalRecordsLoadedListener listener) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || !currentUser.getUid().equals(userId)) {
            listener.onError("Unauthorized access.");
            return;
        }

        FirebaseDatabase.getInstance().getReference("Users")
                .child(userId).child("MedicalHistory")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<MedicalRecord> list = new ArrayList<>();
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                MedicalRecord record = child.getValue(MedicalRecord.class);
                                if (record != null) {
                                    record.setRecordId(child.getKey());
                                    list.add(record);
                                }
                            }
                        }
                        listener.onSuccess(list);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError("Failed to load medical history: " + error.getMessage());
                    }
                });
    }

    /**
     * Adds a new personal medical record for the authenticated patient.
     */
    public void addMedicalRecord(MedicalRecord record, OnMedicalRecordUpdatedListener listener) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            listener.onError("Unauthenticated request.");
            return;
        }

        String userId = currentUser.getUid();
        record.setUserId(userId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users")
                .child(userId).child("MedicalHistory");
        String id = ref.push().getKey();
        if (id == null) id = "REC-" + System.currentTimeMillis();

        record.setRecordId(id);
        record.setCreatedAt(System.currentTimeMillis());
        record.setUpdatedAt(System.currentTimeMillis());

        ref.child(id).setValue(record)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError("Failed to save record: " + e.getMessage()));
    }

    /**
     * Updates an existing medical record.
     */
    public void updateMedicalRecord(MedicalRecord record, OnMedicalRecordUpdatedListener listener) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || record == null || record.getRecordId() == null) {
            listener.onError("Invalid record data.");
            return;
        }

        String userId = currentUser.getUid();
        record.setUpdatedAt(System.currentTimeMillis());

        FirebaseDatabase.getInstance().getReference("Users")
                .child(userId).child("MedicalHistory")
                .child(record.getRecordId()).setValue(record)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError("Update failed: " + e.getMessage()));
    }

    /**
     * Deletes a medical record.
     */
    public void deleteMedicalRecord(String recordId, OnMedicalRecordUpdatedListener listener) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || recordId == null) {
            listener.onError("Invalid request.");
            return;
        }

        String userId = currentUser.getUid();

        FirebaseDatabase.getInstance().getReference("Users")
                .child(userId).child("MedicalHistory")
                .child(recordId).removeValue()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError("Delete failed: " + e.getMessage()));
    }
}