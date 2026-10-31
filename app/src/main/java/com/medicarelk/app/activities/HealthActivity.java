package com.medicarelk.app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.medicarelk.app.R;

/**
 * Healthcare Services Main Dashboard Activity.
 * Navigates to Doctors, Lab Tests, Health Check-up Packages, Hospitals & Clinics, Health Tips, Records, and Health Promotions modules.
 */
public class HealthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_health);

        View rootView = findViewById(R.id.rootHealth);
        View toolbarHealth = findViewById(R.id.toolbarHealth);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                if (toolbarHealth != null && systemBars.top > 0) {
                    int basePadding = (int) (72 * getResources().getDisplayMetrics().density);
                    int topPadding = Math.max(systemBars.top + (int) (16 * getResources().getDisplayMetrics().density), basePadding);
                    toolbarHealth.setPadding(
                            toolbarHealth.getPaddingLeft(),
                            topPadding,
                            toolbarHealth.getPaddingRight(),
                            toolbarHealth.getPaddingBottom()
                    );
                }
                v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        setupCategoryCards();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_health);
        }
    }

    private void setupCategoryCards() {
        bindClick(R.id.btnBannerDoctors, DoctorsActivity.class);
        bindClick(R.id.cardHospitals, HospitalsActivity.class);
        bindClick(R.id.cardLabTests, LabTestsActivity.class);
        bindClick(R.id.cardHealthPackages, HealthPackagesActivity.class);
        bindClick(R.id.cardRecords, MedicalHistoryActivity.class);
        bindClick(R.id.cardHealthTips, HealthTipsActivity.class);
        bindClick(R.id.cardPromotions, PromotionsActivity.class);

        View btnEmergencyCall = findViewById(R.id.btnEmergencyCall);
        if (btnEmergencyCall != null) {
            btnEmergencyCall.setOnClickListener(v -> {
                try {
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                    dialIntent.setData(Uri.parse("tel:1990"));
                    startActivity(dialIntent);
                } catch (Exception e) {
                    Toast.makeText(HealthActivity.this, "Calling 1990 Suwa Seriya Emergency...", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void bindClick(int viewId, Class<?> targetClass) {
        View view = findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(v -> startActivity(new Intent(HealthActivity.this, targetClass)));
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
            bottomNavigation.setSelectedItemId(R.id.nav_health);
            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_health) {
                    return true;
                } else if (itemId == R.id.nav_home) {
                    navigateToTab(HomeActivity.class);
                    return true;
                } else if (itemId == R.id.nav_doctors) {
                    navigateToTab(DoctorsActivity.class);
                    return true;
                } else if (itemId == R.id.nav_appointments) {
                    navigateToTab(AppointmentsActivity.class);
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
        Intent intent = new Intent(HealthActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
