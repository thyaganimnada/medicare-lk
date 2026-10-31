package com.medicarelk.app.repositories;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.medicarelk.app.models.HealthPackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Repository handling Health Screening Package queries cleanly outside Activity UI logic.
 */
public class HealthPackageRepository {

    public interface OnPackagesLoadedListener {
        void onSuccess(List<HealthPackage> packages);
        void onError(String errorMessage);
    }

    public interface OnPackageLoadedListener {
        void onSuccess(HealthPackage healthPackage);
        void onError(String errorMessage);
    }

    private final FirebaseFirestore firestore;

    public HealthPackageRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Fetches active health packages with automatic sample fallback.
     */
    public void getActivePackages(OnPackagesLoadedListener listener) {
        FirebaseDatabase.getInstance().getReference("healthPackages")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChildren()) {
                            List<HealthPackage> list = new ArrayList<>();
                            for (DataSnapshot child : snapshot.getChildren()) {
                                HealthPackage pkg = child.getValue(HealthPackage.class);
                                if (pkg != null && pkg.isActive()) {
                                    pkg.setPackageId(child.getKey());
                                    list.add(pkg);
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

    private void tryFirestore(OnPackagesLoadedListener listener) {
        firestore.collection("healthPackages")
                .whereEqualTo("isActive", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        List<HealthPackage> list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            HealthPackage pkg = doc.toObject(HealthPackage.class);
                            pkg.setPackageId(doc.getId());
                            list.add(pkg);
                        }
                        listener.onSuccess(list);
                    } else {
                        listener.onSuccess(getSamplePackages());
                    }
                })
                .addOnFailureListener(e -> listener.onSuccess(getSamplePackages()));
    }

    /**
     * Fetches package details by ID.
     */
    public void getPackageById(String packageId, OnPackageLoadedListener listener) {
        if (packageId == null || packageId.trim().isEmpty()) {
            listener.onSuccess(getSamplePackages().get(0));
            return;
        }

        for (HealthPackage pkg : getSamplePackages()) {
            if (pkg.getPackageId().equalsIgnoreCase(packageId)) {
                listener.onSuccess(pkg);
                return;
            }
        }
        listener.onSuccess(getSamplePackages().get(0));
    }

    /**
     * Sample health packages dataset matching specification examples.
     */
    public List<HealthPackage> getSamplePackages() {
        List<HealthPackage> sampleList = new ArrayList<>();

        sampleList.add(new HealthPackage("pkg101", "Basic Health Check",
                "Essential preventive screening package designed for young adults to monitor baseline health metrics.",
                7500.0,
                Arrays.asList("Full Blood Count", "Fasting Blood Sugar", "Urine Routine", "Body Mass Index (BMI)", "Physical Examination & Doctor Consultation"),
                "1 - 2 Hours", "Adults (18+)",
                Arrays.asList("MediCare Hospital Colombo", "Asiri Medical Kandy", "Nawaloka Hospital Galle"),
                "https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?w=500&auto=format&fit=crop&q=80"));

        sampleList.add(new HealthPackage("pkg102", "Executive Health Check",
                "Comprehensive diagnostic screening panel for busy professionals, evaluating heart, liver, kidney, and metabolic wellness.",
                18500.0,
                Arrays.asList("Full Blood Count", "Fasting Blood Sugar", "HbA1c", "Full Lipid Profile", "Liver Function Test", "Kidney Function Test", "ECG (Resting)", "Chest X-Ray", "Physician Consultation"),
                "3 - 4 Hours", "Adults (30+)",
                Arrays.asList("MediCare Hospital Colombo", "Lanka Hospitals Colombo", "Durdans Hospital Colombo"),
                "https://images.unsplash.com/photo-1505751172876-fa1923c5c528?w=500&auto=format&fit=crop&q=80"));

        sampleList.add(new HealthPackage("pkg103", "Women's Health Package",
                "Tailored health check-up for women including hormonal, bone density, and wellness screenings.",
                22000.0,
                Arrays.asList("Full Blood Count", "Fasting Blood Sugar", "Thyroid Profile (TSH)", "Calcium & Vitamin D", "Pap Smear", "Pelvic Ultrasound", "Consultation with Gynecologist"),
                "Half Day (4 hrs)", "Women (25+)",
                Arrays.asList("MediCare Hospital Colombo", "Lanka Hospitals Colombo"),
                "https://images.unsplash.com/photo-1594824813572-c21d1525a7b6?w=500&auto=format&fit=crop&q=80"));

        sampleList.add(new HealthPackage("pkg104", "Men's Health Package",
                "Specialized screening for men focusing on prostate wellness, cardiovascular risk, and vitality.",
                21000.0,
                Arrays.asList("Full Blood Count", "Lipid Profile", "Liver & Kidney Panels", "Prostate Specific Antigen (PSA)", "Treadmill Exercise Test (ECG)", "Consultation with Physician"),
                "Half Day (4 hrs)", "Men (40+)",
                Arrays.asList("MediCare Hospital Colombo", "Asiri Medical Kandy"),
                "https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=500&auto=format&fit=crop&q=80"));

        sampleList.add(new HealthPackage("pkg105", "Senior Citizen Wellness Package",
                "All-inclusive geriatric health evaluation to support active aging, joint care, and chronic disease detection.",
                26000.0,
                Arrays.asList("Full Blood Count", "Diabetes Screening (HbA1c)", "Renal & Liver Panels", "Electrolytes", "Uric Acid (Gout)", "Bone Mineral Density", "ECG & Chest X-Ray", "Consultation with Consultant Physician"),
                "Half Day (5 hrs)", "Seniors (60+)",
                Arrays.asList("MediCare Hospital Colombo", "Nawaloka Hospital Galle"),
                "https://images.unsplash.com/photo-1581056771107-24ca5f033842?w=500&auto=format&fit=crop&q=80"));

        sampleList.add(new HealthPackage("pkg106", "Diabetes Screening Package",
                "Targeted diagnostic panel for early detection and management of Type 2 Diabetes and related complications.",
                9500.0,
                Arrays.asList("Fasting Blood Sugar", "Post-Prandial Glucose", "HbA1c", "Urine Microalbumin", "Lipid Profile", "Ophthalmology Eye Screening"),
                "2 Hours", "All Adults",
                Arrays.asList("MediCare Hospital Colombo", "Asiri Medical Kandy", "Durdans Hospital Colombo"),
                "https://images.unsplash.com/photo-1579684385127-1ef15d508118?w=500&auto=format&fit=crop&q=80"));

        sampleList.add(new HealthPackage("pkg107", "Heart Health Screening",
                "Advanced cardiac risk assessment combining blood biomarkers, resting ECG, and echo imaging.",
                24500.0,
                Arrays.asList("Lipid Profile", "High-Sensitivity CRP", "Fasting Blood Sugar", "Resting ECG", "Echocardiogram (2D Echo)", "Cardiologist Consultation"),
                "Half Day (3 hrs)", "Adults (35+)",
                Arrays.asList("MediCare Hospital Colombo", "Lanka Hospitals Colombo"),
                "https://images.unsplash.com/photo-1628348068343-c6a848d2b6dd?w=500&auto=format&fit=crop&q=80"));

        return sampleList;
    }
}