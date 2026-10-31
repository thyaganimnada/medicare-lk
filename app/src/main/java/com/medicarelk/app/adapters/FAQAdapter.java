package com.medicarelk.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.medicarelk.app.R;
import com.medicarelk.app.models.FAQ;

import java.util.List;

public class FAQAdapter extends RecyclerView.Adapter<FAQAdapter.FAQViewHolder> {

    public interface OnFAQClickListener {
        void onFAQClick(FAQ faq);
    }

    private final List<FAQ> faqList;
    private final OnFAQClickListener listener;

    public FAQAdapter(List<FAQ> faqList, OnFAQClickListener listener) {
        this.faqList = faqList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FAQViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_faq, parent, false);
        return new FAQViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FAQViewHolder holder, int position) {
        FAQ faq = faqList.get(position);

        holder.tvQuestion.setText(faq.getQuestion());
        holder.tvAnswer.setText(faq.getAnswer());
        holder.tvCategory.setText(faq.getCategory());

        // Accordion Expansion
        boolean isExpanded = faq.isExpanded();
        holder.tvAnswer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.imgExpand.setRotation(isExpanded ? 90f : 0f);

        holder.itemView.setOnClickListener(v -> {
            faq.setExpanded(!faq.isExpanded());
            notifyItemChanged(position);
            if (listener != null) {
                listener.onFAQClick(faq);
            }
        });
    }

    @Override
    public int getItemCount() {
        return faqList != null ? faqList.size() : 0;
    }

    static class FAQViewHolder extends RecyclerView.ViewHolder {
        final TextView tvCategory;
        final TextView tvQuestion;
        final TextView tvAnswer;
        final ImageView imgExpand;

        FAQViewHolder(View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            tvAnswer = itemView.findViewById(R.id.tvAnswer);
            imgExpand = itemView.findViewById(R.id.imgExpand);
        }
    }
}