package com.medicarelk.app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.medicarelk.app.R;
import com.medicarelk.app.adapters.DoctorAdapter;
import com.medicarelk.app.models.Doctor;
import com.medicarelk.app.repositories.DoctorRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Doctors Activity allowing authenticated patients to search, filter, and browse active doctors.
 * Supports searching by doctor name, specialization, hospital, city, and location.
 */
public class DoctorsActivity extends AppCompatActivity {

    private DoctorRepository doctorRepository;
    private DoctorAdapter adapter;

    private final List<Doctor> allDoctorsList = new ArrayList<>();
    private final List<Doctor> filteredDoctorsList = new ArrayList<>();

    private EditText etSearchDoctor;
    private RecyclerView rvAllDoctors;
    private ProgressBar progressBarDoctors;
    private View layoutEmptyState;
    private View layoutErrorState;
    private View layoutActiveFilter;
    private TextView tvActiveFilterLabel;

    private String filterHospitalName = null;
    private String filterCity = null;

    private String selectedSpecialty = "All Specializations";
    private String selectedLocation = "All Locations";
    private String selectedConsultationType = "All Modes";
    private String selectedFeeRange = "All Fee Ranges";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        filterHospitalName = getIntent().getStringExtra("hospitalName");
        filterCity = getIntent().getStringExtra("city");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_doctors);

        View rootView = findViewById(R.id.rootDoctors);
        View doctorsHeader = findViewById(R.id.doctorsHeader);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                if (doctorsHeader != null && systemBars.top > 0) {
                    int basePadding = (int) (64 * getResources().getDisplayMetrics().density);
                    int topPadding = Math.max(systemBars.top + (int) (16 * getResources().getDisplayMetrics().density), basePadding);
                    doctorsHeader.setPadding(
                            doctorsHeader.getPaddingLeft(),
                            topPadding,
                            doctorsHeader.getPaddingRight(),
                            doctorsHeader.getPaddingBottom()
                    );
                }
                v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        initViews();
        doctorRepository = new DoctorRepository();

        setupRecyclerView();
        setupListeners();
        setupBottomNavigation();

        loadActiveDoctors();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_doctors);
        }
    }

    private void initViews() {
        etSearchDoctor = findViewById(R.id.etSearchDoctor);
        rvAllDoctors = findViewById(R.id.rvAllDoctors);
        progressBarDoctors = findViewById(R.id.progressBarDoctors);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        layoutErrorState = findViewById(R.id.layoutErrorState);
        layoutActiveFilter = findViewById(R.id.layoutActiveFilter);
        tvActiveFilterLabel = findViewById(R.id.tvActiveFilterLabel);
    }

    private void setupRecyclerView() {
        adapter = new DoctorAdapter(filteredDoctorsList, doctor -> {
            Intent intent = new Intent(DoctorsActivity.this, DoctorDetailsActivity.class);
            intent.putExtra("doctorId", doctor.getDoctorId());
            intent.putExtra("doctorName", doctor.getFullName());
            intent.putExtra("doctorSpecialty", doctor.getSpecialization());
            intent.putExtra("hospitalName", doctor.getHospitalName());
            intent.putExtra("location", doctor.getLocation());
            intent.putExtra("consultationFee", doctor.getConsultationFee());
            intent.putExtra("qualification", doctor.getQualification());
            intent.putExtra("experienceYears", doctor.getYearsOfExperience());
            intent.putExtra("bio", doctor.getBio());
            intent.putExtra("address", doctor.getAddress());
            intent.putExtra("languages", doctor.getLanguages());
            intent.putExtra("profileImageUrl", doctor.getProfileImageUrl());
            startActivity(intent);
        });

        if (rvAllDoctors != null) {
            rvAllDoctors.setAdapter(adapter);
        }
    }

    private void loadActiveDoctors() {
        showLoading(true);
        if (layoutErrorState != null) layoutErrorState.setVisibility(View.GONE);

        doctorRepository.getActiveDoctors(new DoctorRepository.OnDoctorsLoadedListener() {
            @Override
            public void onSuccess(List<Doctor> doctors) {
                showLoading(false);
                allDoctorsList.clear();
                if (doctors != null) {
                    allDoctorsList.addAll(doctors);
                }
                applySearchAndFilter();
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                if (layoutErrorState != null) layoutErrorState.setVisibility(View.VISIBLE);
                if (rvAllDoctors != null) rvAllDoctors.setVisibility(View.GONE);
            }
        });
    }

    private void setupListeners() {
        View btnFilter = findViewById(R.id.btnFilter);
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> showFilterBottomSheet());
        }

        View btnClearSearch = findViewById(R.id.btnClearSearch);
        if (btnClearSearch != null) {
            btnClearSearch.setOnClickListener(v -> clearAllFilters());
        }

        View tvClearFilter = findViewById(R.id.tvClearFilter);
        if (tvClearFilter != null) {
            tvClearFilter.setOnClickListener(v -> clearAllFilters());
        }

        View btnRetryDoctors = findViewById(R.id.btnRetryDoctors);
        if (btnRetryDoctors != null) {
            btnRetryDoctors.setOnClickListener(v -> loadActiveDoctors());
        }

        if (etSearchDoctor != null) {
            etSearchDoctor.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    applySearchAndFilter();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void showFilterBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.doctor_filter_bottom_sheet, null);
        dialog.setContentView(dialogView);

        Spinner spSpecialization = dialogView.findViewById(R.id.spSpecialization);
        Spinner spLocation = dialogView.findViewById(R.id.spLocation);
        Spinner spConsultationType = dialogView.findViewById(R.id.spConsultationType);
        Spinner spFeeRange = dialogView.findViewById(R.id.spFeeRange);
        View btnApplyFilters = dialogView.findViewById(R.id.btnApplyFilters);
        View tvClearAll = dialogView.findViewById(R.id.tvClearAll);

        // 1. Dynamic Specializations List
        Set<String> specsSet = new TreeSet<>();
        specsSet.add("Cardiology");
        specsSet.add("Dermatology");
        specsSet.add("ENT");
        specsSet.add("General Practitioner");
        specsSet.add("Neurology");
        specsSet.add("Orthopedics");
        specsSet.add("Pediatrics");
        specsSet.add("Psychiatry");
        for (Doctor doc : allDoctorsList) {
            if (doc.getSpecialization() != null && !doc.getSpecialization().trim().isEmpty()) {
                specsSet.add(doc.getSpecialization().trim());
            }
        }
        List<String> specList = new ArrayList<>();
        specList.add("All Specializations");
        specList.addAll(specsSet);

        ArrayAdapter<String> specAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_selected, specList);
        specAdapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        if (spSpecialization != null) {
            spSpecialization.setAdapter(specAdapter);
            int idx = specList.indexOf(selectedSpecialty);
            if (idx >= 0) spSpecialization.setSelection(idx);
        }

        // 2. Dynamic Locations List
        Set<String> locationsSet = new TreeSet<>();
        locationsSet.add("Colombo");
        locationsSet.add("Kandy");
        locationsSet.add("Galle");
        locationsSet.add("Kurunegala");
        locationsSet.add("Jaffna");
        locationsSet.add("Negombo");
        locationsSet.add("Gampaha");
        locationsSet.add("Matara");
        locationsSet.add("Badulla");
        for (Doctor doc : allDoctorsList) {
            if (doc.getLocation() != null && !doc.getLocation().trim().isEmpty()) {
                locationsSet.add(doc.getLocation().trim());
            }
        }
        List<String> locationList = new ArrayList<>();
        locationList.add("All Locations");
        locationList.addAll(locationsSet);

        ArrayAdapter<String> locAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_selected, locationList);
        locAdapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        if (spLocation != null) {
            spLocation.setAdapter(locAdapter);
            int idx = locationList.indexOf(selectedLocation);
            if (idx >= 0) spLocation.setSelection(idx);
        }

        // 3. Consultation Modes List
        List<String> typeList = new ArrayList<>();
        typeList.add("All Modes");
        typeList.add("In-person Visit");
        typeList.add("Online Video");
        typeList.add("In-person & Online");

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_selected, typeList);
        typeAdapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        if (spConsultationType != null) {
            spConsultationType.setAdapter(typeAdapter);
            int idx = typeList.indexOf(selectedConsultationType);
            if (idx >= 0) spConsultationType.setSelection(idx);
        }

        // 4. Fee Ranges List
        List<String> feeList = new ArrayList<>();
        feeList.add("All Fee Ranges");
        feeList.add("Under LKR 2,500");
        feeList.add("LKR 2,500 - 5,000");
        feeList.add("Above LKR 5,000");

        ArrayAdapter<String> feeAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_selected, feeList);
        feeAdapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        if (spFeeRange != null) {
            spFeeRange.setAdapter(feeAdapter);
            int idx = feeList.indexOf(selectedFeeRange);
            if (idx >= 0) spFeeRange.setSelection(idx);
        }

        if (tvClearAll != null) {
            tvClearAll.setOnClickListener(v -> {
                clearAllFilters();
                dialog.dismiss();
            });
        }

        if (btnApplyFilters != null) {
            btnApplyFilters.setOnClickListener(v -> {
                if (spSpecialization != null && spSpecialization.getSelectedItem() != null) {
                    selectedSpecialty = spSpecialization.getSelectedItem().toString();
                }
                if (spLocation != null && spLocation.getSelectedItem() != null) {
                    selectedLocation = spLocation.getSelectedItem().toString();
                }
                if (spConsultationType != null && spConsultationType.getSelectedItem() != null) {
                    selectedConsultationType = spConsultationType.getSelectedItem().toString();
                }
                if (spFeeRange != null && spFeeRange.getSelectedItem() != null) {
                    selectedFeeRange = spFeeRange.getSelectedItem().toString();
                }

                applySearchAndFilter();
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void applySearchAndFilter() {
        String searchQuery = etSearchDoctor != null && etSearchDoctor.getText() != null ?
                etSearchDoctor.getText().toString().trim().toLowerCase() : "";

        filteredDoctorsList.clear();

        for (Doctor doctor : allDoctorsList) {
            if (!doctor.isActive()) {
                continue;
            }

            boolean matchesSearch = searchQuery.isEmpty() ||
                    doctor.getFullName().toLowerCase().contains(searchQuery) ||
                    doctor.getSpecialization().toLowerCase().contains(searchQuery) ||
                    doctor.getHospitalName().toLowerCase().contains(searchQuery) ||
                    doctor.getLocation().toLowerCase().contains(searchQuery) ||
                    doctor.getAddress().toLowerCase().contains(searchQuery);

            boolean matchesSpecialty = selectedSpecialty == null ||
                    selectedSpecialty.startsWith("All") ||
                    doctor.getSpecialization().equalsIgnoreCase(selectedSpecialty);

            boolean matchesLocation = selectedLocation == null ||
                    selectedLocation.startsWith("All") ||
                    doctor.getLocation().equalsIgnoreCase(selectedLocation);

            boolean matchesType = true;
            if (selectedConsultationType != null && !selectedConsultationType.startsWith("All")) {
                if (selectedConsultationType.contains("In-person Visit")) {
                    matchesType = doctor.isInPersonConsultationAvailable();
                } else if (selectedConsultationType.contains("Online Video")) {
                    matchesType = doctor.isOnlineConsultationAvailable();
                } else if (selectedConsultationType.contains("In-person & Online")) {
                    matchesType = doctor.isInPersonConsultationAvailable() && doctor.isOnlineConsultationAvailable();
                }
            }

            boolean matchesFee = true;
            double fee = doctor.getConsultationFee();
            if (selectedFeeRange != null && !selectedFeeRange.startsWith("All")) {
                if (selectedFeeRange.contains("Under") || (selectedFeeRange.contains("2,500") && !selectedFeeRange.contains("-"))) {
                    matchesFee = fee < 2500;
                } else if (selectedFeeRange.contains("2,500 - 5,000") || selectedFeeRange.contains("2500-5000")) {
                    matchesFee = fee >= 2500 && fee <= 5000;
                } else if (selectedFeeRange.contains("Above")) {
                    matchesFee = fee > 5000;
                }
            }

            boolean matchesHospital = true;
            if (filterHospitalName != null && !filterHospitalName.trim().isEmpty()) {
                String searchHosp = filterHospitalName.trim().toLowerCase();
                String docHosp = doctor.getHospitalName().trim().toLowerCase();
                String docLoc = doctor.getLocation().trim().toLowerCase();

                matchesHospital = docHosp.contains(searchHosp) || searchHosp.contains(docHosp);
                if (!matchesHospital && filterCity != null && !filterCity.trim().isEmpty()) {
                    matchesHospital = docLoc.equalsIgnoreCase(filterCity.trim());
                }
            }

            if (matchesSearch && matchesSpecialty && matchesLocation && matchesType && matchesFee && matchesHospital) {
                filteredDoctorsList.add(doctor);
            }
        }

        boolean hasFilterApplied = (filterHospitalName != null && !filterHospitalName.trim().isEmpty()) ||
                (selectedSpecialty != null && !selectedSpecialty.startsWith("All")) ||
                (selectedLocation != null && !selectedLocation.startsWith("All")) ||
                (selectedConsultationType != null && !selectedConsultationType.startsWith("All")) ||
                (selectedFeeRange != null && !selectedFeeRange.startsWith("All"));

        if (layoutActiveFilter != null && tvActiveFilterLabel != null) {
            if (hasFilterApplied) {
                StringBuilder sb = new StringBuilder("Filter: ");
                if (filterHospitalName != null && !filterHospitalName.trim().isEmpty()) {
                    sb.append("Doctors at ").append(filterHospitalName);
                }
                if (selectedSpecialty != null && !selectedSpecialty.startsWith("All")) {
                    if (sb.length() > 8) sb.append(" | ");
                    sb.append(selectedSpecialty);
                }
                if (selectedLocation != null && !selectedLocation.startsWith("All")) {
                    if (sb.length() > 8) sb.append(" | ");
                    sb.append(selectedLocation);
                }
                if (selectedConsultationType != null && !selectedConsultationType.startsWith("All")) {
                    if (sb.length() > 8) sb.append(" | ");
                    sb.append(selectedConsultationType);
                }
                if (selectedFeeRange != null && !selectedFeeRange.startsWith("All")) {
                    if (sb.length() > 8) sb.append(" | ");
                    sb.append(selectedFeeRange);
                }
                tvActiveFilterLabel.setText(sb.toString());
                layoutActiveFilter.setVisibility(View.VISIBLE);
            } else {
                layoutActiveFilter.setVisibility(View.GONE);
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        if (filteredDoctorsList.isEmpty()) {
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
            if (rvAllDoctors != null) rvAllDoctors.setVisibility(View.GONE);
        } else {
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
            if (rvAllDoctors != null) rvAllDoctors.setVisibility(View.VISIBLE);
        }
    }

    private void clearAllFilters() {
        filterHospitalName = null;
        filterCity = null;
        selectedSpecialty = "All Specializations";
        selectedLocation = "All Locations";
        selectedConsultationType = "All Modes";
        selectedFeeRange = "All Fee Ranges";

        if (etSearchDoctor != null) etSearchDoctor.setText("");
        applySearchAndFilter();
    }

    private void showLoading(boolean isLoading) {
        if (progressBarDoctors != null) {
            progressBarDoctors.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void navigateToTab(Class<?> targetClass) {
        Intent intent = new Intent(this, targetClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_doctors);

            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_doctors) {
                    return true;
                } else if (itemId == R.id.nav_home) {
                    navigateToTab(HomeActivity.class);
                    return true;
                } else if (itemId == R.id.nav_appointments) {
                    navigateToTab(AppointmentsActivity.class);
                    return true;
                } else if (itemId == R.id.nav_health) {
                    navigateToTab(HealthActivity.class);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    navigateToTab(ProfileActivity.class);
                    return true;
                }
                return false;
            });
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(DoctorsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}