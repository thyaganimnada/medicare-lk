package com.medicarelk.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.medicarelk.app.R;
import com.medicarelk.app.adapters.FAQAdapter;
import com.medicarelk.app.models.FAQ;
import com.medicarelk.app.repositories.FAQRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Frequently Asked Questions (FAQ) Activity.
 */
public class FaqActivity extends AppCompatActivity {

    private FAQRepository faqRepository;
    private FAQAdapter adapter;

    private List<FAQ> allFaqs = new ArrayList<>();
    private final List<FAQ> filteredFaqs = new ArrayList<>();

    private EditText etSearchFaq;
    private RecyclerView rvFaqs;
    private ProgressBar progressBarFaq;
    private View layoutEmptyState;
    private ChipGroup chipGroupCategory;

    private String currentSearchQuery = "";
    private String selectedCategory = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Security check
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_faq);

        View rootView = findViewById(R.id.rootFaq);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        initViews();
        faqRepository = new FAQRepository();

        setupRecyclerView();
        setupSearchAndListeners();

        loadFaqs();
    }

    private void initViews() {
        etSearchFaq = findViewById(R.id.etSearchFaq);
        rvFaqs = findViewById(R.id.rvFaqs);
        progressBarFaq = findViewById(R.id.progressBarFaq);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        chipGroupCategory = findViewById(R.id.chipGroupCategory);
    }

    private void setupRecyclerView() {
        adapter = new FAQAdapter(filteredFaqs, faq -> {
            Intent intent = new Intent(FaqActivity.this, FAQDetailsActivity.class);
            intent.putExtra("faqId", faq.getFaqId());
            startActivity(intent);
        });

        if (rvFaqs != null) {
            rvFaqs.setAdapter(adapter);
        }
    }

    private void loadFaqs() {
        showLoading(true);

        faqRepository.getActiveFAQs(new FAQRepository.OnFAQsLoadedListener() {
            @Override
            public void onSuccess(List<FAQ> faqs) {
                showLoading(false);
                allFaqs = faqs != null ? faqs : new ArrayList<>();
                applyFilters();
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupSearchAndListeners() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        View btnContactSupport = findViewById(R.id.btnContactSupport);
        if (btnContactSupport != null) {
            btnContactSupport.setOnClickListener(v ->
                    startActivity(new Intent(FaqActivity.this, CustomerSupportActivity.class)));
        }

        if (etSearchFaq != null) {
            etSearchFaq.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s.toString().trim();
                    applyFilters();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (chipGroupCategory != null) {
            chipGroupCategory.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.chipCatAppt) {
                    selectedCategory = "Appointments";
                } else if (checkedId == R.id.chipCatDocs) {
                    selectedCategory = "Doctors";
                } else if (checkedId == R.id.chipCatLab) {
                    selectedCategory = "Lab Tests";
                } else if (checkedId == R.id.chipCatSupport) {
                    selectedCategory = "Technical Support";
                } else {
                    selectedCategory = "ALL";
                }
                applyFilters();
            });
        }
    }

    private void applyFilters() {
        filteredFaqs.clear();

        for (FAQ faq : allFaqs) {
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    faq.getQuestion().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                    faq.getAnswer().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                    faq.getKeywords().toLowerCase().contains(currentSearchQuery.toLowerCase());

            boolean matchesCategory = selectedCategory.equalsIgnoreCase("ALL") ||
                    faq.getCategory().equalsIgnoreCase(selectedCategory);

            if (matchesSearch && matchesCategory) {
                filteredFaqs.add(faq);
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        if (filteredFaqs.isEmpty()) {
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
            if (rvFaqs != null) rvFaqs.setVisibility(View.GONE);
        } else {
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
            if (rvFaqs != null) rvFaqs.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean isLoading) {
        if (progressBarFaq != null) {
            progressBarFaq.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(FaqActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}