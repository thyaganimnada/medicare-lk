package com.medicarelk.app.adapters;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.medicarelk.app.R;
import com.medicarelk.app.models.Notification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    private final List<Notification> notificationList;
    private final OnNotificationClickListener listener;

    public NotificationAdapter(List<Notification> notificationList, OnNotificationClickListener listener) {
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notif = notificationList.get(position);

        holder.tvNotifTitle.setText(notif.getTitle());
        holder.tvNotifMessage.setText(notif.getMessage());

        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                notif.getCreatedAt(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
        );
        holder.tvNotifTime.setText(timeAgo);

        // Visual distinction for Read vs Unread
        if (notif.isRead()) {
            holder.viewUnreadDot.setVisibility(View.GONE);
            holder.tvNotifTitle.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary));
            holder.itemView.setAlpha(0.8f);
        } else {
            holder.viewUnreadDot.setVisibility(View.VISIBLE);
            holder.tvNotifTitle.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
            holder.itemView.setAlpha(1.0f);
        }

        // Configure type icon
        String type = notif.getType();
        if (Notification.TYPE_APPOINTMENT_CONFIRMED.equalsIgnoreCase(type) ||
                Notification.TYPE_APPOINTMENT_REMINDER.equalsIgnoreCase(type) ||
                Notification.TYPE_APPOINTMENT_RESCHEDULED.equalsIgnoreCase(type)) {
            holder.imgNotifIcon.setImageResource(R.drawable.ic_appointment);
        } else if (Notification.TYPE_APPOINTMENT_CANCELLED.equalsIgnoreCase(type)) {
            holder.imgNotifIcon.setImageResource(R.drawable.ic_appointment);
        } else if (Notification.TYPE_HEALTH_PROMOTION.equalsIgnoreCase(type) ||
                Notification.TYPE_SEASONAL_HEALTH_ALERT.equalsIgnoreCase(type)) {
            holder.imgNotifIcon.setImageResource(R.drawable.ic_health_package);
        } else if (Notification.TYPE_HEALTH_TIP.equalsIgnoreCase(type)) {
            holder.imgNotifIcon.setImageResource(R.drawable.ic_health);
        } else {
            holder.imgNotifIcon.setImageResource(R.drawable.ic_notification);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notif);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList != null ? notificationList.size() : 0;
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgNotifIcon;
        final TextView tvNotifTitle;
        final TextView tvNotifTime;
        final TextView tvNotifMessage;
        final View viewUnreadDot;

        NotificationViewHolder(View itemView) {
            super(itemView);
            imgNotifIcon = itemView.findViewById(R.id.imgNotifIcon);
            tvNotifTitle = itemView.findViewById(R.id.tvNotifTitle);
            tvNotifTime = itemView.findViewById(R.id.tvNotifTime);
            tvNotifMessage = itemView.findViewById(R.id.tvNotifMessage);
            viewUnreadDot = itemView.findViewById(R.id.viewUnreadDot);
        }
    }
}