package com.medicarelk.app.utils;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.medicarelk.app.R;
import com.medicarelk.app.activities.AppointmentDetailsActivity;
import com.medicarelk.app.activities.HealthTipDetailsActivity;
import com.medicarelk.app.activities.NotificationsActivity;
import com.medicarelk.app.activities.PromotionDetailsActivity;

/**
 * Utility managing Android Notification Channels, POST_NOTIFICATIONS runtime permissions, and System Tray Notifications.
 * Tracks delivered notification IDs in SharedPreferences to prevent repeated popups on app launch.
 */
public class NotificationHelper {

    public static final String CHANNEL_APPOINTMENTS = "channel_appointments";
    public static final String CHANNEL_HEALTH = "channel_health";
    public static final String CHANNEL_PROMOTIONS = "channel_promotions";
    public static final String CHANNEL_SYSTEM = "channel_system";

    public static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

    private NotificationHelper() {
        // Private constructor
    }

    /**
     * Checks if a system tray notification ID has already been delivered on this device.
     */
    public static boolean isSystemNotificationDelivered(Context context, String notifId) {
        if (context == null || notifId == null || notifId.trim().isEmpty()) return false;
        SharedPreferences prefs = context.getSharedPreferences("medicare_delivered_notifs", Context.MODE_PRIVATE);
        return prefs.getBoolean("delivered_" + notifId.trim(), false);
    }

    /**
     * Marks a system tray notification ID as delivered in SharedPreferences.
     */
    public static void markSystemNotificationDelivered(Context context, String notifId) {
        if (context == null || notifId == null || notifId.trim().isEmpty()) return;
        SharedPreferences prefs = context.getSharedPreferences("medicare_delivered_notifs", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("delivered_" + notifId.trim(), true).apply();
    }

    /**
     * Registers Notification Channels for Android O (API 26+).
     */
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) return;

            // 1. Appointments Channel (High Importance)
            NotificationChannel appointmentsChannel = new NotificationChannel(
                    CHANNEL_APPOINTMENTS,
                    "Appointment Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            appointmentsChannel.setDescription("Notifications for appointment confirmations, reminders, and status changes.");

            // 2. Health Channel (High Importance)
            NotificationChannel healthChannel = new NotificationChannel(
                    CHANNEL_HEALTH,
                    "Health & Wellness",
                    NotificationManager.IMPORTANCE_HIGH
            );
            healthChannel.setDescription("Educational health tips, seasonal alerts, and wellness updates.");

            // 3. Promotions Channel (Default Importance)
            NotificationChannel promotionsChannel = new NotificationChannel(
                    CHANNEL_PROMOTIONS,
                    "Promotions & Offers",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            promotionsChannel.setDescription("Healthcare package offers and seasonal hospital campaign promotions.");

            // 4. System Channel (High Importance)
            NotificationChannel systemChannel = new NotificationChannel(
                    CHANNEL_SYSTEM,
                    "System Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            systemChannel.setDescription("Account security and general system notifications.");

            manager.createNotificationChannel(appointmentsChannel);
            manager.createNotificationChannel(healthChannel);
            manager.createNotificationChannel(promotionsChannel);
            manager.createNotificationChannel(systemChannel);
        }
    }

    /**
     * Checks if POST_NOTIFICATIONS permission is granted (Android 13+ / API 33+).
     */
    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    /**
     * Requests POST_NOTIFICATIONS runtime permission if required.
     */
    public static void requestNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission(activity)) {
                ActivityCompat.requestPermissions(
                        activity,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION
                );
            }
        }
    }

    /**
     * Displays a system tray notification with appropriate pending intent deep links and delivery tracking.
     */
    public static void showSystemNotification(Context context, String notifId, String channelId, String title,
                                               String message, String type, String targetId) {
        if (context == null) return;

        // Skip if this notification has already been delivered to system tray
        if (notifId != null && !notifId.trim().isEmpty() && isSystemNotificationDelivered(context, notifId)) {
            return;
        }

        createNotificationChannels(context);

        if (!hasNotificationPermission(context)) return;

        Intent intent;
        if ("APPOINTMENT".equalsIgnoreCase(type) || "APPOINTMENT_CONFIRMED".equalsIgnoreCase(type) ||
                "APPOINTMENT_CANCELLED".equalsIgnoreCase(type) || "APPOINTMENT_RESCHEDULED".equalsIgnoreCase(type)) {
            if (targetId != null && !targetId.isEmpty()) {
                intent = new Intent(context, AppointmentDetailsActivity.class);
                intent.putExtra("appointmentId", targetId);
            } else {
                intent = new Intent(context, NotificationsActivity.class);
            }
        } else if ("PROMOTION".equalsIgnoreCase(type) && targetId != null && !targetId.isEmpty()) {
            intent = new Intent(context, PromotionDetailsActivity.class);
            intent.putExtra("promotionId", targetId);
        } else if ("HEALTH_TIP".equalsIgnoreCase(type) && targetId != null && !targetId.isEmpty()) {
            intent = new Intent(context, HealthTipDetailsActivity.class);
            intent.putExtra("healthTipId", targetId);
        } else {
            intent = new Intent(context, NotificationsActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingFlags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) System.currentTimeMillis(),
                intent,
                pendingFlags
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((int) (System.currentTimeMillis() % 100000), builder.build());
            if (notifId != null && !notifId.trim().isEmpty()) {
                markSystemNotificationDelivered(context, notifId);
            }
        }
    }

    /**
     * Overloaded backward-compatible helper without explicit notifId.
     */
    public static void showSystemNotification(Context context, String channelId, String title,
                                               String message, String type, String targetId) {
        showSystemNotification(context, null, channelId, title, message, type, targetId);
    }
}