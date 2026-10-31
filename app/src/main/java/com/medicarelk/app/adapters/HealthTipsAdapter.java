package com.medicarelk.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.medicarelk.app.databinding.ItemHealthTipBinding;
import com.medicarelk.app.models.HealthTip;

import java.util.List;

public class HealthTipsAdapter extends RecyclerView.Adapter<HealthTipsAdapter.TipViewHolder> {

    public interface OnHealthTipClickListener {
        void onHealthTipClick(HealthTip healthTip);
    }

    private final List<HealthTip> tipsList;
    private final OnHealthTipClickListener listener;

    public HealthTipsAdapter(List<HealthTip> tipsList) {
        this.tipsList = tipsList;
        this.listener = null;
    }

    public HealthTipsAdapter(List<HealthTip> tipsList, OnHealthTipClickListener listener) {
        this.tipsList = tipsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHealthTipBinding binding = ItemHealthTipBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TipViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TipViewHolder holder, int position) {
        HealthTip tip = tipsList.get(position);
        holder.binding.tvTipCategory.setText(tip.getCategory());
        holder.binding.tvTipTitle.setText(tip.getTitle());
        holder.binding.tvTipDescription.setText(tip.getSummary());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHealthTipClick(tip);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tipsList != null ? tipsList.size() : 0;
    }

    static class TipViewHolder extends RecyclerView.ViewHolder {
        final ItemHealthTipBinding binding;

        TipViewHolder(ItemHealthTipBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}