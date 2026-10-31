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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.medicarelk.app.R;
import com.medicarelk.app.adapters.PromotionAdapter;
import com.medicarelk.app.models.Promotion;
import com.medicarelk.app.repositories.PromotionRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Healthcare Promotions & Seasonal Campaigns Activity.
 */
public class PromotionsActivity extends AppCompatActivity {

    private PromotionRepository promotionRepository;
    private PromotionAdapter adapter;

    private List<Promotion> allPromotions = new ArrayList<>();
    private final List<Promotion> filteredPromotions = new ArrayList<>();

    private EditText etSearchPromotions;
    private RecyclerView rvPromotions;
    private ProgressBar progressBarPromotions;
    private View layoutEmptyState;

    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Security check
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_promotions);

        View rootView = findViewById(R.id.rootPromotions);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        initViews();
        promotionRepository = new PromotionRepository();

        setupRecyclerView();
        setupSearchAndListeners();

        loadPromotions();
    }

    private void initViews() {
        etSearchPromotions = findViewById(R.id.etSearchPromotions);
        rvPromotions = findViewById(R.id.rvPromotions);
        progressBarPromotions = findViewById(R.id.progressBarPromotions);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
    }

    private void setupRecyclerView() {
        adapter = new PromotionAdapter(filteredPromotions, promo -> {
            Intent intent = new Intent(PromotionsActivity.this, PromotionDetailsActivity.class);
            intent.putExtra("promotionId", promo.getPromotionId());
            intent.putExtra("title", promo.getTitle());
            intent.putExtra("type", promo.getPromotionType());
            intent.putExtra("hospital", promo.getHospitalName());
            intent.putExtra("validity", promo.getEndDate());
            intent.putExtra("description", promo.getDescription());
            intent.putExtra("terms", promo.getTermsAndConditions());
            intent.putExtra("contactNumber", promo.getContactNumber());
            startActivity(intent);
        });

        if (rvPromotions != null) {
            rvPromotions.setAdapter(adapter);
        }
    }

    private void loadPromotions() {
        showLoading(true);

        promotionRepository.getActivePromotions(new PromotionRepository.OnPromotionsLoadedListener() {
            @Override
            public void onSuccess(List<Promotion> promotions) {
                showLoading(false);
                allPromotions = promotions != null ? promotions : new ArrayList<>();
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

        if (etSearchPromotions != null) {
            etSearchPromotions.addTextChangedListener(new TextWatcher() {
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
    }

    private void applyFilters() {
        filteredPromotions.clear();

        for (Promotion promo : allPromotions) {
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    promo.getTitle().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                    promo.getPromotionType().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                    promo.getHospitalName().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                    promo.getShortDescription().toLowerCase().contains(currentSearchQuery.toLowerCase());

            if (matchesSearch) {
                filteredPromotions.add(promo);
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        if (filteredPromotions.isEmpty()) {
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
            if (rvPromotions != null) rvPromotions.setVisibility(View.GONE);
        } else {
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
            if (rvPromotions != null) rvPromotions.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean isLoading) {
        if (progressBarPromotions != null) {
            progressBarPromotions.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(PromotionsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}