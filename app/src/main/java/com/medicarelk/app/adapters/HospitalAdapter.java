package com.medicarelk.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.medicarelk.app.R;
import com.medicarelk.app.models.Hospital;

import java.util.List;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder> {

    public interface OnHospitalClickListener {
        void onHospitalClick(Hospital hospital);
    }

    private final List<Hospital> hospitalList;
    private final OnHospitalClickListener listener;

    public HospitalAdapter(List<Hospital> hospitalList, OnHospitalClickListener listener) {
        this.hospitalList = hospitalList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HospitalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hospital, parent, false);
        return new HospitalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HospitalViewHolder holder, int position) {
        Hospital h = hospitalList.get(position);

        holder.tvHospitalName.setText(h.getName());
        String locText = h.getCity() + ", " + h.getLocation();
        holder.tvLocation.setText(locText);
        holder.tvAddress.setText(h.getAddress());
        holder.tvServices.setText(h.getServicesText());

        if (h.isEmergencyAvailable()) {
            holder.tvEmergencyBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvEmergencyBadge.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHospitalClick(h);
            }
        });
    }

    @Override
    public int getItemCount() {
        return hospitalList != null ? hospitalList.size() : 0;
    }

    static class HospitalViewHolder extends RecyclerView.ViewHolder {
        final TextView tvHospitalName;
        final TextView tvLocation;
        final TextView tvAddress;
        final TextView tvServices;
        final TextView tvEmergencyBadge;

        HospitalViewHolder(View itemView) {
            super(itemView);
            tvHospitalName = itemView.findViewById(R.id.tvHospitalName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvServices = itemView.findViewById(R.id.tvServices);
            tvEmergencyBadge = itemView.findViewById(R.id.tvEmergencyBadge);
        }
    }
}