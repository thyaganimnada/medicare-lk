package com.medicarelk.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.medicarelk.app.R;
import com.medicarelk.app.models.HealthPackage;
import com.medicarelk.app.utils.ImageUtils;

import java.util.List;

public class HealthPackageAdapter extends RecyclerView.Adapter<HealthPackageAdapter.PackageViewHolder> {

    public interface OnPackageClickListener {
        void onPackageClick(HealthPackage healthPackage);
    }

    private final List<HealthPackage> packageList;
    private final OnPackageClickListener listener;

    public HealthPackageAdapter(List<HealthPackage> packageList, OnPackageClickListener listener) {
        this.packageList = packageList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PackageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_health_package, parent, false);
        return new PackageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PackageViewHolder holder, int position) {
        HealthPackage pkg = packageList.get(position);

        holder.tvPackageName.setText(pkg.getName());
        holder.tvPrice.setText(pkg.getFormattedPrice());
        holder.tvDescription.setText(pkg.getDescription());
        holder.tvIncludedCount.setText(pkg.getIncludedCountText());
        holder.tvDuration.setText("⏱ " + pkg.getDuration());

        if (holder.ivPackageImage != null) {
            ImageUtils.loadRoundedImage(holder.itemView.getContext(), pkg.getImageUrl(), holder.ivPackageImage, 12);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPackageClick(pkg);
            }
        });
    }

    @Override
    public int getItemCount() {
        return packageList != null ? packageList.size() : 0;
    }

    static class PackageViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivPackageImage;
        final TextView tvPackageName;
        final TextView tvPrice;
        final TextView tvDescription;
        final TextView tvIncludedCount;
        final TextView tvDuration;

        PackageViewHolder(View itemView) {
            super(itemView);
            ivPackageImage = itemView.findViewById(R.id.ivPackageImage);
            tvPackageName = itemView.findViewById(R.id.tvPackageName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvIncludedCount = itemView.findViewById(R.id.tvIncludedCount);
            tvDuration = itemView.findViewById(R.id.tvDuration);
        }
    }
}
