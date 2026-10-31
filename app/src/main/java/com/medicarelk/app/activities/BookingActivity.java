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
import com.medicarelk.app.models.Doctor;
import com.medicarelk.app.repositories.AppointmentRepository;
import com.medicarelk.app.repositories.DoctorRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Booking Activity handling the appointment creation workflow and Double-Booking Prevention.
 */
public class BookingActivity extends AppCompatActivity {

    private DoctorRepository doctorRepository;
    private AppointmentRepository appointmentRepository;
    private Doctor currentDoctor;

    private TextView tvDoctorName;
    private TextView tvDoctorSpecialty;
    private TextView tvHospitalLocation;
    private TextView tvConsultationFee;
    private TextView tvSelectedDate;
    private TextView tvSummaryDoctor;
    private TextView tvSummaryDateTime;
    private TextView tvSummaryType;
    private TextView tvSummaryFee;
    private ChipGroup chipGroupType;
    private Chip chipInPerson;
    private Chip chipOnline;
    private RecyclerView rvTimeSlots;
    private Button btnConfirmAppointment;
    private ProgressBar progressBarBooking;

    private String intentDoctorId;
    private String intentDoctorName;
    private String intentDoctorSpecialty;
    private String intentHospitalName;
    private String intentLocation;
    private double intentConsultationFee;
    private String intentProfileImageUrl;

    private String selectedConsultationType = "In-person";
    private String selectedDate = "";
    private String selectedTime = "";
    private long selectedTimestamp = 0;
    private final List<String> availableSlots = new ArrayList<>();
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

        setContentView(R.layout.activity_booking);

        View rootView = findViewById(R.id.rootBooking);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        intentDoctorId = getIntent().getStringExtra("doctorId");
        intentDoctorName = getIntent().getStringExtra("doctorName");
        intentDoctorSpecialty = getIntent().getStringExtra("doctorSpecialty");
        intentHospitalName = getIntent().getStringExtra("hospitalName");
        intentLocation = getIntent().getStringExtra("location");
        intentConsultationFee = getIntent().getDoubleExtra("consultationFee", 3500.0);
        intentProfileImageUrl = getIntent().getStringExtra("profileImageUrl");

        String selectedDoctorId = intentDoctorId != null && !intentDoctorId.trim().isEmpty() ? intentDoctorId : "doc101";

        doctorRepository = new DoctorRepository();
        appointmentRepository = new AppointmentRepository();

        initViews();
        setupListeners();
        setupSlotRecyclerView();

