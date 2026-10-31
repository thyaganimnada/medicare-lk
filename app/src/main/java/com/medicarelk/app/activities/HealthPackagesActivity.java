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
import com.medicarelk.app.adapters.HealthPackageAdapter;
import com.medicarelk.app.models.HealthPackage;
import com.medicarelk.app.repositories.HealthPackageRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Health Check-up Packages Activity displaying complete health screening packages.
 */
public class HealthPackagesActivity extends AppCompatActivity {

    private HealthPackageRepository packageRepository;
    private HealthPackageAdapter adapter;

    private List<HealthPackage> allPackages = new ArrayList<>();
    private List<HealthPackage> filteredList = new ArrayList<>();

    private EditText etSearchPackages;
    private RecyclerView rvPackages;
    private ProgressBar progressBarPackages;
    private View layoutEmptyState;
    private View layoutErrorState;

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

        setContentView(R.layout.activity_health_packages);

        View rootView = findViewById(R.id.rootHealthPackages);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        initViews();
        packageRepository = new HealthPackageRepository();

        setupRecyclerView();
        setupSearchAndListeners();

        loadPackages();
    }

    private void initViews() {
        etSearchPackages = findViewById(R.id.etSearchPackages);
        rvPackages = findViewById(R.id.rvPackages);
        progressBarPackages = findViewById(R.id.progressBarPackages);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        layoutErrorState = findViewById(R.id.layoutErrorState);
    }

    private void setupRecyclerView() {
        adapter = new HealthPackageAdapter(filteredList, healthPackage -> {
            Intent intent = new Intent(HealthPackagesActivity.this, HealthPackageDetailsActivity.class);
            intent.putExtra("packageId", healthPackage.getPackageId());
            intent.putExtra("name", healthPackage.getName());
            intent.putExtra("description", healthPackage.getDescription());
            intent.putExtra("price", healthPackage.getPrice());
            intent.putExtra("duration", healthPackage.getDuration());
            intent.putExtra("eligibility", healthPackage.getEligibility());
            intent.putExtra("locations", healthPackage.getLocationsText());
            intent.putExtra("includedTests", String.join("\n• ", healthPackage.getIncludedTests()));
            intent.putExtra("imageUrl", healthPackage.getImageUrl());
            startActivity(intent);
        });

        if (rvPackages != null) {
            rvPackages.setAdapter(adapter);
        }
    }

    private void loadPackages() {
        showLoading(true);
        showError(false);

        packageRepository.getActivePackages(new HealthPackageRepository.OnPackagesLoadedListener() {
            @Override
            public void onSuccess(List<HealthPackage> packages) {
                showLoading(false);
                allPackages = packages != null ? packages : new ArrayList<>();
                applyFilters();
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                showError(true);
            }
        });
    }

    private void setupSearchAndListeners() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        View btnRetry = findViewById(R.id.btnRetry);
        if (btnRetry != null) btnRetry.setOnClickListener(v -> loadPackages());

        if (etSearchPackages != null) {
            etSearchPackages.addTextChangedListener(new TextWatcher() {
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
        filteredList.clear();

        for (HealthPackage pkg : allPackages) {
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    pkg.getName().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                    pkg.getDescription().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                    pkg.getEligibility().toLowerCase().contains(currentSearchQuery.toLowerCase());

            if (matchesSearch) {
                filteredList.add(pkg);
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        if (filteredList.isEmpty()) {
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
            if (rvPackages != null) rvPackages.setVisibility(View.GONE);
        } else {
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
            if (rvPackages != null) rvPackages.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean isLoading) {
        if (progressBarPackages != null) {
            progressBarPackages.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void showError(boolean isError) {
        if (layoutErrorState != null) {
            layoutErrorState.setVisibility(isError ? View.VISIBLE : View.GONE);
        }
        if (rvPackages != null && isError) {
            rvPackages.setVisibility(View.GONE);
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(HealthPackagesActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}