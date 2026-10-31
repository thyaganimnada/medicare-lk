package com.medicarelk.app.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.medicarelk.app.utils.NotificationHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Firebase Cloud Messaging Service handling FCM tokens and remote pushes.
 * Stores tokens under users/{uid}/fcmTokens/{tokenId} securely.
 */
public class MediCareFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        if (token != null && !token.trim().isEmpty()) {
            saveTokenToUserAccount(token);
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String message = remoteMessage.getNotification().getBody();
            String type = remoteMessage.getData().get("type");
            String targetId = remoteMessage.getData().get("targetId");

            if (title == null) title = "MediCare LK";
            if (message == null) message = "You have a new update.";

            NotificationHelper.showSystemNotification(
                    this,
                    NotificationHelper.CHANNEL_APPOINTMENTS,
                    title,
                    message,
                    type != null ? type : "SYSTEM",
                    targetId
            );
        }
    }

    /**
     * Stores the current user's FCM token securely in Firebase database.
     */
    public static void saveTokenToUserAccount(String token) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || token == null || token.trim().isEmpty()) {
            return;
        }

        String uid = currentUser.getUid();
        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("token", token);
        tokenData.put("createdAt", System.currentTimeMillis());
        tokenData.put("updatedAt", System.currentTimeMillis());
        tokenData.put("platform", "Android");

        // Save token under Users/{uid}/fcmToken
        try {
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(uid).child("fcmToken").setValue(tokenData);
        } catch (Exception ignored) {}
    }
}