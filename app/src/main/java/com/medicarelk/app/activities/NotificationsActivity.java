package com.medicarelk.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.medicarelk.app.R;
import com.medicarelk.app.adapters.NotificationAdapter;
import com.medicarelk.app.models.Notification;
import com.medicarelk.app.repositories.NotificationRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Notifications Center Activity displaying in-app patient notifications and status updates.
 * Automatically marks notifications as read upon opening to prevent repeated alerts.
 */
public class NotificationsActivity extends AppCompatActivity {

    private NotificationRepository notificationRepository;
    private NotificationAdapter adapter;

    private List<Notification> notificationList = new ArrayList<>();

    private RecyclerView rvNotifications;
    private ProgressBar progressBarNotifications;
    private View layoutEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Security check
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_notifications);

        View rootView = findViewById(R.id.rootNotifications);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        rvNotifications = findViewById(R.id.rvNotifications);
        progressBarNotifications = findViewById(R.id.progressBarNotifications);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);

        notificationRepository = new NotificationRepository();

        setupRecyclerView();
        setupListeners();

        loadNotifications(currentUser.getUid());

        // Automatically mark all notifications as read in Firebase upon viewing
        notificationRepository.markAllNotificationsAsRead(currentUser.getUid(), null);
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(notificationList, notif -> {
            // Mark notification as read
            notificationRepository.markNotificationAsRead(notif.getNotificationId(), null);
            notif.setRead(true);
            adapter.notifyDataSetChanged();

            // Deep-link navigation
            String type = notif.getType();
            if (Notification.TYPE_APPOINTMENT_CONFIRMED.equalsIgnoreCase(type) ||
                    Notification.TYPE_APPOINTMENT_REMINDER.equalsIgnoreCase(type) ||
                    Notification.TYPE_APPOINTMENT_RESCHEDULED.equalsIgnoreCase(type) ||
                    Notification.TYPE_APPOINTMENT_CANCELLED.equalsIgnoreCase(type)) {
                if (notif.getAppointmentId() != null && !notif.getAppointmentId().isEmpty()) {
                    Intent intent = new Intent(NotificationsActivity.this, AppointmentDetailsActivity.class);
                    intent.putExtra("appointmentId", notif.getAppointmentId());
                    startActivity(intent);
                }
            } else if (Notification.TYPE_HEALTH_PROMOTION.equalsIgnoreCase(type) ||
                    Notification.TYPE_SEASONAL_HEALTH_ALERT.equalsIgnoreCase(type)) {
                Intent intent = new Intent(NotificationsActivity.this, PromotionsActivity.class);
                startActivity(intent);
            } else if (Notification.TYPE_HEALTH_TIP.equalsIgnoreCase(type)) {
                Intent intent = new Intent(NotificationsActivity.this, HealthTipsActivity.class);
                startActivity(intent);
            }
        });

        if (rvNotifications != null) {
            rvNotifications.setAdapter(adapter);
        }
    }

    private void loadNotifications(String userId) {
        showLoading(true);

        notificationRepository.getPatientNotifications(userId, new NotificationRepository.OnNotificationsLoadedListener() {
            @Override
            public void onSuccess(List<Notification> notifications, int unreadCount) {
                showLoading(false);
                notificationList.clear();
                if (notifications != null) {
                    notificationList.addAll(notifications);
                }

                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }

                if (notificationList.isEmpty()) {
                    if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
                    if (rvNotifications != null) rvNotifications.setVisibility(View.GONE);
                } else {
                    if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
                    if (rvNotifications != null) rvNotifications.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                Toast.makeText(NotificationsActivity.this, "Unable to load notifications.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        TextView btnMarkAllRead = findViewById(R.id.btnMarkAllRead);
        if (btnMarkAllRead != null) {
            btnMarkAllRead.setOnClickListener(v -> {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    notificationRepository.markAllNotificationsAsRead(user.getUid(), new NotificationRepository.OnNotificationUpdatedListener() {
                        @Override
                        public void onSuccess() {
                            for (Notification n : notificationList) {
                                n.setRead(true);
                            }
                            if (adapter != null) adapter.notifyDataSetChanged();
                            Toast.makeText(NotificationsActivity.this, "All notifications marked as read.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String errorMessage) {}
                    });
                }
            });
        }
    }

    private void showLoading(boolean isLoading) {
        if (progressBarNotifications != null) {
            progressBarNotifications.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(NotificationsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}