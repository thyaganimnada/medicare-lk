package com.medicarelk.app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.medicarelk.app.R;
import com.medicarelk.app.adapters.DoctorAdapter;
import com.medicarelk.app.models.Appointment;
import com.medicarelk.app.models.Doctor;
import com.medicarelk.app.models.Notification;
import com.medicarelk.app.models.Patient;
import com.medicarelk.app.repositories.AppointmentRepository;
import com.medicarelk.app.repositories.DoctorRepository;
import com.medicarelk.app.repositories.NotificationRepository;
import com.medicarelk.app.repositories.PatientRepository;
import com.medicarelk.app.services.MediCareFirebaseMessagingService;
import com.medicarelk.app.utils.ImageUtils;
import com.medicarelk.app.utils.NotificationHelper;

import java.util.List;

/**
 * Home Activity representing the Patient Health Dashboard matching the prototype s-home layout.
 */
public class HomeActivity extends AppCompatActivity {

    private PatientRepository patientRepository;
    private AppointmentRepository appointmentRepository;
    private DoctorRepository doctorRepository;
    private NotificationRepository notificationRepository;

    private ImageView ivPatientAvatar;
    private TextView tvPatientName;
    private TextView tvPatientLocation;
    private ProgressBar progressBarHome;

    private View layoutAppointmentDetails;
    private View layoutNoAppointments;
    private TextView tvAppointmentDoctorName;
    private TextView tvAppointmentDoctorSpecialty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        // Set transparent status bar to eliminate any status bar layer/strip
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_home);

        View rootView = findViewById(R.id.rootHome);
        View homeHeader = findViewById(R.id.homeHeader);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                if (homeHeader != null) {
                    int basePadding = (int) (64 * getResources().getDisplayMetrics().density);
                    int topPadding = Math.max(systemBars.top + (int) (16 * getResources().getDisplayMetrics().density), basePadding);
                    homeHeader.setPadding(
                            homeHeader.getPaddingLeft(),
                            topPadding,
                            homeHeader.getPaddingRight(),
                            homeHeader.getPaddingBottom()
                    );
                }
                v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        initViews();

        patientRepository = new PatientRepository();
        appointmentRepository = new AppointmentRepository();
        doctorRepository = new DoctorRepository();
        notificationRepository = new NotificationRepository();

        setupBottomNavigation();
        setupListeners();
        setupFeaturedDoctors();

        NotificationHelper.requestNotificationPermission(this);
        syncFcmToken();

        // Listen in real-time for new notifications to trigger mobile system tray alerts
        notificationRepository.listenForLiveNotifications(this, currentUser.getUid());

        loadPatientProfile(currentUser.getUid());
        loadUpcomingAppointment(currentUser.getUid());
        loadUnreadNotificationCount(currentUser.getUid());
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            loadPatientProfile(currentUser.getUid());
        }
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }

    private void initViews() {
        ivPatientAvatar = findViewById(R.id.ivPatientAvatar);
        tvPatientName = findViewById(R.id.tvPatientName);
        tvPatientLocation = findViewById(R.id.tvPatientLocation);
        progressBarHome = findViewById(R.id.progressBarHome);

        layoutAppointmentDetails = findViewById(R.id.layoutAppointmentDetails);
        layoutNoAppointments = findViewById(R.id.layoutNoAppointments);
        tvAppointmentDoctorName = findViewById(R.id.tvAppointmentDoctorName);
        tvAppointmentDoctorSpecialty = findViewById(R.id.tvAppointmentDoctorSpecialty);
    }

    private void syncFcmToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                MediCareFirebaseMessagingService.saveTokenToUserAccount(task.getResult());
            }
        });
    }

    private void loadUnreadNotificationCount(String uid) {
        notificationRepository.getPatientNotifications(uid, new NotificationRepository.OnNotificationsLoadedListener() {
            @Override
            public void onSuccess(List<Notification> notifications, int unreadCount) {
                View vNotificationBadge = findViewById(R.id.vNotificationBadge);
                if (vNotificationBadge != null) {
                    vNotificationBadge.setVisibility(unreadCount > 0 ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onError(String errorMessage) {}
        });
    }

    private void loadPatientProfile(String uid) {
        if (progressBarHome != null) progressBarHome.setVisibility(View.VISIBLE);

        patientRepository.getPatientProfile(uid, new PatientRepository.OnPatientLoadedListener() {
            @Override
            public void onSuccess(Patient patient) {
                if (progressBarHome != null) progressBarHome.setVisibility(View.GONE);

                if (!patient.isPatientRole()) {
                    Toast.makeText(HomeActivity.this, "Access restricted to patients.", Toast.LENGTH_LONG).show();
                    redirectToLogin();
                    return;
                }

                String nameText = "Hi, " + patient.getFullName() + " 👋";
                if (tvPatientName != null) tvPatientName.setText(nameText);
                if (patient.getAddress() != null && !patient.getAddress().isEmpty() && tvPatientLocation != null) {
                    tvPatientLocation.setText(patient.getAddress());
                }

                if (ivPatientAvatar != null) {
                    ImageUtils.loadProfileImage(HomeActivity.this, patient.getProfileImageUrl(), ivPatientAvatar);
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (progressBarHome != null) progressBarHome.setVisibility(View.GONE);
                if (tvPatientName != null) tvPatientName.setText("Hi, Patient 👋");
            }
        });
    }

    private void loadUpcomingAppointment(String uid) {
        appointmentRepository.getUpcomingAppointment(uid, new AppointmentRepository.OnAppointmentDetailsLoadedListener() {
            @Override
            public void onSuccess(Appointment appointment) {
                if (layoutAppointmentDetails != null) layoutAppointmentDetails.setVisibility(View.VISIBLE);
                if (layoutNoAppointments != null) layoutNoAppointments.setVisibility(View.GONE);

                if (tvAppointmentDoctorName != null) tvAppointmentDoctorName.setText(appointment.getDoctorNameSnapshot());
                String details = appointment.getSpecializationSnapshot() + " • " + appointment.getAppointmentTime();
                if (tvAppointmentDoctorSpecialty != null) tvAppointmentDoctorSpecialty.setText(details);
            }

            @Override
            public void onError(String errorMessage) {
                if (layoutAppointmentDetails != null) layoutAppointmentDetails.setVisibility(View.GONE);
                if (layoutNoAppointments != null) layoutNoAppointments.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupFeaturedDoctors() {
        List<Doctor> doctors = doctorRepository.getSampleDoctors();
        DoctorAdapter doctorAdapter = new DoctorAdapter(doctors, doctor -> {
            Intent intent = new Intent(HomeActivity.this, DoctorDetailsActivity.class);
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
        RecyclerView rvTopDoctors = findViewById(R.id.rvTopDoctors);
        if (rvTopDoctors != null) rvTopDoctors.setAdapter(doctorAdapter);
    }

    private void setupListeners() {
        ImageButton btnNotification = findViewById(R.id.btnNotification);
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, NotificationsActivity.class)));
        }

        View btnEmergencyCall = findViewById(R.id.btnEmergencyCall);
        if (btnEmergencyCall != null) {
            btnEmergencyCall.setOnClickListener(v -> {
                try {
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                    dialIntent.setData(Uri.parse("tel:1990"));
                    startActivity(dialIntent);
                } catch (Exception e) {
                    Toast.makeText(HomeActivity.this, "Dialing Emergency 1990...", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Quick Services
        bindViewClick(R.id.quickDoctors, DoctorsActivity.class);
        bindViewClick(R.id.quickHospitals, HospitalsActivity.class);
        bindViewClick(R.id.quickLabTests, LabTestsActivity.class);
        bindViewClick(R.id.quickPackages, HealthPackagesActivity.class);
        bindViewClick(R.id.quickAppointments, AppointmentsActivity.class);
        bindViewClick(R.id.quickRecords, MedicalHistoryActivity.class);
        bindViewClick(R.id.quickTips, HealthTipsActivity.class);
        bindViewClick(R.id.quickOffers, PromotionsActivity.class);

        bindViewClick(R.id.cardUpcomingAppt, AppointmentsActivity.class);
        bindViewClick(R.id.tvSeeAllDoctors, DoctorsActivity.class);
        bindViewClick(R.id.tvSeeAllPackages, HealthPackagesActivity.class);
        bindViewClick(R.id.cardFeaturedPackage, HealthPackageDetailsActivity.class);
    }

    private void bindViewClick(int viewId, Class<?> targetActivity) {
        View view = findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(v -> navigateToTab(targetActivity));
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
            bottomNavigation.setSelectedItemId(R.id.nav_home);
            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
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
                } else if (itemId == R.id.nav_profile) {
                    navigateToTab(ProfileActivity.class);
                    return true;
                }
                return false;
            });
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}