        loadDoctorDetails(selectedDoctorId);
    }

    private void initViews() {
        tvDoctorName = findViewById(R.id.tvDoctorName);
        tvDoctorSpecialty = findViewById(R.id.tvDoctorSpecialty);
        tvHospitalLocation = findViewById(R.id.tvHospitalLocation);
        tvConsultationFee = findViewById(R.id.tvConsultationFee);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvSummaryDoctor = findViewById(R.id.tvSummaryDoctor);
        tvSummaryDateTime = findViewById(R.id.tvSummaryDateTime);
        tvSummaryType = findViewById(R.id.tvSummaryType);
        tvSummaryFee = findViewById(R.id.tvSummaryFee);
        chipGroupType = findViewById(R.id.chipGroupType);
        chipInPerson = findViewById(R.id.chipInPerson);
        chipOnline = findViewById(R.id.chipOnline);
        rvTimeSlots = findViewById(R.id.rvTimeSlots);
        btnConfirmAppointment = findViewById(R.id.btnConfirmAppointment);
        progressBarBooking = findViewById(R.id.progressBarBooking);
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

    private void loadDoctorDetails(String doctorId) {
        showLoading(true);

        doctorRepository.getDoctorById(doctorId, new DoctorRepository.OnDoctorLoadedListener() {
            @Override
            public void onSuccess(Doctor doctor) {
                showLoading(false);

                // Override doctor details with Intent parameters if provided
                if (intentDoctorName != null && !intentDoctorName.trim().isEmpty()) {
                    doctor.setFullName(intentDoctorName);
                }
                if (intentDoctorSpecialty != null && !intentDoctorSpecialty.trim().isEmpty()) {
                    doctor.setSpecialization(intentDoctorSpecialty);
                }
                if (intentHospitalName != null && !intentHospitalName.trim().isEmpty()) {
                    doctor.setHospitalName(intentHospitalName);
                }
                if (intentLocation != null && !intentLocation.trim().isEmpty()) {
                    doctor.setLocation(intentLocation);
                }
                if (intentConsultationFee > 0) {
                    doctor.setConsultationFee(intentConsultationFee);
                }
                if (intentProfileImageUrl != null && !intentProfileImageUrl.trim().isEmpty()) {
                    doctor.setProfileImageUrl(intentProfileImageUrl);
                }

                // Set currentDoctor FIRST so listeners/summaries can safely read it
                currentDoctor = doctor;

                if (!doctor.isActive()) {
                    Toast.makeText(BookingActivity.this, "This doctor is currently unavailable for booking.", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                if (tvDoctorName != null) tvDoctorName.setText(doctor.getFullName());
                if (tvDoctorSpecialty != null) tvDoctorSpecialty.setText(doctor.getSpecialization());
                String loc = doctor.getHospitalName() + " - " + doctor.getLocation();
                if (tvHospitalLocation != null) tvHospitalLocation.setText(loc);
                if (tvConsultationFee != null) tvConsultationFee.setText("Fee: " + doctor.getFormattedFee());

                // Configure available consultation types
                if (chipInPerson != null) chipInPerson.setEnabled(doctor.isInPersonConsultationAvailable());
                if (chipOnline != null) chipOnline.setEnabled(doctor.isOnlineConsultationAvailable());

                if (doctor.isInPersonConsultationAvailable()) {
                    selectedConsultationType = "In-person";
                    if (chipInPerson != null) chipInPerson.setChecked(true);
                } else if (doctor.isOnlineConsultationAvailable()) {
                    selectedConsultationType = "Online";
                    if (chipOnline != null) chipOnline.setChecked(true);
                }

                // Default date: Today
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                selectedDate = sdf.format(cal.getTime());
                if (tvSelectedDate != null) tvSelectedDate.setText("Selected Date: " + selectedDate);

                loadAvailableSlots();
                updateSummary();
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                Toast.makeText(BookingActivity.this, "Unable to load doctor information.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupListeners() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        View cardSelectDate = findViewById(R.id.cardSelectDate);
        if (cardSelectDate != null) cardSelectDate.setOnClickListener(v -> showDatePicker());

        if (chipGroupType != null) {
            chipGroupType.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.chipOnline) {
                    selectedConsultationType = "Online";
                } else {
                    selectedConsultationType = "In-person";
                }
                updateSummary();
            });
        }

        if (btnConfirmAppointment != null) {
            btnConfirmAppointment.setOnClickListener(v -> confirmBooking());
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
                    loadAvailableSlots();
                    updateSummary();
                }, year, month, day);

        // Block past dates
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void loadAvailableSlots() {
        if (currentDoctor == null || selectedDate.isEmpty()) return;

        showLoading(true);

        // Generate standard 30-minute consultation slots
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
        allSlots.add("03:30 PM");
        allSlots.add("04:00 PM");

        // Query Firestore booked slots to exclude double booking
        appointmentRepository.getBookedTimeSlots(currentDoctor.getDoctorId(), selectedDate, bookedSlots -> {
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
                selectedTime = availableSlots.get(0);
                if (slotAdapter != null) slotAdapter.setSelectedSlot(selectedTime);
            } else {
                selectedTime = "";
                Toast.makeText(BookingActivity.this, "No available slots for " + selectedDate, Toast.LENGTH_SHORT).show();
            }
            calculateTimestamp();
            updateSummary();
        });
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
        if (currentDoctor != null && tvSummaryDoctor != null) {
            tvSummaryDoctor.setText("Doctor: " + currentDoctor.getFullName());
        }
        if (tvSummaryDateTime != null) {
            tvSummaryDateTime.setText("Date & Time: " + selectedDate + " | " + selectedTime);
        }
        if (tvSummaryType != null) {
            tvSummaryType.setText("Type: " + selectedConsultationType);
        }
        if (currentDoctor != null && tvSummaryFee != null) {
            tvSummaryFee.setText("Total Fee: " + currentDoctor.getFormattedFee());
        }
    }

    private void confirmBooking() {
        if (currentDoctor == null) return;
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select an appointment date.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedTime.isEmpty()) {
            Toast.makeText(this, "Please select an available time slot.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        final String docName = currentDoctor.getFullName();
        final String hospName = currentDoctor.getHospitalName();
        final double feeAmt = currentDoctor.getConsultationFee();

        String patientName = currentUser.getEmail() != null ? currentUser.getEmail().split("@")[0] : "Patient";

        Appointment appointment = new Appointment(
                "",
                currentUser.getUid(),
                patientName,
                currentDoctor.getDoctorId(),
                docName,
                currentDoctor.getSpecialization(),
                hospName,
                currentDoctor.getLocation(),
                selectedDate,
                selectedTime,
                selectedTimestamp,
                selectedConsultationType,
                feeAmt,
                Appointment.STATUS_CONFIRMED
        );
        appointment.setDoctorProfileImageUrlSnapshot(currentDoctor.getProfileImageUrl());

        showLoading(true);

        // Atomic double-booking prevention transaction
        appointmentRepository.createAppointmentWithTransaction(appointment, new AppointmentRepository.OnAppointmentCreatedListener() {
            @Override
            public void onSuccess(String appointmentId) {
                showLoading(false);
                Intent intent = new Intent(BookingActivity.this, BookingSuccessActivity.class);
                intent.putExtra("appointmentId", appointmentId);
                intent.putExtra("doctorName", docName);
                intent.putExtra("appointmentDate", selectedDate);
                intent.putExtra("appointmentTime", selectedTime);
                intent.putExtra("consultationType", selectedConsultationType);
                intent.putExtra("hospitalName", hospName);
                intent.putExtra("consultationFee", feeAmt);
                startActivity(intent);
                finish();
            }

            @Override
            public void onDoubleBookingError() {
                showLoading(false);
                Toast.makeText(BookingActivity.this, "This appointment slot is no longer available. Please select another time.", Toast.LENGTH_LONG).show();
                loadAvailableSlots(); // Refresh slots
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                Toast.makeText(BookingActivity.this, "Booking failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (progressBarBooking != null) {
            progressBarBooking.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (btnConfirmAppointment != null) {
            btnConfirmAppointment.setEnabled(!isLoading);
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(BookingActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}