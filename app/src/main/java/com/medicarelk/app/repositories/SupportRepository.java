package com.medicarelk.app.repositories;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.medicarelk.app.models.Notification;
import com.medicarelk.app.models.SupportMessage;
import com.medicarelk.app.models.SupportRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository managing patient customer support requests and messages.
 */
public class SupportRepository {

    public interface OnSupportRequestCreatedListener {
        void onSuccess(String requestId);
        void onError(String errorMessage);
    }

    public interface OnSupportRequestsLoadedListener {
        void onSuccess(List<SupportRequest> requests);
        void onError(String errorMessage);
    }

    public interface OnSupportRequestLoadedListener {
        void onSuccess(SupportRequest request);
        void onError(String errorMessage);
    }

    public interface OnSupportMessagesLoadedListener {
        void onSuccess(List<SupportMessage> messages);
        void onError(String errorMessage);
    }

    private final NotificationRepository notificationRepository;

    public SupportRepository() {
        this.notificationRepository = new NotificationRepository();
    }

    /**
     * Creates a new support request and triggers an in-app system notification.
     */
    public void createSupportRequest(SupportRequest request, OnSupportRequestCreatedListener listener) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            listener.onError("Unauthenticated request. Please log in.");
            return;
        }

        String userId = user.getUid();
        request.setUserId(userId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("SupportRequests");
        String reqId = ref.push().getKey();
        if (reqId == null) {
            reqId = "REQ-" + System.currentTimeMillis();
        }

        request.setRequestId(reqId);
        request.setCreatedAt(System.currentTimeMillis());
        request.setUpdatedAt(System.currentTimeMillis());
        request.setLastResponseAt(System.currentTimeMillis());

        final String finalReqId = reqId;

        ref.child(reqId).setValue(request)
                .addOnSuccessListener(aVoid -> {
                    // Trigger in-app confirmation notification
                    Notification notif = new Notification(
                            "",
                            userId,
                            Notification.TYPE_SYSTEM,
                            "Support Request Submitted",
                            "Your support request #" + finalReqId.substring(Math.max(0, finalReqId.length() - 6)) + " has been submitted successfully.",
                            "", "", ""
                    );
                    notificationRepository.createNotification(notif, null);

                    listener.onSuccess(finalReqId);
                })
                .addOnFailureListener(e -> listener.onError("Failed to submit support request: " + e.getMessage()));
    }

    /**
     * Retrieves support requests belonging ONLY to the authenticated patient.
     */
    public void getPatientSupportRequests(String userId, OnSupportRequestsLoadedListener listener) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || !user.getUid().equals(userId)) {
            listener.onError("Unauthorized access.");
            return;
        }

        FirebaseDatabase.getInstance().getReference("SupportRequests")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<SupportRequest> list = new ArrayList<>();
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                SupportRequest req = child.getValue(SupportRequest.class);
                                if (req != null && userId.equals(req.getUserId())) {
                                    req.setRequestId(child.getKey());
                                    list.add(req);
                                }
                            }
                        }

                        // Sort newest first
                        list.sort((r1, r2) -> Long.compare(r2.getCreatedAt(), r1.getCreatedAt()));

                        listener.onSuccess(list);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError("Failed to load support requests: " + error.getMessage());
                    }
                });
    }

    /**
     * Retrieves support request details with strict ownership verification.
     */
    public void getSupportRequestById(String requestId, OnSupportRequestLoadedListener listener) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            listener.onError("Unauthenticated request.");
            return;
        }

        if (requestId == null || requestId.trim().isEmpty()) {
            listener.onError("Invalid request ID.");
            return;
        }

        FirebaseDatabase.getInstance().getReference("SupportRequests").child(requestId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            SupportRequest req = snapshot.getValue(SupportRequest.class);
                            if (req != null) {
                                // Ownership check
                                if (!user.getUid().equals(req.getUserId())) {
                                    listener.onError("You are not authorized to view this support request.");
                                    return;
                                }

                                req.setRequestId(requestId);
                                listener.onSuccess(req);
                                return;
                            }
                        }
                        listener.onError("Support request not found.");
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError("Error loading support request details.");
                    }
                });
    }

    /**
     * Retrieves messages for a support request.
     */
    public void getSupportMessages(String requestId, OnSupportMessagesLoadedListener listener) {
        if (requestId == null || requestId.trim().isEmpty()) {
            listener.onSuccess(new ArrayList<>());
            return;
        }

        FirebaseDatabase.getInstance().getReference("SupportRequests")
                .child(requestId).child("messages")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<SupportMessage> messages = new ArrayList<>();
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                SupportMessage msg = child.getValue(SupportMessage.class);
                                if (msg != null) {
                                    msg.setMessageId(child.getKey());
                                    messages.add(msg);
                                }
                            }
                        }
                        listener.onSuccess(messages);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onSuccess(new ArrayList<>());
                    }
                });
    }
}