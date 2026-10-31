package com.medicarelk.app.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.medicarelk.app.R;
import com.medicarelk.app.adapters.SlotAdapter;
import com.medicarelk.app.repositories.AppointmentRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Reschedule Appointment Activity allowing patients to update consultation date and time slot.
 */
public class RescheduleAppointmentActivity extends AppCompatActivity {

    private AppointmentRepository appointmentRepository;

    private TextView tvDoctorName;
    private TextView tvCurrentSlot;
    private TextView tvNewDate;
    private RecyclerView rvTimeSlots;
    private Button btnConfirmReschedule;
    private ProgressBar progressBarReschedule;

    private String appointmentId;
    private String doctorId;
    private String newSelectedDate = "";
    private String newSelectedTime = "";
    private long newSelectedTimestamp = 0;

    private List<String> availableSlots = new ArrayList<>();
    private SlotAdapter slotAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Security check
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_reschedule_appointment);

        View rootView = findViewById(R.id.rootReschedule);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        appointmentId = getIntent().getStringExtra("appointmentId");
        doctorId = getIntent().getStringExtra("doctorId");
        String doctorName = getIntent().getStringExtra("doctorName");
        String currentDate = getIntent().getStringExtra("currentDate");
        String currentTime = getIntent().getStringExtra("currentTime");

        appointmentRepository = new AppointmentRepository();

        initViews();

        if (doctorName != null && tvDoctorName != null) tvDoctorName.setText(doctorName);
        if (tvCurrentSlot != null) tvCurrentSlot.setText("Current: " + currentDate + " | " + currentTime);

        // Default date
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        newSelectedDate = sdf.format(cal.getTime());
        if (tvNewDate != null) tvNewDate.setText("New Date: " + newSelectedDate);

        setupSlotRecyclerView();
        setupListeners();

        loadAvailableSlots();
    }

    private void initViews() {
        tvDoctorName = findViewById(R.id.tvDoctorName);
        tvCurrentSlot = findViewById(R.id.tvCurrentSlot);
        tvNewDate = findViewById(R.id.tvNewDate);
        rvTimeSlots = findViewById(R.id.rvTimeSlots);
        btnConfirmReschedule = findViewById(R.id.btnConfirmReschedule);
        progressBarReschedule = findViewById(R.id.progressBarReschedule);
    }

    private void setupSlotRecyclerView() {
        slotAdapter = new SlotAdapter(availableSlots, time -> {
            newSelectedTime = time;
            calculateTimestamp();
        });
        if (rvTimeSlots != null) {
            rvTimeSlots.setAdapter(slotAdapter);
        }
    }

    private void setupListeners() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        View cardSelectNewDate = findViewById(R.id.cardSelectNewDate);
        if (cardSelectNewDate != null) cardSelectNewDate.setOnClickListener(v -> showDatePicker());

        if (btnConfirmReschedule != null) {
            btnConfirmReschedule.setOnClickListener(v -> confirmReschedule());
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    newSelectedDate = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    if (tvNewDate != null) tvNewDate.setText("New Date: " + newSelectedDate);
                    loadAvailableSlots();
                }, year, month, day);

        // Block past dates
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void loadAvailableSlots() {
        if (doctorId == null || newSelectedDate.isEmpty()) return;

        showLoading(true);

        List<String> allSlots = new ArrayList<>();
        allSlots.add("09:00 AM");
        allSlots.add("09:30 AM");
        allSlots.add("10:00 AM");
        allSlots.add("10:30 AM");
        allSlots.add("11:00 AM");
        allSlots.add("11:30 AM");
        allSlots.add("02:00 PM");
        allSlots.add("02:30 PM");
        allSlots.add("03:00 PM");

        appointmentRepository.getBookedTimeSlots(doctorId, newSelectedDate, bookedSlots -> {
            showLoading(false);
            availableSlots.clear();

            for (String slot : allSlots) {
                if (!bookedSlots.contains(slot)) {
                    availableSlots.add(slot);
                }
            }

            if (slotAdapter != null) {
                slotAdapter.notifyDataSetChanged();
            }

            if (!availableSlots.isEmpty()) {
                newSelectedTime = availableSlots.get(0);
                if (slotAdapter != null) slotAdapter.setSelectedSlot(newSelectedTime);
            } else {
                newSelectedTime = "";
                Toast.makeText(RescheduleAppointmentActivity.this, "No available slots for " + newSelectedDate, Toast.LENGTH_SHORT).show();
            }
            calculateTimestamp();
        });
    }

    private void calculateTimestamp() {
        if (newSelectedDate.isEmpty() || newSelectedTime.isEmpty()) return;
        try {
            String fullStr = newSelectedDate + " " + newSelectedTime;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US);
            Date date = sdf.parse(fullStr);
            if (date != null) {
                newSelectedTimestamp = date.getTime();
            }
        } catch (ParseException e) {
            newSelectedTimestamp = System.currentTimeMillis();
        }
    }

    private void confirmReschedule() {
        if (newSelectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a new date.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newSelectedTime.isEmpty()) {
            Toast.makeText(this, "Please select an available time slot.", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        appointmentRepository.rescheduleAppointment(appointmentId, doctorId, newSelectedDate, newSelectedTime, newSelectedTimestamp, new AppointmentRepository.OnAppointmentUpdatedListener() {
            @Override
            public void onSuccess() {
                showLoading(false);
                Toast.makeText(RescheduleAppointmentActivity.this, "Appointment rescheduled successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                Toast.makeText(RescheduleAppointmentActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (progressBarReschedule != null) {
            progressBarReschedule.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (btnConfirmReschedule != null) {
            btnConfirmReschedule.setEnabled(!isLoading);
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(RescheduleAppointmentActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}