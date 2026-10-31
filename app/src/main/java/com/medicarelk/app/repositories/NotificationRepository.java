package com.medicarelk.app.repositories;

import android.content.Context;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.medicarelk.app.models.Notification;
import com.medicarelk.app.utils.NotificationHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for managing patient in-app notifications.
 * Restricts queries strictly to the authenticated user's notifications node.
 */
public class NotificationRepository {

    public interface OnNotificationsLoadedListener {
        void onSuccess(List<Notification> notifications, int unreadCount);
        void onError(String errorMessage);
    }

    public interface OnNotificationUpdatedListener {
        void onSuccess();
        void onError(String errorMessage);
    }

    /**
     * Creates an in-app notification record under Notifications node and posts a system tray notification.
     */
    public void createNotification(Context context, Notification notification, OnNotificationUpdatedListener listener) {
        if (notification == null || notification.getUserId() == null || notification.getUserId().trim().isEmpty()) {
            if (listener != null) listener.onError("Invalid notification recipient.");
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Notifications");
        String id = ref.push().getKey();
        if (id == null) id = "NOTIF-" + System.currentTimeMillis();

        notification.setNotificationId(id);
        if (notification.getCreatedAt() <= 0) {
            notification.setCreatedAt(System.currentTimeMillis());
        }

        ref.child(id).setValue(notification)
                .addOnSuccessListener(aVoid -> {
                    if (context != null) {
                        String channelId = NotificationHelper.CHANNEL_APPOINTMENTS;
                        if (Notification.TYPE_HEALTH_PROMOTION.equalsIgnoreCase(notification.getType())) {
                            channelId = NotificationHelper.CHANNEL_PROMOTIONS;
                        } else if (Notification.TYPE_HEALTH_TIP.equalsIgnoreCase(notification.getType())) {
                            channelId = NotificationHelper.CHANNEL_HEALTH;
                        } else if (Notification.TYPE_SYSTEM.equalsIgnoreCase(notification.getType())) {
                            channelId = NotificationHelper.CHANNEL_SYSTEM;
                        }

                        NotificationHelper.showSystemNotification(
                                context.getApplicationContext(),
                                notification.getNotificationId(),
                                channelId,
                                notification.getTitle(),
                                notification.getMessage(),
                                notification.getType(),
                                notification.getAppointmentId()
                        );
                    }
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onError(e.getMessage());
                });
    }

    /**
     * Overloaded helper without context parameter.
     */
    public void createNotification(Notification notification, OnNotificationUpdatedListener listener) {
        createNotification(null, notification, listener);
    }

    /**
     * Listens for newly added in-app notifications in real-time and triggers mobile system tray notifications.
     */
    public void listenForLiveNotifications(Context context, String userId) {
        if (userId == null || userId.trim().isEmpty() || context == null) return;

        FirebaseDatabase.getInstance().getReference("Notifications")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                        if (!snapshot.exists()) return;
                        Notification notif = snapshot.getValue(Notification.class);
                        if (notif != null && userId.equals(notif.getUserId())) {
                            long now = System.currentTimeMillis();
                            // Fire system notification for recent notifications (created within last 30 seconds)
                            if (now - notif.getCreatedAt() < 30000L && !notif.isRead()) {
                                String channelId = NotificationHelper.CHANNEL_APPOINTMENTS;
                                if (Notification.TYPE_HEALTH_PROMOTION.equalsIgnoreCase(notif.getType())) {
                                    channelId = NotificationHelper.CHANNEL_PROMOTIONS;
                                } else if (Notification.TYPE_HEALTH_TIP.equalsIgnoreCase(notif.getType())) {
                                    channelId = NotificationHelper.CHANNEL_HEALTH;
                                } else if (Notification.TYPE_SYSTEM.equalsIgnoreCase(notif.getType())) {
                                    channelId = NotificationHelper.CHANNEL_SYSTEM;
                                }
                                NotificationHelper.showSystemNotification(
                                        context.getApplicationContext(),
                                        notif.getNotificationId(),
                                        channelId,
                                        notif.getTitle(),
                                        notif.getMessage(),
                                        notif.getType(),
                                        notif.getAppointmentId()
                                );
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot snapshot, String previousChildName) {}

                    @Override
                    public void onChildRemoved(DataSnapshot snapshot) {}

                    @Override
                    public void onChildMoved(DataSnapshot snapshot, String previousChildName) {}

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    /**
     * Retrieves notifications belonging strictly to the currently authenticated patient.
     */
    public void getPatientNotifications(String userId, OnNotificationsLoadedListener listener) {
        if (userId == null || userId.trim().isEmpty()) {
            listener.onError("Unauthenticated request.");
            return;
        }

        FirebaseDatabase.getInstance().getReference("Notifications")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Notification> list = new ArrayList<>();
                        int unreadCount = 0;

                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                Notification notif = child.getValue(Notification.class);
                                if (notif != null && userId.equals(notif.getUserId())) {
                                    notif.setNotificationId(child.getKey());
                                    list.add(notif);
                                    if (!notif.isRead()) {
                                        unreadCount++;
                                    }
                                }
                            }
                        }

                        // Sort newest first
                        list.sort((n1, n2) -> Long.compare(n2.getCreatedAt(), n1.getCreatedAt()));

                        // If empty, inject initial welcome/system notification
                        if (list.isEmpty()) {
                            Notification welcomeNotif = new Notification(
                                    "notif_welcome",
                                    userId,
                                    Notification.TYPE_SYSTEM,
                                    "Welcome to MediCare LK!",
                                    "Your healthcare portal is set up. You will receive real-time appointment and health updates here.",
                                    "", "", ""
                            );
                            list.add(welcomeNotif);
                            unreadCount = 1;
                        }

                        listener.onSuccess(list, unreadCount);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError("Error loading notifications: " + error.getMessage());
                    }
                });
    }

    /**
     * Marks a single notification as read.
     */
    public void markNotificationAsRead(String notificationId, OnNotificationUpdatedListener listener) {
        if (notificationId == null || notificationId.trim().isEmpty()) {
            if (listener != null) listener.onSuccess();
            return;
        }

        FirebaseDatabase.getInstance().getReference("Notifications")
                .child(notificationId).child("read").setValue(true)
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onError(e.getMessage());
                });
    }

    /**
     * Marks all notifications belonging to the user as read.
     */
    public void markAllNotificationsAsRead(String userId, OnNotificationUpdatedListener listener) {
        if (userId == null || userId.trim().isEmpty()) {
            if (listener != null) listener.onSuccess();
            return;
        }

        FirebaseDatabase.getInstance().getReference("Notifications")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                Notification notif = child.getValue(Notification.class);
                                if (notif != null && userId.equals(notif.getUserId()) && !notif.isRead()) {
                                    child.getRef().child("read").setValue(true);
                                }
                            }
                        }
                        if (listener != null) listener.onSuccess();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        if (listener != null) listener.onError(error.getMessage());
                    }
                });
    }
}