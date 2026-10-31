package com.medicarelk.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.medicarelk.app.databinding.ItemDoctorSpecialtyBinding;
import java.util.List;

public class SpecialtyAdapter extends RecyclerView.Adapter<SpecialtyAdapter.SpecialtyViewHolder> {

    public interface OnSpecialtyClickListener {
        void onSpecialtyClick(String specialty);
    }

    private final List<String> specialtyList;
    private final OnSpecialtyClickListener listener;

    public SpecialtyAdapter(List<String> specialtyList, OnSpecialtyClickListener listener) {
        this.specialtyList = specialtyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SpecialtyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDoctorSpecialtyBinding binding = ItemDoctorSpecialtyBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SpecialtyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SpecialtyViewHolder holder, int position) {
        String specialty = specialtyList.get(position);
        holder.binding.tvSpecialtyName.setText(specialty);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSpecialtyClick(specialty);
            }
        });
    }

    @Override
    public int getItemCount() {
        return specialtyList != null ? specialtyList.size() : 0;
    }

    static class SpecialtyViewHolder extends RecyclerView.ViewHolder {
        final ItemDoctorSpecialtyBinding binding;

        SpecialtyViewHolder(ItemDoctorSpecialtyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}