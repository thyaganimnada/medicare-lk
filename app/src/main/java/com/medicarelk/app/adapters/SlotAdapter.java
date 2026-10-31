package com.medicarelk.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.medicarelk.app.R;

import java.util.List;

public class SlotAdapter extends RecyclerView.Adapter<SlotAdapter.SlotViewHolder> {

    public interface OnSlotClickListener {
        void onSlotClick(String slotTime);
    }

    private final List<String> slotList;
    private String selectedSlot = "";
    private final OnSlotClickListener listener;

    public SlotAdapter(List<String> slotList, OnSlotClickListener listener) {
        this.slotList = slotList;
        this.listener = listener;
    }

    public void setSelectedSlot(String slot) {
        this.selectedSlot = slot;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slot, parent, false);
        return new SlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
        String slot = slotList.get(position);
        holder.tvSlotTime.setText(slot);

        boolean isSelected = slot.equalsIgnoreCase(selectedSlot);
        if (isSelected) {
            holder.cardSlot.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.brand_blue));
            holder.tvSlotTime.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
        } else {
            holder.cardSlot.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
            holder.tvSlotTime.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
        }

        holder.itemView.setOnClickListener(v -> {
            selectedSlot = slot;
            notifyDataSetChanged();
            if (listener != null) {
                listener.onSlotClick(slot);
            }
        });
    }

    @Override
    public int getItemCount() {
        return slotList != null ? slotList.size() : 0;
    }

    static class SlotViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView cardSlot;
        final TextView tvSlotTime;

        SlotViewHolder(View itemView) {
            super(itemView);
            cardSlot = itemView.findViewById(R.id.cardSlot);
            tvSlotTime = itemView.findViewById(R.id.tvSlotTime);
        }
    }
}