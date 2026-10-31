package com.medicarelk.app.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.medicarelk.app.R;
import com.medicarelk.app.models.Appointment;
import com.medicarelk.app.models.Doctor;
import com.medicarelk.app.repositories.AppointmentRepository;
import com.medicarelk.app.repositories.DoctorRepository;
import com.medicarelk.app.utils.ImageUtils;
import com.medicarelk.app.utils.QRCodeGenerator;

/**
 * Appointment Details Activity matching prototype s-appt-details layout.
 * Shows queue token banner, REAL scannable QR Code, doctor info, date/time/hospital details, and directions/cancel/reschedule actions.
 */
public class AppointmentDetailsActivity extends AppCompatActivity {

    private AppointmentRepository appointmentRepository;
    private Appointment currentAppointment;
    private String appointmentId;

    private TextView tvQueueToken;
    private TextView tvStatus;
    private ImageView ivQRCode;
    private ImageView ivDoctorAvatar;
    private TextView tvDoctorName;
    private TextView tvDoctorSpecialty;
    private TextView tvHospital;
    private TextView tvDate;
    private TextView tvTime;
    private TextView tvType;
    private View btnGetDirections;
    private Button btnReschedule;
    private Button btnCancel;
    private ProgressBar progressBarDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_appointment_details);

        View rootView = findViewById(R.id.rootAppointmentDetails);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        appointmentId = getIntent().getStringExtra("appointmentId");
        appointmentRepository = new AppointmentRepository();

        initViews();
        setupListeners();

        loadAppointmentDetails();
    }

    private void initViews() {
        tvQueueToken = findViewById(R.id.tvQueueToken);
        tvStatus = findViewById(R.id.tvStatus);
        ivQRCode = findViewById(R.id.ivQRCode);
        ivDoctorAvatar = findViewById(R.id.ivDoctorAvatar);
        tvDoctorName = findViewById(R.id.tvDoctorName);
        tvDoctorSpecialty = findViewById(R.id.tvDoctorSpecialty);
        tvHospital = findViewById(R.id.tvHospital);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        tvType = findViewById(R.id.tvType);
        btnGetDirections = findViewById(R.id.btnGetDirections);
        btnReschedule = findViewById(R.id.btnReschedule);
        btnCancel = findViewById(R.id.btnCancel);
        progressBarDetails = findViewById(R.id.progressBarDetails);
    }

    private void loadAppointmentDetails() {
        showLoading(true);

        appointmentRepository.getAppointmentById(appointmentId, new AppointmentRepository.OnAppointmentDetailsLoadedListener() {
            @Override
            public void onSuccess(Appointment appointment) {
                showLoading(false);
                currentAppointment = appointment;

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null || !user.getUid().equals(appointment.getPatientId())) {
                    Toast.makeText(AppointmentDetailsActivity.this, "Unauthorized access.", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                bindAppointmentData(appointment);
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                Toast.makeText(AppointmentDetailsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void bindAppointmentData(Appointment appointment) {
        if (tvDoctorName != null) tvDoctorName.setText(appointment.getDoctorNameSnapshot());
        if (tvDoctorSpecialty != null) tvDoctorSpecialty.setText(appointment.getSpecializationSnapshot());
        if (tvHospital != null) tvHospital.setText(appointment.getHospitalNameSnapshot());
        if (tvDate != null) tvDate.setText(appointment.getAppointmentDate());
        if (tvTime != null) tvTime.setText(appointment.getAppointmentTime());
        if (tvType != null) tvType.setText(appointment.getConsultationType());

        // Load Doctor Profile Avatar Image
        if (ivDoctorAvatar != null) {
            String imgUrl = appointment.getDoctorProfileImageUrlSnapshot();
            if (imgUrl != null && !imgUrl.trim().isEmpty()) {
                ImageUtils.loadProfileImage(this, imgUrl, ivDoctorAvatar);
            } else if (appointment.getDoctorId() != null && !appointment.getDoctorId().trim().isEmpty()) {
                DoctorRepository docRepo = new DoctorRepository();
                docRepo.getDoctorById(appointment.getDoctorId(), new com.medicarelk.app.repositories.DoctorRepository.OnDoctorLoadedListener() {
                    @Override
                    public void onSuccess(com.medicarelk.app.models.Doctor doctor) {
                        if (doctor != null && doctor.getProfileImageUrl() != null && !doctor.getProfileImageUrl().trim().isEmpty()) {
                            ImageUtils.loadProfileImage(AppointmentDetailsActivity.this, doctor.getProfileImageUrl(), ivDoctorAvatar);
                        } else {
                            ImageUtils.loadProfileImage(AppointmentDetailsActivity.this, null, ivDoctorAvatar);
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        ImageUtils.loadProfileImage(AppointmentDetailsActivity.this, null, ivDoctorAvatar);
                    }
                });
            } else {
                ImageUtils.loadProfileImage(this, null, ivDoctorAvatar);
            }
        }

        String apptId = appointment.getAppointmentId();
        String token = (apptId != null && apptId.length() >= 3) ? "A-" + apptId.substring(apptId.length() - 3) : "A-042";

        if (tvQueueToken != null) {
            tvQueueToken.setText(token);
        }

        String effStatus = appointment.getEffectiveStatus();
        if (tvStatus != null) {
            if (Appointment.STATUS_COMPLETED.equalsIgnoreCase(effStatus)) {
                tvStatus.setText("Closed");
            } else if (Appointment.STATUS_CANCELLED.equalsIgnoreCase(effStatus)) {
                tvStatus.setText("Cancelled");
            } else {
                tvStatus.setText("Upcoming");
            }
        }

        // Generate REAL scannable QR Code
        if (ivQRCode != null) {
            String qrPayload = "MEDICARE-LK APPOINTMENT VERIFICATION\n" +
                    "Queue Token: " + token + "\n" +
                    "Appointment ID: " + (apptId != null ? apptId : "") + "\n" +
                    "Doctor: " + appointment.getDoctorNameSnapshot() + "\n" +
                    "Date & Time: " + appointment.getAppointmentDate() + " " + appointment.getAppointmentTime() + "\n" +
                    "Hospital: " + appointment.getHospitalNameSnapshot();

            int sizePx = (int) (180 * getResources().getDisplayMetrics().density);
            Bitmap qrBitmap = QRCodeGenerator.generateQRCode(qrPayload, sizePx, sizePx);
            if (qrBitmap != null) {
                ivQRCode.setImageBitmap(qrBitmap);
            }
        }

        boolean isUpcoming = appointment.isUpcoming();
        if (btnReschedule != null) btnReschedule.setVisibility(isUpcoming ? View.VISIBLE : View.GONE);
        if (btnCancel != null) btnCancel.setVisibility(isUpcoming ? View.VISIBLE : View.GONE);
    }

    private void setupListeners() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnGetDirections != null) {
            btnGetDirections.setOnClickListener(v -> {
                String hosp = currentAppointment != null ? currentAppointment.getHospitalNameSnapshot() : "Nawaloka Hospital";
                try {
                    Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(hosp));
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                } catch (Exception e) {
                    Toast.makeText(AppointmentDetailsActivity.this, "Opening maps...", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> showCancelConfirmationDialog());
        }

        if (btnReschedule != null) {
            btnReschedule.setOnClickListener(v -> {
                if (currentAppointment != null) {
                    Intent intent = new Intent(AppointmentDetailsActivity.this, RescheduleAppointmentActivity.class);
                    intent.putExtra("appointmentId", currentAppointment.getAppointmentId());
                    intent.putExtra("doctorId", currentAppointment.getDoctorId());
                    intent.putExtra("doctorName", currentAppointment.getDoctorNameSnapshot());
                    intent.putExtra("currentDate", currentAppointment.getAppointmentDate());
                    intent.putExtra("currentTime", currentAppointment.getAppointmentTime());
                    startActivity(intent);
                }
            });
        }
    }

    private void showCancelConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Appointment")
                .setMessage("Are you sure you want to cancel this appointment?")
                .setPositiveButton("Cancel Appointment", (dialog, which) -> performCancellation())
                .setNegativeButton("Keep Appointment", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void performCancellation() {
        if (currentAppointment == null) return;

        showLoading(true);

        appointmentRepository.cancelAppointment(currentAppointment, new AppointmentRepository.OnAppointmentUpdatedListener() {
            @Override
            public void onSuccess() {
                showLoading(false);
                Toast.makeText(AppointmentDetailsActivity.this, "Appointment cancelled successfully.", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                Toast.makeText(AppointmentDetailsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (progressBarDetails != null) {
            progressBarDetails.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (btnCancel != null) btnCancel.setEnabled(!isLoading);
        if (btnReschedule != null) btnReschedule.setEnabled(!isLoading);
    }

    private void redirectToLogin() {
        Intent intent = new Intent(AppointmentDetailsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}