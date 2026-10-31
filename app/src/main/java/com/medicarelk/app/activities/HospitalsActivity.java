package com.medicarelk.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.medicarelk.app.R;
import com.medicarelk.app.adapters.HospitalAdapter;
import com.medicarelk.app.models.Hospital;
import com.medicarelk.app.repositories.HospitalRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Hospitals & Clinics Activity displaying partnered healthcare locations.
 */
public class HospitalsActivity extends AppCompatActivity {

    private HospitalRepository hospitalRepository;
    private HospitalAdapter adapter;

    private List<Hospital> allHospitals = new ArrayList<>();
    private List<Hospital> filteredList = new ArrayList<>();

    private EditText etSearchHospitals;
    private RecyclerView rvHospitals;
    private ProgressBar progressBarHospitals;
    private View layoutEmptyState;
    private View layoutErrorState;

    private String currentSearchQuery = "";
    private String selectedCity = "ALL";
    private boolean emergencyOnlyFilter = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Security check
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_hospitals);

        View rootView = findViewById(R.id.rootHospitals);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        initViews();
        hospitalRepository = new HospitalRepository();

        setupRecyclerView();
        setupSearchAndListeners();

        loadHospitals();
    }

    private void initViews() {
        etSearchHospitals = findViewById(R.id.etSearchHospitals);
        rvHospitals = findViewById(R.id.rvHospitals);
        progressBarHospitals = findViewById(R.id.progressBarHospitals);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        layoutErrorState = findViewById(R.id.layoutErrorState);
    }

    private void setupRecyclerView() {
        adapter = new HospitalAdapter(filteredList, hospital -> {
            Intent intent = new Intent(HospitalsActivity.this, HospitalDetailsActivity.class);
            intent.putExtra("hospitalId", hospital.getHospitalId());
            intent.putExtra("name", hospital.getName());
            intent.putExtra("address", hospital.getAddress());
            intent.putExtra("city", hospital.getCity());
            intent.putExtra("location", hospital.getLocation());
            intent.putExtra("contactNumber", hospital.getContactNumber());
            intent.putExtra("openingHours", hospital.getOpeningHours());
            intent.putExtra("emergencyAvailable", hospital.isEmergencyAvailable());
            intent.putExtra("services", hospital.getServicesText());
            startActivity(intent);
        });

        if (rvHospitals != null) {
            rvHospitals.setAdapter(adapter);
        }
    }

    private void loadHospitals() {
        showLoading(true);
        showError(false);

        hospitalRepository.getActiveHospitals(new HospitalRepository.OnHospitalsLoadedListener() {
            @Override
            public void onSuccess(List<Hospital> hospitals) {
                showLoading(false);
                allHospitals = hospitals != null ? hospitals : new ArrayList<>();
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

        View btnFilter = findViewById(R.id.btnFilterHospitals);
        if (btnFilter != null) btnFilter.setOnClickListener(v -> showFilterBottomSheet());

        View btnResetSearch = findViewById(R.id.btnResetSearch);
        if (btnResetSearch != null) btnResetSearch.setOnClickListener(v -> resetFilters());

        View btnRetry = findViewById(R.id.btnRetry);
        if (btnRetry != null) btnRetry.setOnClickListener(v -> loadHospitals());

        if (etSearchHospitals != null) {
            etSearchHospitals.addTextChangedListener(new TextWatcher() {
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

        for (Hospital h : allHospitals) {
            // 1. Search Query Filter (name, city, location, address)
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    h.getName().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                    h.getCity().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                    h.getLocation().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                    h.getAddress().toLowerCase().contains(currentSearchQuery.toLowerCase());

            // 2. City Filter
            boolean matchesCity = selectedCity.equalsIgnoreCase("ALL") ||
                    h.getCity().equalsIgnoreCase(selectedCity);

            // 3. Emergency Filter
            boolean matchesEmergency = !emergencyOnlyFilter || h.isEmergencyAvailable();

            if (matchesSearch && matchesCity && matchesEmergency) {
                filteredList.add(h);
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        if (filteredList.isEmpty()) {
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
            if (rvHospitals != null) rvHospitals.setVisibility(View.GONE);
        } else {
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
            if (rvHospitals != null) rvHospitals.setVisibility(View.VISIBLE);
        }
    }

    private void showFilterBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.hospital_filter_bottom_sheet, null);
        bottomSheetDialog.setContentView(dialogView);

        ChipGroup chipGroupCity = dialogView.findViewById(R.id.chipGroupCity);
        CheckBox cbEmergencyOnly = dialogView.findViewById(R.id.cbEmergencyOnly);
        View btnReset = dialogView.findViewById(R.id.btnResetFilter);
        View btnApply = dialogView.findViewById(R.id.btnApplyFilter);

        if (btnReset != null) {
            btnReset.setOnClickListener(v -> {
                selectedCity = "ALL";
                emergencyOnlyFilter = false;
                applyFilters();
                bottomSheetDialog.dismiss();
            });
        }

        if (btnApply != null) {
            btnApply.setOnClickListener(v -> {
                if (chipGroupCity != null) {
                    int cityId = chipGroupCity.getCheckedChipId();
                    if (cityId == R.id.chipCityColombo) {
                        selectedCity = "Colombo";
                    } else if (cityId == R.id.chipCityKandy) {
                        selectedCity = "Kandy";
                    } else if (cityId == R.id.chipCityGalle) {
                        selectedCity = "Galle";
                    } else if (cityId == R.id.chipCityKurunegala) {
                        selectedCity = "Kurunegala";
                    } else {
                        selectedCity = "ALL";
                    }
                }

                if (cbEmergencyOnly != null) {
                    emergencyOnlyFilter = cbEmergencyOnly.isChecked();
                }

                applyFilters();
                bottomSheetDialog.dismiss();
            });
        }

        bottomSheetDialog.show();
    }

    private void resetFilters() {
        currentSearchQuery = "";
        selectedCity = "ALL";
        emergencyOnlyFilter = false;
        if (etSearchHospitals != null) etSearchHospitals.setText("");
        applyFilters();
    }

    private void showLoading(boolean isLoading) {
        if (progressBarHospitals != null) {
            progressBarHospitals.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void showError(boolean isError) {
        if (layoutErrorState != null) {
            layoutErrorState.setVisibility(isError ? View.VISIBLE : View.GONE);
        }
        if (rvHospitals != null && isError) {
            rvHospitals.setVisibility(View.GONE);
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(HospitalsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}