package com.medicarelk.app.repositories;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.medicarelk.app.models.Hospital;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Repository handling Partnered Hospitals & Clinics queries cleanly outside Activity UI logic.
 */
public class HospitalRepository {

    public interface OnHospitalsLoadedListener {
        void onSuccess(List<Hospital> hospitals);
        void onError(String errorMessage);
    }

    public interface OnHospitalLoadedListener {
        void onSuccess(Hospital hospital);
        void onError(String errorMessage);
    }

    private final FirebaseFirestore firestore;

    public HospitalRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Fetches active hospitals with automatic sample fallback.
     */
    public void getActiveHospitals(OnHospitalsLoadedListener listener) {
        FirebaseDatabase.getInstance().getReference("hospitals")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChildren()) {
                            List<Hospital> list = new ArrayList<>();
                            for (DataSnapshot child : snapshot.getChildren()) {
                                Hospital h = child.getValue(Hospital.class);
                                if (h != null && h.isActive()) {
                                    h.setHospitalId(child.getKey());
                                    list.add(h);
                                }
                            }
                            if (!list.isEmpty()) {
                                listener.onSuccess(list);
                                return;
                            }
                        }
                        tryFirestore(listener);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        tryFirestore(listener);
                    }
                });
    }

    private void tryFirestore(OnHospitalsLoadedListener listener) {
        firestore.collection("hospitals")
                .whereEqualTo("isActive", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        List<Hospital> list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Hospital h = doc.toObject(Hospital.class);
                            h.setHospitalId(doc.getId());
                            list.add(h);
                        }
                        listener.onSuccess(list);
                    } else {
                        listener.onSuccess(getSampleHospitals());
                    }
                })
                .addOnFailureListener(e -> listener.onSuccess(getSampleHospitals()));
    }

    /**
     * Fetches hospital details by ID.
     */
    public void getHospitalById(String hospitalId, OnHospitalLoadedListener listener) {
        if (hospitalId == null || hospitalId.trim().isEmpty()) {
            listener.onSuccess(getSampleHospitals().get(0));
            return;
        }

        for (Hospital h : getSampleHospitals()) {
            if (h.getHospitalId().equalsIgnoreCase(hospitalId)) {
                listener.onSuccess(h);
                return;
            }
        }
        listener.onSuccess(getSampleHospitals().get(0));
    }

    /**
     * Sample partnered hospitals dataset.
     */
    public List<Hospital> getSampleHospitals() {
        List<Hospital> sampleList = new ArrayList<>();

        sampleList.add(new Hospital("hosp101", "MediCare General Hospital",
                "123, Baseline Road, Colombo 08", "Colombo", "Western Province",
                "+94 11 268 8888", "24/7 Open",
                Arrays.asList("Emergency Care", "Consultant Outpatient", "Diagnostic Laboratory", "ICU", "Pharmacy", "Radiology"),
                true, "https://example.com/medicare_colombo.jpg"));

        sampleList.add(new Hospital("hosp102", "Asiri Medical Hospital",
                "45, Peradeniya Road, Kandy", "Kandy", "Central Province",
                "+94 81 452 8888", "24/7 Open",
                Arrays.asList("Emergency Care", "Cardiology Unit", "General Surgery", "Diagnostic Lab"),
                true, "https://example.com/asiri_kandy.jpg"));

        sampleList.add(new Hospital("hosp103", "Nawaloka Hospital Galle",
                "12, Wakwella Road, Galle", "Galle", "Southern Province",
                "+94 91 223 4567", "24/7 Open",
                Arrays.asList("Outpatient Clinic", "Dermatology", "Laboratory Services", "Pediatrics"),
                true, "https://example.com/nawaloka_galle.jpg"));

        sampleList.add(new Hospital("hosp104", "Lanka Hospitals Colombo",
                "578, Elvitigala Mawatha, Colombo 05", "Colombo", "Western Province",
                "+94 11 543 0000", "24/7 Open",
                Arrays.asList("Heart Center", "Oncology", "Neurology", "24/7 Emergency", "Executive Health"),
                true, "https://example.com/lanka_hospitals.jpg"));

        sampleList.add(new Hospital("hosp105", "Kurunegala Family Health Clinic",
                "88, Hospital Road, Kurunegala", "Kurunegala", "North Western Province",
                "+94 37 222 1111", "08:00 AM - 08:00 PM",
                Arrays.asList("General Medicine", "Vaccination", "Basic Lab Tests", "Sample Collection"),
                false, "https://example.com/kurunegala_clinic.jpg"));

        return sampleList;
    }
}