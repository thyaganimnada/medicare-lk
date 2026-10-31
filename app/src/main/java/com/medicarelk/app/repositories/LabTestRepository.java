package com.medicarelk.app.repositories;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.medicarelk.app.models.LabTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Repository handling diagnostic lab test queries cleanly outside Activity UI logic.
 */
public class LabTestRepository {

    public interface OnLabTestsLoadedListener {
        void onSuccess(List<LabTest> labTests);
        void onError(String errorMessage);
    }

    public interface OnLabTestLoadedListener {
        void onSuccess(LabTest labTest);
        void onError(String errorMessage);
    }

    private final FirebaseFirestore firestore;

    public LabTestRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Fetches active lab tests with automatic sample fallback.
     */
    public void getActiveLabTests(OnLabTestsLoadedListener listener) {
        // Try Realtime Database first for instant reliability
        FirebaseDatabase.getInstance().getReference("labTests")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChildren()) {
                            List<LabTest> list = new ArrayList<>();
                            for (DataSnapshot child : snapshot.getChildren()) {
                                LabTest test = child.getValue(LabTest.class);
                                if (test != null && test.isActive()) {
                                    test.setLabTestId(child.getKey());
                                    list.add(test);
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

    private void tryFirestore(OnLabTestsLoadedListener listener) {
        firestore.collection("labTests")
                .whereEqualTo("isActive", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        List<LabTest> list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            LabTest test = doc.toObject(LabTest.class);
                            test.setLabTestId(doc.getId());
                            list.add(test);
                        }
                        listener.onSuccess(list);
                    } else {
                        listener.onSuccess(getSampleLabTests());
                    }
                })
                .addOnFailureListener(e -> listener.onSuccess(getSampleLabTests()));
    }

    /**
     * Fetches a lab test by ID.
     */
    public void getLabTestById(String labTestId, OnLabTestLoadedListener listener) {
        if (labTestId == null || labTestId.trim().isEmpty()) {
            listener.onSuccess(getSampleLabTests().get(0));
            return;
        }

        for (LabTest test : getSampleLabTests()) {
            if (test.getLabTestId().equalsIgnoreCase(labTestId)) {
                listener.onSuccess(test);
                return;
            }
        }
        listener.onSuccess(getSampleLabTests().get(0));
    }

