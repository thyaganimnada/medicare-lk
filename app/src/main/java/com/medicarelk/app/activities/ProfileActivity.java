package com.medicarelk.app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.medicarelk.app.R;
import com.medicarelk.app.models.Patient;
import com.medicarelk.app.repositories.ProfileRepository;
import com.medicarelk.app.utils.ImageUtils;

/**
 * Profile Activity displaying patient information header, gender, age, weight, blood group,
 * and navigation rows matching prototype user profile layout.
 */
public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ProfileRepository profileRepository;

    private ImageView imgAvatar;
    private View btnEditAvatarOverlay;
    private TextView tvProfileName;
    private TextView tvProfilePhone;
    private TextView tvStatGender;
    private TextView tvStatAge;
    private TextView tvStatWeight;
    private TextView tvStatBlood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_profile);

        View rootView = findViewById(R.id.rootProfile);
        View profileHeaderBar = findViewById(R.id.profileHeaderBar);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                if (profileHeaderBar != null) {
                    int basePadding = (int) (72 * getResources().getDisplayMetrics().density);
                    int topPadding = Math.max(systemBars.top + (int) (16 * getResources().getDisplayMetrics().density), basePadding);
                    profileHeaderBar.setPadding(
                            profileHeaderBar.getPaddingLeft(),
                            topPadding,
                            profileHeaderBar.getPaddingRight(),
                            profileHeaderBar.getPaddingBottom()
                    );
                }
                v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        initViews();
        profileRepository = new ProfileRepository();

        setupListeners();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loadProfile(currentUser.getUid());
        }
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_profile);
        }
    }

    private void initViews() {
        imgAvatar = findViewById(R.id.imgAvatar);
        btnEditAvatarOverlay = findViewById(R.id.btnEditAvatarOverlay);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvStatGender = findViewById(R.id.tvStatGender);
        tvStatAge = findViewById(R.id.tvStatAge);
        tvStatWeight = findViewById(R.id.tvStatWeight);
        tvStatBlood = findViewById(R.id.tvStatBlood);
    }

    private void loadProfile(String uid) {
        profileRepository.getProfile(uid, new ProfileRepository.OnProfileLoadedListener() {
            @Override
            public void onSuccess(Patient patient) {
                if (tvProfileName != null) tvProfileName.setText(patient.getFullName());

                String phoneOrEmail = patient.getPhone();
                if (phoneOrEmail == null || phoneOrEmail.trim().isEmpty()) {
                    phoneOrEmail = patient.getEmail();
                }
                if (tvProfilePhone != null) tvProfilePhone.setText(phoneOrEmail);

                if (tvStatGender != null) tvStatGender.setText(patient.getGender());
                if (tvStatAge != null) tvStatAge.setText(patient.getFormattedAge());
                if (tvStatWeight != null) tvStatWeight.setText(patient.getWeight());
                if (tvStatBlood != null) tvStatBlood.setText(patient.getBloodGroup());

                if (imgAvatar != null) {
                    ImageUtils.loadProfileImage(ProfileActivity.this, patient.getProfileImageUrl(), imgAvatar);
                }
            }

            @Override
            public void onError(String errorMessage) {}
        });
    }

    private void setupListeners() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        View btnNotification = findViewById(R.id.btnNotification);
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v ->
                    startActivity(new Intent(ProfileActivity.this, NotificationsActivity.class)));
        }

        View.OnClickListener openEditProfile = v ->
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));

        if (imgAvatar != null) imgAvatar.setOnClickListener(openEditProfile);
        if (btnEditAvatarOverlay != null) btnEditAvatarOverlay.setOnClickListener(openEditProfile);

        View btnEditProfile = findViewById(R.id.btnEditProfile);
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(openEditProfile);
        }

        View cardMedicalHistory = findViewById(R.id.cardMedicalHistory);
        if (cardMedicalHistory != null) {
            cardMedicalHistory.setOnClickListener(v ->
                    startActivity(new Intent(ProfileActivity.this, MedicalHistoryActivity.class)));
        }

        View cardAppointments = findViewById(R.id.cardAppointments);
        if (cardAppointments != null) {
            cardAppointments.setOnClickListener(v ->
                    startActivity(new Intent(ProfileActivity.this, AppointmentsActivity.class)));
        }

        View cardHelpSupport = findViewById(R.id.cardHelpSupport);
        if (cardHelpSupport != null) {
            cardHelpSupport.setOnClickListener(v ->
                    startActivity(new Intent(ProfileActivity.this, HelpSupportActivity.class)));
        }

        View cardSettings = findViewById(R.id.cardSettings);
        if (cardSettings != null) {
            cardSettings.setOnClickListener(v ->
                    startActivity(new Intent(ProfileActivity.this, SettingsActivity.class)));
        }

        View cardAbout = findViewById(R.id.cardAbout);
        if (cardAbout != null) {
            cardAbout.setOnClickListener(v ->
                    startActivity(new Intent(ProfileActivity.this, AboutActivity.class)));
        }

        View optLogout = findViewById(R.id.optLogout);
        if (optLogout != null) {
            optLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
        }
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out of MediCare Pro?")
                .setPositiveButton("Sign Out", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void performLogout() {
        mAuth.signOut();
        Toast.makeText(ProfileActivity.this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
        redirectToLogin();
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
            bottomNavigation.setSelectedItemId(R.id.nav_profile);

            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_profile) {
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
                } else if (itemId == R.id.nav_health) {
                    navigateToTab(HealthActivity.class);
                    return true;
                }
                return false;
            });
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}