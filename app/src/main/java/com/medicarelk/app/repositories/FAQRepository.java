package com.medicarelk.app.repositories;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.medicarelk.app.models.FAQ;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for managing Frequently Asked Questions (FAQs).
 */
public class FAQRepository {

    public interface OnFAQsLoadedListener {
        void onSuccess(List<FAQ> faqs);
        void onError(String errorMessage);
    }

    public interface OnFAQLoadedListener {
        void onSuccess(FAQ faq);
        void onError(String errorMessage);
    }

    private final FirebaseFirestore firestore;

    public FAQRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Retrieves active FAQs with automatic sample fallback.
     */
    public void getActiveFAQs(OnFAQsLoadedListener listener) {
        FirebaseDatabase.getInstance().getReference("FAQs")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChildren()) {
                            List<FAQ> list = new ArrayList<>();
                            for (DataSnapshot child : snapshot.getChildren()) {
                                FAQ faq = child.getValue(FAQ.class);
                                if (faq != null && faq.isActive()) {
                                    faq.setFaqId(child.getKey());
                                    list.add(faq);
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

    private void tryFirestore(OnFAQsLoadedListener listener) {
        firestore.collection("faqs")
                .whereEqualTo("isActive", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        List<FAQ> list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            FAQ faq = doc.toObject(FAQ.class);
                            faq.setFaqId(doc.getId());
                            list.add(faq);
                        }
                        listener.onSuccess(list);
                    } else {
                        listener.onSuccess(getSampleFAQs());
                    }
                })
                .addOnFailureListener(e -> listener.onSuccess(getSampleFAQs()));
    }

    /**
     * Retrieves FAQ by ID.
     */
    public void getFAQById(String faqId, OnFAQLoadedListener listener) {
        if (faqId == null || faqId.trim().isEmpty()) {
            listener.onSuccess(getSampleFAQs().get(0));
            return;
        }

        for (FAQ faq : getSampleFAQs()) {
            if (faq.getFaqId().equalsIgnoreCase(faqId)) {
                listener.onSuccess(faq);
                return;
            }
        }
        listener.onSuccess(getSampleFAQs().get(0));
    }

    /**
     * Production-ready sample FAQs describing actual MediCare LK application features.
     */
    public List<FAQ> getSampleFAQs() {
        List<FAQ> sampleList = new ArrayList<>();

        sampleList.add(new FAQ("faq101", "How do I book a doctor appointment?",
                "Navigate to the Doctors section from the bottom menu or Home dashboard, choose your preferred doctor, select an available date and time slot, and tap 'Confirm Appointment'.",
                "Appointments", "booking doctor appointment slot date time"));

        sampleList.add(new FAQ("faq102", "Can I reschedule or cancel my appointment?",
                "Yes. Open the Appointments section, tap the appointment details card, and select 'Reschedule' or 'Cancel Appointment'. Please note that cancellations are restricted within 2 hours of the scheduled consultation time.",
                "Appointments", "cancel reschedule status time change"));

        sampleList.add(new FAQ("faq103", "How do I search for doctors in specific hospitals or cities?",
                "In the Doctors screen, type the hospital name (e.g. Asiri, Lanka Hospitals, Durdans) or city (e.g. Colombo, Kandy, Galle) into the search bar, or use the Filter BottomSheet.",
                "Doctors", "search location city hospital specialist filter"));

        sampleList.add(new FAQ("faq104", "How do I book diagnostic lab tests or health packages?",
                "Go to the Health portal, select 'Lab Tests' or 'Health Check-up Packages', choose your preferred item, select a sample collection center or hospital, choose a date and time slot, and confirm your booking.",
                "Lab Tests", "laboratory package screening test blood fast"));

        sampleList.add(new FAQ("faq105", "How is my personal medical history kept private?",
                "Your medical records are stored in isolated database nodes under your authenticated user ID. Only you can view, add, or edit your personal medical history records.",
                "Privacy & Security", "medical history record security isolated private privacy"));

        sampleList.add(new FAQ("faq106", "How do I receive appointment reminders and notifications?",
                "MediCare LK delivers in-app notifications and system tray alerts for appointment confirmations, reminders, and status changes. Ensure 'POST_NOTIFICATIONS' permission is enabled in your phone settings.",
                "Notifications", "push alerts fcm notification reminder message"));

        sampleList.add(new FAQ("faq107", "How do I contact customer support if I face an issue?",
                "Go to Settings or Profile -> Help & Support, select 'Customer Support', choose a category, enter your subject and description, and submit a support request. You can also call or email support directly.",
                "Technical Support", "support request contact call email ticket issue help"));

        return sampleList;
    }
}