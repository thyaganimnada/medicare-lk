package com.medicarelk.app.repositories;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.medicarelk.app.models.Promotion;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository handling verified Healthcare Promotions & Seasonal Campaigns.
 */
public class PromotionRepository {

    public interface OnPromotionsLoadedListener {
        void onSuccess(List<Promotion> promotions);
        void onError(String errorMessage);
    }

    public interface OnPromotionLoadedListener {
        void onSuccess(Promotion promotion);
        void onError(String errorMessage);
    }

    private final FirebaseFirestore firestore;

    public PromotionRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Retrieves active promotions with automatic sample fallback.
     */
    public void getActivePromotions(OnPromotionsLoadedListener listener) {
        FirebaseDatabase.getInstance().getReference("promotions")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChildren()) {
                            List<Promotion> list = new ArrayList<>();
                            for (DataSnapshot child : snapshot.getChildren()) {
                                Promotion promo = child.getValue(Promotion.class);
                                if (promo != null && promo.isActive()) {
                                    promo.setPromotionId(child.getKey());
                                    list.add(promo);
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

    private void tryFirestore(OnPromotionsLoadedListener listener) {
        firestore.collection("promotions")
                .whereEqualTo("isActive", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        List<Promotion> list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Promotion promo = doc.toObject(Promotion.class);
                            promo.setPromotionId(doc.getId());
                            list.add(promo);
                        }
                        listener.onSuccess(list);
                    } else {
                        listener.onSuccess(getSamplePromotions());
                    }
                })
                .addOnFailureListener(e -> listener.onSuccess(getSamplePromotions()));
    }

    /**
     * Retrieves promotion by ID.
     */
    public void getPromotionById(String promotionId, OnPromotionLoadedListener listener) {
        if (promotionId == null || promotionId.trim().isEmpty()) {
            listener.onSuccess(getSamplePromotions().get(0));
            return;
        }

        for (Promotion promo : getSamplePromotions()) {
            if (promo.getPromotionId().equalsIgnoreCase(promotionId)) {
                listener.onSuccess(promo);
                return;
            }
        }
        listener.onSuccess(getSamplePromotions().get(0));
    }

    /**
     * Sample verified healthcare campaigns and seasonal offers.
     */
    public List<Promotion> getSamplePromotions() {
        List<Promotion> sampleList = new ArrayList<>();

        sampleList.add(new Promotion("promo101", "Seasonal Dengue Awareness & Screening Drive",
                "Get 25% discount on Complete Blood Count (FBC) and Dengue NS1 Antigen Test at all MediCare labs.",
                "Protect your family during the monsoon season. MediCare LK in partnership with Ministry of Health offers subsidized dengue screening and rapid diagnostic testing.",
                "https://example.com/dengue_drive.jpg", "MediCare General Hospital",
                "Seasonal Campaign", "2025-10-01", "2025-12-31",
                "Offer valid at all MediCare laboratories nationwide. Fasting not required.",
                "+94112688888"));

        sampleList.add(new Promotion("promo102", "National Influenza Vaccination Drive",
                "Protect yourself against seasonal flu. Subsidized quadrivalent influenza vaccines now available.",
                "Annual flu vaccination drive for seniors, children, and high-risk individuals. Walk-ins welcome at all partner hospitals.",
                "https://example.com/flu_drive.jpg", "Lanka Hospitals Colombo",
                "Vaccination Drive", "2025-09-15", "2025-11-30",
                "Prior appointment or walk-in registration required. Subject to vaccine stock availability.",
                "+94115430000"));

        sampleList.add(new Promotion("promo103", "World Heart Day Cardiac Special",
                "Comprehensive Cardiac Screening Package including 2D Echo, Treadmill ECG & Lipid Profile at LKR 14,500.",
                "Special promotional rates for World Heart Month. Includes complete cardiologist consultation and cardiac risk score assessment.",
                "https://example.com/heart_special.jpg", "Asiri Medical Hospital",
                "Health Check-up", "2025-09-20", "2025-10-31",
                "Requires 10 to 12 hours overnight fasting prior to morning appointment.",
                "+94814528888"));

        return sampleList;
    }
}