package com.medicarelk.app.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.medicarelk.app.R;
import com.medicarelk.app.adapters.AppointmentAdapter;
import com.medicarelk.app.models.Appointment;
import com.medicarelk.app.repositories.AppointmentRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Appointments Activity displaying the authenticated patient's upcoming, history (completed/closed),
 * and cancelled appointment records matching prototype s-appointments layout.
 */
public class AppointmentsActivity extends AppCompatActivity {

    private AppointmentRepository appointmentRepository;
    private AppointmentAdapter adapter;

    private final List<Appointment> upcomingList = new ArrayList<>();
    private final List<Appointment> historyList = new ArrayList<>();
    private final List<Appointment> cancelledList = new ArrayList<>();
    private final List<Appointment> displayList = new ArrayList<>();

    private MaterialButton btnTabUpcoming;
    private MaterialButton btnTabHistory;
    private MaterialButton btnTabCancelled;
    private RecyclerView rvAppointments;
    private View layoutEmptyState;

    private int activeTab = 0; // 0: Upcoming, 1: History, 2: Cancelled

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_appointments);

        View rootView = findViewById(R.id.rootAppointments);
        View toolbarAppointments = findViewById(R.id.toolbarAppointments);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                if (toolbarAppointments != null && systemBars.top > 0) {
                    int basePadding = (int) (72 * getResources().getDisplayMetrics().density);
                    int topPadding = Math.max(systemBars.top + (int) (16 * getResources().getDisplayMetrics().density), basePadding);
                    toolbarAppointments.setPadding(
                            toolbarAppointments.getPaddingLeft(),
                            topPadding,
                            toolbarAppointments.getPaddingRight(),
                            toolbarAppointments.getPaddingBottom()
                    );
                }
                v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        initViews();
        appointmentRepository = new AppointmentRepository();

        setupRecyclerView();
        setupTabs();
        setupBottomNavigation();

        loadPatientAppointments(currentUser.getUid());
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_appointments);
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            loadPatientAppointments(currentUser.getUid());
        }
    }

    private void initViews() {
        btnTabUpcoming = findViewById(R.id.btnTabUpcoming);
        btnTabHistory = findViewById(R.id.btnTabHistory);
        btnTabCancelled = findViewById(R.id.btnTabCancelled);
        rvAppointments = findViewById(R.id.rvAppointments);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
    }

    private void setupRecyclerView() {
        adapter = new AppointmentAdapter(displayList, appointment -> {
            Intent intent = new Intent(AppointmentsActivity.this, AppointmentDetailsActivity.class);
            intent.putExtra("appointmentId", appointment.getAppointmentId());
            startActivity(intent);
        });

        if (rvAppointments != null) {
            rvAppointments.setAdapter(adapter);
        }
    }

    private void setupTabs() {
        if (btnTabUpcoming != null) btnTabUpcoming.setOnClickListener(v -> switchTab(0));
        if (btnTabHistory != null) btnTabHistory.setOnClickListener(v -> switchTab(1));
        if (btnTabCancelled != null) btnTabCancelled.setOnClickListener(v -> switchTab(2));
    }

    private void switchTab(int tabIndex) {
        activeTab = tabIndex;
        updateTabStyles();

        displayList.clear();
        if (tabIndex == 0) {
            displayList.addAll(upcomingList);
        } else if (tabIndex == 1) {
            displayList.addAll(historyList);
        } else {
            displayList.addAll(cancelledList);
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        if (displayList.isEmpty()) {
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
            if (rvAppointments != null) rvAppointments.setVisibility(View.GONE);
        } else {
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
            if (rvAppointments != null) rvAppointments.setVisibility(View.VISIBLE);
        }
    }

    private void updateTabStyles() {
        int teal = ContextCompat.getColor(this, R.color.primary_teal);
        int white = ContextCompat.getColor(this, R.color.white);
        int secondary = ContextCompat.getColor(this, R.color.text_secondary);

        updateSingleTab(btnTabUpcoming, activeTab == 0, teal, white, secondary);
        updateSingleTab(btnTabHistory, activeTab == 1, teal, white, secondary);
        updateSingleTab(btnTabCancelled, activeTab == 2, teal, white, secondary);
    }

    private void updateSingleTab(MaterialButton button, boolean isActive, int activeBg, int activeText, int inactiveText) {
        if (button == null) return;
        if (isActive) {
            button.setBackgroundTintList(ColorStateList.valueOf(activeBg));
            button.setTextColor(activeText);
            button.setStrokeWidth(0);
        } else {
            button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
            button.setTextColor(inactiveText);
            button.setStrokeWidth(2);
            button.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.input_stroke)));
        }
    }

    private void loadPatientAppointments(String patientId) {
        appointmentRepository.getPatientAppointments(patientId, new AppointmentRepository.OnAppointmentsLoadedListener() {
            @Override
            public void onSuccess(List<Appointment> rawUpcoming, List<Appointment> rawHistory, List<Appointment> rawCancelled) {
                upcomingList.clear();
                historyList.clear();
                cancelledList.clear();

                if (rawUpcoming != null) upcomingList.addAll(rawUpcoming);
                if (rawHistory != null) historyList.addAll(rawHistory);
                if (rawCancelled != null) cancelledList.addAll(rawCancelled);

                switchTab(activeTab);
            }

            @Override
            public void onError(String errorMessage) {
                switchTab(activeTab);
            }
        });
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
            bottomNavigation.setSelectedItemId(R.id.nav_appointments);
            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_appointments) {
                    return true;
                } else if (itemId == R.id.nav_home) {
                    navigateToTab(HomeActivity.class);
                    return true;
                } else if (itemId == R.id.nav_doctors) {
                    navigateToTab(DoctorsActivity.class);
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
        Intent intent = new Intent(AppointmentsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}