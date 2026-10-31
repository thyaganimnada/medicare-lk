package com.medicarelk.app.repositories;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.medicarelk.app.models.HealthTip;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository handling general educational health tips outside UI activity code.
 */
public class HealthTipRepository {

    public interface OnHealthTipsLoadedListener {
        void onSuccess(List<HealthTip> healthTips);
        void onError(String errorMessage);
    }

    public interface OnHealthTipLoadedListener {
        void onSuccess(HealthTip healthTip);
        void onError(String errorMessage);
    }

    private final FirebaseFirestore firestore;

    public HealthTipRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Retrieves active health tips with automatic sample fallback.
     */
    public void getActiveHealthTips(OnHealthTipsLoadedListener listener) {
        FirebaseDatabase.getInstance().getReference("healthTips")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChildren()) {
                            List<HealthTip> list = new ArrayList<>();
                            for (DataSnapshot child : snapshot.getChildren()) {
                                HealthTip tip = child.getValue(HealthTip.class);
                                if (tip != null && tip.isActive()) {
                                    tip.setTipId(child.getKey());
                                    list.add(tip);
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

    private void tryFirestore(OnHealthTipsLoadedListener listener) {
        firestore.collection("healthTips")
                .whereEqualTo("isActive", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        List<HealthTip> list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            HealthTip tip = doc.toObject(HealthTip.class);
                            tip.setTipId(doc.getId());
                            list.add(tip);
                        }
                        listener.onSuccess(list);
                    } else {
                        listener.onSuccess(getSampleHealthTips());
                    }
                })
                .addOnFailureListener(e -> listener.onSuccess(getSampleHealthTips()));
    }

    /**
     * Retrieves health tip by ID.
     */
    public void getHealthTipById(String tipId, OnHealthTipLoadedListener listener) {
        if (tipId == null || tipId.trim().isEmpty()) {
            listener.onSuccess(getSampleHealthTips().get(0));
            return;
        }

        for (HealthTip tip : getSampleHealthTips()) {
            if (tip.getTipId().equalsIgnoreCase(tipId)) {
                listener.onSuccess(tip);
                return;
            }
        }
        listener.onSuccess(getSampleHealthTips().get(0));
    }

    /**
     * Educational sample health tips across required medical categories.
     */
    public List<HealthTip> getSampleHealthTips() {
        List<HealthTip> sampleList = new ArrayList<>();

        sampleList.add(new HealthTip("tip101", "Nutrition", "Stay Hydrated Daily",
                "Drink at least 8 to 10 glasses of water every day to support kidney function, skin vitality, and optimal digestion. Proper hydration helps regulate body temperature, flush out cellular toxins, and prevent daily fatigue."));

        sampleList.add(new HealthTip("tip102", "Exercise", "30 Minutes of Daily Exercise",
                "Engaging in 30 minutes of moderate aerobic exercise like brisk walking, cycling, or swimming 5 days a week reduces cardiovascular disease risk, improves blood circulation, and maintains healthy blood pressure."));

        sampleList.add(new HealthTip("tip103", "Mental Wellbeing", "Mindfulness and Stress Reduction", "Practicing 10 minutes of deep breathing or guided meditation daily significantly lowers cortisol stress hormone levels and mental anxiety."));

        sampleList.add(new HealthTip("tip104", "Sleep", "Prioritize 7-8 Hours of Restful Sleep",
                "Consistent sleep schedules allow brain cell repair, hormone regulation, and immune system strengthening. Avoid blue light screens 1 hour before bedtime for deeper sleep quality."));

        sampleList.add(new HealthTip("tip105", "Preventive Care", "Annual Health Screening Importance",
                "Regular annual check-ups enable early detection of asymptomatic conditions like high blood pressure, elevated cholesterol, and early-stage diabetes."));

        sampleList.add(new HealthTip("tip106", "General Wellness", "Balanced Plate Nutrition",
                "Fill half your plate with colorful vegetables and fruits, one quarter with lean protein, and one quarter with whole grains for sustained daily energy."));

        return sampleList;
    }
}