package com.medicarelk.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.medicarelk.app.R;
import com.medicarelk.app.models.Promotion;

import java.util.List;

public class PromotionAdapter extends RecyclerView.Adapter<PromotionAdapter.PromotionViewHolder> {

    public interface OnPromotionClickListener {
        void onPromotionClick(Promotion promotion);
    }

    private final List<Promotion> promotionList;
    private final OnPromotionClickListener listener;

    public PromotionAdapter(List<Promotion> promotionList, OnPromotionClickListener listener) {
        this.promotionList = promotionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PromotionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_promotion, parent, false);
        return new PromotionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PromotionViewHolder holder, int position) {
        Promotion promo = promotionList.get(position);

        holder.tvPromoTitle.setText(promo.getTitle());
        holder.tvPromoShortDesc.setText(promo.getShortDescription());
        holder.tvPromoType.setText(promo.getPromotionType());
        holder.tvHospitalName.setText(promo.getHospitalName());

        String validity = "⏱ Valid until: " + (promo.getEndDate() != null ? promo.getEndDate() : "Ongoing");
        holder.tvValidity.setText(validity);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPromotionClick(promo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return promotionList != null ? promotionList.size() : 0;
    }

    static class PromotionViewHolder extends RecyclerView.ViewHolder {
        final TextView tvPromoType;
        final TextView tvHospitalName;
        final TextView tvPromoTitle;
        final TextView tvPromoShortDesc;
        final TextView tvValidity;

        PromotionViewHolder(View itemView) {
            super(itemView);
            tvPromoType = itemView.findViewById(R.id.tvPromoType);
            tvHospitalName = itemView.findViewById(R.id.tvHospitalName);
            tvPromoTitle = itemView.findViewById(R.id.tvPromoTitle);
            tvPromoShortDesc = itemView.findViewById(R.id.tvPromoShortDesc);
            tvValidity = itemView.findViewById(R.id.tvValidity);
        }
    }
}