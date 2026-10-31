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

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.medicarelk.app.R;
import com.medicarelk.app.adapters.SlotAdapter;
import com.medicarelk.app.models.Appointment;
import com.medicarelk.app.repositories.AppointmentRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Lab Test Booking Activity allowing patients to select center location, collection date, and time slot.
 */
public class LabTestBookingActivity extends AppCompatActivity {

    private AppointmentRepository appointmentRepository;

    private TextView tvTestName;
    private TextView tvCategory;
    private TextView tvPrice;
    private TextView tvSelectedDate;
    private TextView tvSummaryTest;
    private TextView tvSummaryLocation;
    private TextView tvSummaryDateTime;
    private TextView tvSummaryFee;
    private ChipGroup chipGroupLocation;
    private RecyclerView rvTimeSlots;
    private Button btnConfirmLabBooking;
    private ProgressBar progressBarLabBooking;

    private String testName = "";
    private String category = "";
    private double testPrice = 1800.0;
    private String selectedLocation = "MediCare Hospital Colombo";
    private String selectedDate = "";
    private String selectedTime = "";
    private long selectedTimestamp = 0;
    private final List<String> availableSlots = new ArrayList<>();
    private SlotAdapter slotAdapter;

    private long lastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Security check
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_lab_test_booking);

        View rootView = findViewById(R.id.rootLabBooking);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        testName = getIntent().getStringExtra("name");
        if (testName == null || testName.isEmpty()) testName = "Full Blood Count (FBC)";
        category = getIntent().getStringExtra("category");
        if (category == null || category.isEmpty()) category = "Blood Tests";
        testPrice = getIntent().getDoubleExtra("price", 1800.0);

        appointmentRepository = new AppointmentRepository();

        initViews();
        bindTestInfo();
        setupListeners();
        setupSlotRecyclerView();

        // Default Date
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        selectedDate = sdf.format(cal.getTime());
        if (tvSelectedDate != null) tvSelectedDate.setText("Selected Date: " + selectedDate);

        loadSlots();
        updateSummary();
    }

    private void initViews() {
        tvTestName = findViewById(R.id.tvTestName);
        tvCategory = findViewById(R.id.tvCategory);
        tvPrice = findViewById(R.id.tvPrice);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvSummaryTest = findViewById(R.id.tvSummaryTest);
        tvSummaryLocation = findViewById(R.id.tvSummaryLocation);
        tvSummaryDateTime = findViewById(R.id.tvSummaryDateTime);
        tvSummaryFee = findViewById(R.id.tvSummaryFee);
        chipGroupLocation = findViewById(R.id.chipGroupLocation);
        rvTimeSlots = findViewById(R.id.rvTimeSlots);
        btnConfirmLabBooking = findViewById(R.id.btnConfirmLabBooking);
        progressBarLabBooking = findViewById(R.id.progressBarLabBooking);
    }

    private void bindTestInfo() {
        if (tvTestName != null) tvTestName.setText(testName);
        if (tvCategory != null) tvCategory.setText(category);
        if (tvPrice != null) tvPrice.setText(String.format(Locale.US, "Price: LKR %,.0f", testPrice));
    }

    private void setupSlotRecyclerView() {
        slotAdapter = new SlotAdapter(availableSlots, time -> {
            selectedTime = time;
            calculateTimestamp();
            updateSummary();
        });
        if (rvTimeSlots != null) {
            rvTimeSlots.setAdapter(slotAdapter);
        }
    }

    private void setupListeners() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        View cardSelectDate = findViewById(R.id.cardSelectDate);
        if (cardSelectDate != null) cardSelectDate.setOnClickListener(v -> showDatePicker());

        if (chipGroupLocation != null) {
            chipGroupLocation.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.chipLocKandy) {
                    selectedLocation = "Asiri Medical Kandy";
                } else if (checkedId == R.id.chipLocGalle) {
                    selectedLocation = "Nawaloka Hospital Galle";
                } else {
                    selectedLocation = "MediCare Hospital Colombo";
                }
                updateSummary();
            });
        }

        if (btnConfirmLabBooking != null) {
            btnConfirmLabBooking.setOnClickListener(v -> confirmLabBooking());
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDate = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    if (tvSelectedDate != null) tvSelectedDate.setText("Selected Date: " + selectedDate);
                    loadSlots();
                    updateSummary();
                }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void loadSlots() {
        availableSlots.clear();
        availableSlots.add("08:00 AM");
        availableSlots.add("08:30 AM");
        availableSlots.add("09:00 AM");
        availableSlots.add("09:30 AM");
        availableSlots.add("10:00 AM");
        availableSlots.add("10:30 AM");
        availableSlots.add("11:00 AM");
        availableSlots.add("02:00 PM");
        availableSlots.add("02:30 PM");

        if (slotAdapter != null) slotAdapter.notifyDataSetChanged();
        if (!availableSlots.isEmpty()) {
            selectedTime = availableSlots.get(0);
            if (slotAdapter != null) slotAdapter.setSelectedSlot(selectedTime);
        }
        calculateTimestamp();
    }

    private void calculateTimestamp() {
        if (selectedDate.isEmpty() || selectedTime.isEmpty()) return;
        try {
            String fullStr = selectedDate + " " + selectedTime;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US);
            Date date = sdf.parse(fullStr);
            if (date != null) {
                selectedTimestamp = date.getTime();
            }
        } catch (ParseException e) {
            selectedTimestamp = System.currentTimeMillis();
        }
    }

    private void updateSummary() {
        if (tvSummaryTest != null) tvSummaryTest.setText("Test: " + testName);
        if (tvSummaryLocation != null) tvSummaryLocation.setText("Center: " + selectedLocation);
        if (tvSummaryDateTime != null) tvSummaryDateTime.setText("Date & Time: " + selectedDate + " | " + selectedTime);
        if (tvSummaryFee != null) tvSummaryFee.setText(String.format(Locale.US, "Total Price: LKR %,.0f", testPrice));
    }

    private void confirmLabBooking() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < 1200) return;
        lastClickTime = currentTime;

        if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(this, "Please select sample collection date and time.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        showLoading(true);

        String patientName = currentUser.getEmail() != null ? currentUser.getEmail().split("@")[0] : "Patient";

        String labImageUrl = getIntent().getStringExtra("imageUrl");

        Appointment labBooking = new Appointment(
                "",
                currentUser.getUid(),
                patientName,
                "lab_center",
                "Diagnostic Laboratory (" + testName + ")",
                category,
                selectedLocation,
                selectedLocation,
                selectedDate,
                selectedTime,
                selectedTimestamp,
                "Lab Test Sample Collection",
                testPrice,
                Appointment.STATUS_CONFIRMED
        );
        if (labImageUrl != null && !labImageUrl.trim().isEmpty()) {
            labBooking.setDoctorProfileImageUrlSnapshot(labImageUrl);
        }

        appointmentRepository.createAppointmentWithTransaction(labBooking, new AppointmentRepository.OnAppointmentCreatedListener() {
            @Override
            public void onSuccess(String appointmentId) {
                showLoading(false);
                Intent intent = new Intent(LabTestBookingActivity.this, BookingSuccessActivity.class);
                intent.putExtra("appointmentId", appointmentId);
                intent.putExtra("doctorName", "Lab Test: " + testName);
                intent.putExtra("appointmentDate", selectedDate);
                intent.putExtra("appointmentTime", selectedTime);
                intent.putExtra("consultationType", "Lab Sample Collection");
                intent.putExtra("hospitalName", selectedLocation);
                intent.putExtra("consultationFee", testPrice);
                startActivity(intent);
                finish();
            }

            @Override
            public void onDoubleBookingError() {
                showLoading(false);
                Toast.makeText(LabTestBookingActivity.this, "This slot is no longer available. Please select another time.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                Toast.makeText(LabTestBookingActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (progressBarLabBooking != null) progressBarLabBooking.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (btnConfirmLabBooking != null) btnConfirmLabBooking.setEnabled(!isLoading);
    }

    private void redirectToLogin() {
        Intent intent = new Intent(LabTestBookingActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}