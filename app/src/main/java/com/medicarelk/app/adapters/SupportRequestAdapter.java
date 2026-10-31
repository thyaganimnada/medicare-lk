package com.medicarelk.app.adapters;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.medicarelk.app.R;
import com.medicarelk.app.models.SupportRequest;

import java.util.List;

public class SupportRequestAdapter extends RecyclerView.Adapter<SupportRequestAdapter.SupportViewHolder> {

    public interface OnSupportRequestListener {
        void onRequestClick(SupportRequest request);
    }

    private final List<SupportRequest> requestList;
    private final OnSupportRequestListener listener;

    public SupportRequestAdapter(List<SupportRequest> requestList, OnSupportRequestListener listener) {
        this.requestList = requestList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SupportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_support_request, parent, false);
        return new SupportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SupportViewHolder holder, int position) {
        SupportRequest req = requestList.get(position);

        String refId = "#" + (req.getRequestId().length() > 8 ? req.getRequestId().substring(req.getRequestId().length() - 8) : req.getRequestId());
        holder.tvRefId.setText("Req " + refId);
        holder.tvSubject.setText(req.getSubject());
        holder.tvCategory.setText("Category: " + req.getCategory());

        CharSequence dateStr = DateFormat.format("yyyy-MM-dd", req.getCreatedAt());
        holder.tvCreatedDate.setText(dateStr);

        String status = req.getFormattedStatus();
        holder.tvStatusBadge.setText(status);

        if (SupportRequest.STATUS_OPEN.equalsIgnoreCase(req.getStatus())) {
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.brand_blue));
        } else if (SupportRequest.STATUS_IN_PROGRESS.equalsIgnoreCase(req.getStatus())) {
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary_teal_dark));
        } else if (SupportRequest.STATUS_RESOLVED.equalsIgnoreCase(req.getStatus())) {
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.success_green));
        } else {
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRequestClick(req);
            }
        });
    }

    @Override
    public int getItemCount() {
        return requestList != null ? requestList.size() : 0;
    }

    static class SupportViewHolder extends RecyclerView.ViewHolder {
        final TextView tvRefId;
        final TextView tvStatusBadge;
        final TextView tvSubject;
        final TextView tvCategory;
        final TextView tvCreatedDate;

        SupportViewHolder(View itemView) {
            super(itemView);
            tvRefId = itemView.findViewById(R.id.tvRefId);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvCreatedDate = itemView.findViewById(R.id.tvCreatedDate);
        }
    }
}