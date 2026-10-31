package com.medicarelk.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.medicarelk.app.adapters.LabTestAdapter;
import com.medicarelk.app.models.LabTest;
import com.medicarelk.app.repositories.LabTestRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Lab Tests Activity allowing patients to browse, search, and filter diagnostic lab tests.
 */
public class LabTestsActivity extends AppCompatActivity {

    private LabTestRepository labTestRepository;
    private LabTestAdapter adapter;

    private List<LabTest> allLabTests = new ArrayList<>();
    private List<LabTest> filteredList = new ArrayList<>();

    private EditText etSearchLabTests;
    private RecyclerView rvLabTests;
    private ProgressBar progressBarLabTests;
    private View layoutEmptyState;
    private View layoutErrorState;
    private ChipGroup chipGroupCategory;
    private View layoutActiveHospitalFilter;
    private TextView tvActiveHospitalFilterLabel;

    private String filterHospitalName = null;
    private String filterCity = null;

    private String currentSearchQuery = "";
    private String selectedCategory = "ALL";
    private String selectedPriceRange = "ALL";
    private String selectedLocation = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        filterHospitalName = getIntent().getStringExtra("hospitalName");
        filterCity = getIntent().getStringExtra("city");

        // Security check
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_lab_tests);

        View rootView = findViewById(R.id.rootLabTests);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        initViews();
        labTestRepository = new LabTestRepository();

        setupRecyclerView();
        setupSearchAndListeners();

        loadLabTests();
    }

    private void initViews() {
        etSearchLabTests = findViewById(R.id.etSearchLabTests);
        rvLabTests = findViewById(R.id.rvLabTests);
        progressBarLabTests = findViewById(R.id.progressBarLabTests);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        layoutErrorState = findViewById(R.id.layoutErrorState);
        chipGroupCategory = findViewById(R.id.chipGroupCategory);
        layoutActiveHospitalFilter = findViewById(R.id.layoutActiveHospitalFilter);
        tvActiveHospitalFilterLabel = findViewById(R.id.tvActiveHospitalFilterLabel);
    }

    private void setupRecyclerView() {
        adapter = new LabTestAdapter(filteredList, labTest -> {
            Intent intent = new Intent(LabTestsActivity.this, LabTestDetailsActivity.class);
            intent.putExtra("labTestId", labTest.getLabTestId());
            intent.putExtra("name", labTest.getName());
            intent.putExtra("category", labTest.getCategory());
            intent.putExtra("description", labTest.getDescription());
            intent.putExtra("price", labTest.getPrice());
            intent.putExtra("resultTime", labTest.getResultTime());
            intent.putExtra("preparation", labTest.getPreparationInstructions());
            intent.putExtra("locations", labTest.getLocationsText());
            intent.putExtra("imageUrl", labTest.getImageUrl());
            startActivity(intent);
        });

        if (rvLabTests != null) {
            rvLabTests.setAdapter(adapter);
        }
    }

    private void loadLabTests() {
        showLoading(true);
        showError(false);

        labTestRepository.getActiveLabTests(new LabTestRepository.OnLabTestsLoadedListener() {
            @Override
            public void onSuccess(List<LabTest> labTests) {
                showLoading(false);
                allLabTests = labTests != null ? labTests : new ArrayList<>();
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

        View btnFilter = findViewById(R.id.btnFilterLabTests);
        if (btnFilter != null) btnFilter.setOnClickListener(v -> showFilterBottomSheet());

        View btnResetSearch = findViewById(R.id.btnResetSearch);
        if (btnResetSearch != null) btnResetSearch.setOnClickListener(v -> resetFilters());

        View btnRetry = findViewById(R.id.btnRetry);
        if (btnRetry != null) btnRetry.setOnClickListener(v -> loadLabTests());

        View tvClearHospitalFilter = findViewById(R.id.tvClearHospitalFilter);
        if (tvClearHospitalFilter != null) {
            tvClearHospitalFilter.setOnClickListener(v -> {
                filterHospitalName = null;
                filterCity = null;
                applyFilters();
            });
        }

        if (etSearchLabTests != null) {
            etSearchLabTests.addTextChangedListener(new TextWatcher() {
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
                if (checkedId == R.id.chipCatBlood) {
                    selectedCategory = "Blood Tests";
                } else if (checkedId == R.id.chipCatDiabetes) {
                    selectedCategory = "Diabetes";
                } else if (checkedId == R.id.chipCatLiver) {
                    selectedCategory = "Liver";
                } else if (checkedId == R.id.chipCatKidney) {
                    selectedCategory = "Kidney";
                } else if (checkedId == R.id.chipCatCholesterol) {
                    selectedCategory = "Cholesterol";
                } else {
                    selectedCategory = "ALL";
                }
                applyFilters();
            });
        }
    }

    private void applyFilters() {
        filteredList.clear();

        for (LabTest test : allLabTests) {
            // 1. Hospital Filter
            boolean matchesHospital = true;
            if (filterHospitalName != null && !filterHospitalName.trim().isEmpty()) {
                matchesHospital = false;
                String searchHosp = filterHospitalName.trim().toLowerCase();
                String searchCity = filterCity != null ? filterCity.trim().toLowerCase() : "";

                if (test.getAvailableLocations() != null) {
                    for (String loc : test.getAvailableLocations()) {
                        String locLower = loc.trim().toLowerCase();
                        if (locLower.contains(searchHosp) || searchHosp.contains(locLower) ||
                                (!searchCity.isEmpty() && locLower.contains(searchCity))) {
                            matchesHospital = true;
                            break;
                        }
                    }
                }
                if (!matchesHospital && test.getLocationsText() != null) {
                    String locTextLower = test.getLocationsText().toLowerCase();
                    if (locTextLower.contains(searchHosp) || (!searchCity.isEmpty() && locTextLower.contains(searchCity))) {
                        matchesHospital = true;
                    }
                }
            }

            // 2. Text Search Filter (name, category, description, locations)
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    test.getName().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                    test.getCategory().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                    test.getLocationsText().toLowerCase().contains(currentSearchQuery.toLowerCase());

            // 3. Category Filter
            boolean matchesCategory = selectedCategory.equalsIgnoreCase("ALL") ||
                    test.getCategory().equalsIgnoreCase(selectedCategory);

            // 4. Location Filter
            boolean matchesLocation = selectedLocation.equalsIgnoreCase("ALL") ||
                    test.getLocationsText().toLowerCase().contains(selectedLocation.toLowerCase());

            // 5. Price Range Filter
            boolean matchesPrice = true;
            double p = test.getPrice();
            if (selectedPriceRange.equalsIgnoreCase("UNDER_2500")) {
                matchesPrice = p < 2500.0;
            } else if (selectedPriceRange.equalsIgnoreCase("2500_TO_5000")) {
                matchesPrice = p >= 2500.0 && p <= 5000.0;
            } else if (selectedPriceRange.equalsIgnoreCase("5000_TO_10000")) {
                matchesPrice = p > 5000.0 && p <= 10000.0;
            } else if (selectedPriceRange.equalsIgnoreCase("ABOVE_10000")) {
                matchesPrice = p > 10000.0;
            }

            if (matchesHospital && matchesSearch && matchesCategory && matchesLocation && matchesPrice) {
                filteredList.add(test);
            }
        }

        if (layoutActiveHospitalFilter != null && tvActiveHospitalFilterLabel != null) {
            if (filterHospitalName != null && !filterHospitalName.trim().isEmpty()) {
                tvActiveHospitalFilterLabel.setText("Showing tests available at " + filterHospitalName);
                layoutActiveHospitalFilter.setVisibility(View.VISIBLE);
            } else {
                layoutActiveHospitalFilter.setVisibility(View.GONE);
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        if (filteredList.isEmpty()) {
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
            if (rvLabTests != null) rvLabTests.setVisibility(View.GONE);
        } else {
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
            if (rvLabTests != null) rvLabTests.setVisibility(View.VISIBLE);
        }
    }

    private void showFilterBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.lab_test_filter_bottom_sheet, null);
        bottomSheetDialog.setContentView(dialogView);

        ChipGroup chipGroupPrice = dialogView.findViewById(R.id.chipGroupPrice);
        ChipGroup chipGroupLocation = dialogView.findViewById(R.id.chipGroupLocation);
        View btnReset = dialogView.findViewById(R.id.btnResetFilter);
        View btnApply = dialogView.findViewById(R.id.btnApplyFilter);

        if (btnReset != null) {
            btnReset.setOnClickListener(v -> {
                selectedPriceRange = "ALL";
                selectedLocation = "ALL";
                applyFilters();
                bottomSheetDialog.dismiss();
            });
        }

        if (btnApply != null) {
            btnApply.setOnClickListener(v -> {
                if (chipGroupPrice != null) {
                    int priceId = chipGroupPrice.getCheckedChipId();
                    if (priceId == R.id.chipPriceUnder2500) {
                        selectedPriceRange = "UNDER_2500";
                    } else if (priceId == R.id.chipPrice2500to5000) {
                        selectedPriceRange = "2500_TO_5000";
                    } else if (priceId == R.id.chipPrice5000to10000) {
                        selectedPriceRange = "5000_TO_10000";
                    } else if (priceId == R.id.chipPriceAbove10000) {
                        selectedPriceRange = "ABOVE_10000";
                    } else {
                        selectedPriceRange = "ALL";
                    }
                }

                if (chipGroupLocation != null) {
                    int locId = chipGroupLocation.getCheckedChipId();
                    if (locId == R.id.chipLocColombo) {
                        selectedLocation = "Colombo";
                    } else if (locId == R.id.chipLocKandy) {
                        selectedLocation = "Kandy";
                    } else if (locId == R.id.chipLocGalle) {
                        selectedLocation = "Galle";
                    } else {
                        selectedLocation = "ALL";
                    }
                }

                applyFilters();
                bottomSheetDialog.dismiss();
            });
        }

        bottomSheetDialog.show();
    }

    private void resetFilters() {
        filterHospitalName = null;
        filterCity = null;
        currentSearchQuery = "";
        selectedCategory = "ALL";
        selectedPriceRange = "ALL";
        selectedLocation = "ALL";
        if (etSearchLabTests != null) etSearchLabTests.setText("");
        if (chipGroupCategory != null) chipGroupCategory.check(R.id.chipCatAll);
        applyFilters();
    }

    private void showLoading(boolean isLoading) {
        if (progressBarLabTests != null) {
            progressBarLabTests.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void showError(boolean isError) {
        if (layoutErrorState != null) {
            layoutErrorState.setVisibility(isError ? View.VISIBLE : View.GONE);
        }
        if (rvLabTests != null && isError) {
            rvLabTests.setVisibility(View.GONE);
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(LabTestsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