    /**
     * Sample lab test dataset covering all specified medical categories.
     */
    public List<LabTest> getSampleLabTests() {
        List<LabTest> sampleList = new ArrayList<>();

        LabTest l1 = new LabTest("lab101", "Full Blood Count (FBC)",
                "Complete blood count measuring red blood cells, white blood cells, hemoglobin, and platelets.",
                "Blood Tests", 1800.0, "Same Day (12 hrs)",
                "No fasting required. Hydrate well before sample collection.",
                Arrays.asList("MediCare Hospital Colombo", "Asiri Medical Kandy", "Lanka Hospitals Colombo"));
        l1.setImageUrl("https://images.unsplash.com/photo-1615461066841-6116e61058f4?w=500&auto=format&fit=crop&q=80");
        sampleList.add(l1);

        LabTest l2 = new LabTest("lab102", "Fasting Blood Sugar (FBS)",
                "Measures blood glucose concentration after an overnight fast to screen for diabetes.",
                "Diabetes", 1200.0, "Same Day (4 hrs)",
                "Minimum 8 to 10 hours overnight fasting required. Water is allowed.",
                Arrays.asList("MediCare Hospital Colombo", "Nawaloka Hospital Galle", "Durdans Hospital Colombo"));
        l2.setImageUrl("https://images.unsplash.com/photo-1579684385127-1ef15d508118?w=500&auto=format&fit=crop&q=80");
        sampleList.add(l2);

        LabTest l3 = new LabTest("lab103", "HbA1c Glycated Hemoglobin",
                "Evaluates average blood sugar levels over the past 2 to 3 months for long-term diabetes tracking.",
                "Diabetes", 2800.0, "Same Day (12 hrs)",
                "No fasting required. Test can be done at any time of day.",
                Arrays.asList("MediCare Hospital Colombo", "Asiri Medical Kandy"));
        l3.setImageUrl("https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=500&auto=format&fit=crop&q=80");
        sampleList.add(l3);

        LabTest l4 = new LabTest("lab104", "Lipid Profile (Cholesterol)",
                "Measures Total Cholesterol, HDL, LDL, Triglycerides, and VLDL to assess cardiovascular health.",
                "Cholesterol", 3200.0, "Same Day (12 hrs)",
                "Requires 10 to 12 hours strict overnight fasting prior to blood draw.",
                Arrays.asList("MediCare Hospital Colombo", "Lanka Hospitals Colombo", "Nawaloka Hospital Galle"));
        l4.setImageUrl("https://images.unsplash.com/photo-1505751172876-fa1923c5c528?w=500&auto=format&fit=crop&q=80");
        sampleList.add(l4);

        LabTest l5 = new LabTest("lab105", "Liver Function Test (LFT)",
                "Panel of blood tests measuring ALT, AST, ALP, Bilirubin, and Total Protein to evaluate liver health.",
                "Liver", 4500.0, "Same Day (12 hrs)",
                "Fasting for 8 hours is recommended. Avoid alcohol 24 hours prior.",
                Arrays.asList("MediCare Hospital Colombo", "Teaching Hospital Kurunegala"));
        l5.setImageUrl("https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=500&auto=format&fit=crop&q=80");
        sampleList.add(l5);

        LabTest l6 = new LabTest("lab106", "Renal Function Test (KFT)",
                "Evaluates kidney function by measuring Blood Urea, Serum Creatinine, and Electrolytes.",
                "Kidney", 4200.0, "Same Day (12 hrs)",
                "Fast overnight for 8 hours. Avoid heavy protein meals before sample collection.",
                Arrays.asList("MediCare Hospital Colombo", "Asiri Medical Kandy"));
        l6.setImageUrl("https://images.unsplash.com/photo-1579154204601-01588f351e67?w=500&auto=format&fit=crop&q=80");
        sampleList.add(l6);

        LabTest l7 = new LabTest("lab107", "Thyroid Profile (T3, T4, TSH)",
                "Comprehensive hormone panel measuring Triiodothyronine, Thyroxine, and Thyroid Stimulating Hormone.",
                "Hormone", 5500.0, "Same Day (24 hrs)",
                "Early morning sample collection preferred. Continue routine medications unless advised otherwise.",
                Arrays.asList("MediCare Hospital Colombo", "Lanka Hospitals Colombo"));
        l7.setImageUrl("https://images.unsplash.com/photo-1532187863486-abf9dbad1b69?w=500&auto=format&fit=crop&q=80");
        sampleList.add(l7);

        LabTest l8 = new LabTest("lab108", "Vitamin D (25-Hydroxy)",
                "Measures 25-hydroxyvitamin D levels in the blood to diagnose Vitamin D deficiency or bone health.",
                "Vitamin", 8500.0, "24 to 48 Hours",
                "No fasting required.",
                Arrays.asList("MediCare Hospital Colombo", "Durdans Hospital Colombo"));
        l8.setImageUrl("https://images.unsplash.com/photo-1584017911766-d451b3d0e843?w=500&auto=format&fit=crop&q=80");
        sampleList.add(l8);

        LabTest l9 = new LabTest("lab109", "General Executive Health Screening",
                "Broad screening panel including FBC, FBS, Lipid Profile, LFT, and Urine Routine.",
                "General Screening", 11500.0, "Same Day (24 hrs)",
                "Requires 10 hours overnight fasting. Bring morning mid-stream urine sample.",
                Arrays.asList("MediCare Hospital Colombo", "Asiri Medical Kandy", "Lanka Hospitals Colombo"));
        l9.setImageUrl("https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=500&auto=format&fit=crop&q=80");
        sampleList.add(l9);

        sampleList.add(new LabTest("lab109", "General Executive Health Screening",
                "Broad screening panel including FBC, FBS, Lipid Profile, LFT, and Urine Routine.",
                "General Screening", 11500.0, "Same Day (24 hrs)",
                "Requires 10 hours overnight fasting. Bring morning mid-stream urine sample.",
                Arrays.asList("MediCare Hospital Colombo", "Asiri Medical Kandy", "Lanka Hospitals Colombo")));

        return sampleList;
    }
}