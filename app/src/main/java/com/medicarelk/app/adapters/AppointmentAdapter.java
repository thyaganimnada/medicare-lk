package com.medicarelk.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.medicarelk.app.R;
import com.medicarelk.app.models.Appointment;
import com.medicarelk.app.utils.ImageUtils;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    public interface OnAppointmentClickListener {
        void onAppointmentClick(Appointment appointment);
    }

    private final List<Appointment> appointmentList;
    private final OnAppointmentClickListener listener;

    public AppointmentAdapter(List<Appointment> appointmentList, OnAppointmentClickListener listener) {
        this.appointmentList = appointmentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appt = appointmentList.get(position);

        holder.tvDoctorName.setText(appt.getDoctorNameSnapshot());
        holder.tvDoctorSpecialty.setText(appt.getSpecializationSnapshot());
        holder.tvHospital.setText(appt.getHospitalNameSnapshot());

        String dateTime = appt.getAppointmentDate() + " | " + appt.getAppointmentTime();
        holder.tvDateTime.setText(dateTime);
        holder.tvConsultationType.setText(appt.getConsultationType());

        // Bind Doctor / Lab / Package Image
        if (holder.ivDoctorAvatar != null) {
            String imgUrl = appt.getDoctorProfileImageUrlSnapshot();
            String docId = appt.getDoctorId();
            String docName = appt.getDoctorNameSnapshot();

            boolean isLabTest = "lab_center".equalsIgnoreCase(docId) ||
                    (docName != null && (docName.toLowerCase().contains("lab") || docName.toLowerCase().contains("diagnostic")));
            boolean isPackage = "health_package".equalsIgnoreCase(docId) ||
                    (docName != null && (docName.toLowerCase().contains("package") || docName.toLowerCase().contains("screening")));

            if (imgUrl != null && !imgUrl.trim().isEmpty()) {
                if (isLabTest || isPackage) {
                    ImageUtils.loadRoundedImage(holder.itemView.getContext(), imgUrl, holder.ivDoctorAvatar, 12);
                } else {
                    ImageUtils.loadProfileImage(holder.itemView.getContext(), imgUrl, holder.ivDoctorAvatar);
                }
            } else {
                if (isLabTest) {
                    holder.ivDoctorAvatar.setImageResource(R.drawable.ic_lab_test);
                } else if (isPackage) {
                    holder.ivDoctorAvatar.setImageResource(R.drawable.ic_health_package);
                } else {
                    holder.ivDoctorAvatar.setImageResource(R.drawable.ic_doctor);
                }
            }
        }

        String effectiveStatus = appt.getEffectiveStatus();
        if (Appointment.STATUS_COMPLETED.equalsIgnoreCase(effectiveStatus)) {
            holder.tvStatus.setText("Closed");
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.badge_completed_text));
        } else if (Appointment.STATUS_CANCELLED.equalsIgnoreCase(effectiveStatus)) {
            holder.tvStatus.setText("Cancelled");
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.badge_cancelled_text));
        } else {
            holder.tvStatus.setText("Upcoming");
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.badge_upcoming_text));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAppointmentClick(appt);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointmentList != null ? appointmentList.size() : 0;
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivDoctorAvatar;
        final TextView tvDoctorName;
        final TextView tvDoctorSpecialty;
        final TextView tvStatus;
        final TextView tvHospital;
        final TextView tvDateTime;
        final TextView tvConsultationType;

        AppointmentViewHolder(View itemView) {
            super(itemView);
            ivDoctorAvatar = itemView.findViewById(R.id.ivDoctorAvatar);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvDoctorSpecialty = itemView.findViewById(R.id.tvDoctorSpecialty);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvHospital = itemView.findViewById(R.id.tvHospital);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvConsultationType = itemView.findViewById(R.id.tvConsultationType);
        }
    }
}
