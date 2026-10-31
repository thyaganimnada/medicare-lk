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
import com.medicarelk.app.adapters.HealthTipsAdapter;
import com.medicarelk.app.models.HealthTip;
import com.medicarelk.app.repositories.HealthTipRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Health & Wellness Tips Activity allowing patients to search and browse educational tips.
 */
public class HealthTipsActivity extends AppCompatActivity {

    private HealthTipRepository tipRepository;
    private HealthTipsAdapter adapter;

    private List<HealthTip> allTips = new ArrayList<>();
    private final List<HealthTip> filteredTips = new ArrayList<>();

    private EditText etSearchHealthTips;
    private RecyclerView rvHealthTips;
    private ProgressBar progressBarHealthTips;
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

        setContentView(R.layout.activity_health_tips);

        View rootView = findViewById(R.id.rootHealthTips);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        initViews();
        tipRepository = new HealthTipRepository();

        setupRecyclerView();
        setupSearchAndListeners();

        loadHealthTips();
    }

    private void initViews() {
        etSearchHealthTips = findViewById(R.id.etSearchHealthTips);
        rvHealthTips = findViewById(R.id.rvHealthTips);
        progressBarHealthTips = findViewById(R.id.progressBarHealthTips);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
    }

    private void setupRecyclerView() {
        adapter = new HealthTipsAdapter(filteredTips, tip -> {
            Intent intent = new Intent(HealthTipsActivity.this, HealthTipDetailsActivity.class);
            intent.putExtra("tipId", tip.getTipId());
            intent.putExtra("title", tip.getTitle());
            intent.putExtra("category", tip.getCategory());
            intent.putExtra("author", tip.getAuthor());
            intent.putExtra("content", tip.getContent());
            startActivity(intent);
        });

        if (rvHealthTips != null) {
            rvHealthTips.setAdapter(adapter);
        }
    }

    private void loadHealthTips() {
        showLoading(true);

        tipRepository.getActiveHealthTips(new HealthTipRepository.OnHealthTipsLoadedListener() {
            @Override
            public void onSuccess(List<HealthTip> healthTips) {
                showLoading(false);
                allTips = healthTips != null ? healthTips : new ArrayList<>();
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

        if (etSearchHealthTips != null) {
            etSearchHealthTips.addTextChangedListener(new TextWatcher() {
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
        filteredTips.clear();

        for (HealthTip tip : allTips) {
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    tip.getTitle().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                    tip.getCategory().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                    tip.getSummary().toLowerCase().contains(currentSearchQuery.toLowerCase());

            if (matchesSearch) {
                filteredTips.add(tip);
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        if (filteredTips.isEmpty()) {
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
            if (rvHealthTips != null) rvHealthTips.setVisibility(View.GONE);
        } else {
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
            if (rvHealthTips != null) rvHealthTips.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean isLoading) {
        if (progressBarHealthTips != null) {
            progressBarHealthTips.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(HealthTipsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}