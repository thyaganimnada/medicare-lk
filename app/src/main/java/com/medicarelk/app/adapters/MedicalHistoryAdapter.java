package com.medicarelk.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.medicarelk.app.R;
import com.medicarelk.app.models.MedicalRecord;

import java.util.List;

public class MedicalHistoryAdapter extends RecyclerView.Adapter<MedicalHistoryAdapter.RecordViewHolder> {

    public interface OnRecordClickListener {
        void onRecordClick(MedicalRecord record);
    }

    private final List<MedicalRecord> recordList;
    private final OnRecordClickListener listener;

    public MedicalHistoryAdapter(List<MedicalRecord> recordList, OnRecordClickListener listener) {
        this.recordList = recordList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medical_record, parent, false);
        return new RecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        MedicalRecord record = recordList.get(position);

        holder.tvConditionName.setText(record.getConditionName());
        holder.tvDiagnosisDate.setText("Diagnosed: " + (record.getDiagnosisDate() != null && !record.getDiagnosisDate().isEmpty() ? record.getDiagnosisDate() : "N/A"));
        holder.tvDescription.setText(record.getDescription());

        String status = record.getStatus();
        holder.tvStatusBadge.setText(status);

        if (MedicalRecord.STATUS_ACTIVE.equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.brand_blue));
        } else if (MedicalRecord.STATUS_RESOLVED.equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.success_green));
        } else {
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecordClick(record);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recordList != null ? recordList.size() : 0;
    }

    static class RecordViewHolder extends RecyclerView.ViewHolder {
        final TextView tvConditionName;
        final TextView tvStatusBadge;
        final TextView tvDiagnosisDate;
        final TextView tvDescription;

        RecordViewHolder(View itemView) {
            super(itemView);
            tvConditionName = itemView.findViewById(R.id.tvConditionName);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvDiagnosisDate = itemView.findViewById(R.id.tvDiagnosisDate);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }
}