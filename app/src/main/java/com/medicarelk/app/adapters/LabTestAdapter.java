package com.medicarelk.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.medicarelk.app.R;
import com.medicarelk.app.models.LabTest;
import com.medicarelk.app.utils.ImageUtils;

import java.util.List;

public class LabTestAdapter extends RecyclerView.Adapter<LabTestAdapter.LabTestViewHolder> {

    public interface OnLabTestClickListener {
        void onLabTestClick(LabTest labTest);
    }

    private final List<LabTest> labTestList;
    private final OnLabTestClickListener listener;

    public LabTestAdapter(List<LabTest> labTestList, OnLabTestClickListener listener) {
        this.labTestList = labTestList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LabTestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lab_test, parent, false);
        return new LabTestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LabTestViewHolder holder, int position) {
        LabTest test = labTestList.get(position);

        holder.tvTestName.setText(test.getName());
        holder.tvCategory.setText(test.getCategory());
        holder.tvPrice.setText(test.getFormattedPrice());
        holder.tvDescription.setText(test.getDescription());
        holder.tvResultTime.setText("⏱ " + test.getResultTime());

        String prep = test.getPreparationInstructions();
        if (prep != null && prep.toLowerCase().contains("fasting") && !prep.toLowerCase().contains("no fasting")) {
            holder.tvPreparation.setText("Fasting Required");
        } else {
            holder.tvPreparation.setText("No Fasting");
        }

        if (holder.ivTestImage != null) {
            ImageUtils.loadRoundedImage(holder.itemView.getContext(), test.getImageUrl(), holder.ivTestImage, 12);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLabTestClick(test);
            }
        });
    }

    @Override
    public int getItemCount() {
        return labTestList != null ? labTestList.size() : 0;
    }

    static class LabTestViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivTestImage;
        final TextView tvTestName;
        final TextView tvCategory;
        final TextView tvPrice;
        final TextView tvDescription;
        final TextView tvResultTime;
        final TextView tvPreparation;

        LabTestViewHolder(View itemView) {
            super(itemView);
            ivTestImage = itemView.findViewById(R.id.ivTestImage);
            tvTestName = itemView.findViewById(R.id.tvTestName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvResultTime = itemView.findViewById(R.id.tvResultTime);
            tvPreparation = itemView.findViewById(R.id.tvPreparation);
        }
    }
}
