package com.medicarelk.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.medicarelk.app.R;
import com.medicarelk.app.models.Doctor;
import com.medicarelk.app.utils.ImageUtils;

import java.util.List;
import java.util.Locale;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    public interface OnDoctorClickListener {
        void onDoctorClick(Doctor doctor);
    }

    private final List<Doctor> doctorList;
    private final OnDoctorClickListener listener;

    public DoctorAdapter(List<Doctor> doctorList, OnDoctorClickListener listener) {
        this.doctorList = doctorList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doctor, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctor doctor = doctorList.get(position);

        if (holder.tvDoctorName != null) holder.tvDoctorName.setText(doctor.getFullName());
        if (holder.tvDoctorSpecialty != null) holder.tvDoctorSpecialty.setText(doctor.getSpecialization());

        String hospitalLoc = doctor.getHospitalName() + " - " + doctor.getLocation();
        if (holder.tvHospitalLocation != null) holder.tvHospitalLocation.setText(hospitalLoc);

        if (holder.tvRating != null) {
            holder.tvRating.setText(String.format(Locale.US, "%.1f", doctor.getRating()));
        }

        if (holder.tvConsultationFee != null) {
            holder.tvConsultationFee.setText(doctor.getFormattedFee());
        }

        if (holder.tvConsultationType != null) {
            holder.tvConsultationType.setText(doctor.getConsultationTypesText());
        }

        if (holder.tvAvailabilityStatus != null) {
            holder.tvAvailabilityStatus.setText(doctor.getAvailabilityStatus());
        }

        if (holder.ivDoctorImage != null) {
            ImageUtils.loadRoundedImage(holder.itemView.getContext(), doctor.getProfileImageUrl(), holder.ivDoctorImage, 12);
        }

        if (holder.btnViewProfile != null) {
            holder.btnViewProfile.setOnClickListener(v -> {
                if (listener != null) listener.onDoctorClick(doctor);
            });
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDoctorClick(doctor);
        });
    }

    @Override
    public int getItemCount() {
        return doctorList != null ? doctorList.size() : 0;
    }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivDoctorImage;
        final TextView tvDoctorName;
        final TextView tvDoctorSpecialty;
        final TextView tvHospitalLocation;
        final TextView tvRating;
        final TextView tvConsultationFee;
        final TextView tvConsultationType;
        final TextView tvAvailabilityStatus;
        final Button btnViewProfile;

        DoctorViewHolder(View itemView) {
            super(itemView);
            ivDoctorImage = itemView.findViewById(R.id.ivDoctorImage);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvDoctorSpecialty = itemView.findViewById(R.id.tvDoctorSpecialty);
            tvHospitalLocation = itemView.findViewById(R.id.tvHospitalLocation);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvConsultationFee = itemView.findViewById(R.id.tvConsultationFee);
            tvConsultationType = itemView.findViewById(R.id.tvConsultationType);
            tvAvailabilityStatus = itemView.findViewById(R.id.tvAvailabilityStatus);
            btnViewProfile = itemView.findViewById(R.id.btnViewProfile);
        }
    }
}