package com.medicarelk.app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.medicarelk.app.R;
import com.medicarelk.app.models.Appointment;
import com.medicarelk.app.models.SupportRequest;
import com.medicarelk.app.repositories.AppointmentRepository;
import com.medicarelk.app.repositories.SupportRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Customer Support Ticket Creation Activity.
 */
public class CustomerSupportActivity extends AppCompatActivity {

    private SupportRepository supportRepository;
    private AppointmentRepository appointmentRepository;

    private EditText etSubject;
    private Spinner spCategory;
    private Spinner spAppointment;
    private EditText etDescription;
    private Button btnSubmitSupport;
    private ProgressBar progressBarSupport;

    private final List<Appointment> userAppointments = new ArrayList<>();
    private final List<String> appointmentDisplayList = new ArrayList<>();

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

        getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_customer_support);

        View rootView = findViewById(R.id.rootCustomerSupport);
        View toolbarSupport = findViewById(R.id.toolbarSupport);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                if (toolbarSupport != null && systemBars.top > 0) {
                    int basePadding = (int) (72 * getResources().getDisplayMetrics().density);
                    int topPadding = Math.max(systemBars.top + (int) (16 * getResources().getDisplayMetrics().density), basePadding);
                    toolbarSupport.setPadding(
                            toolbarSupport.getPaddingLeft(),
                            topPadding,
                            toolbarSupport.getPaddingRight(),
                            toolbarSupport.getPaddingBottom()
                    );
                }
                v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        initViews();
        setupCategorySpinner();

        supportRepository = new SupportRepository();
        appointmentRepository = new AppointmentRepository();

        setupListeners();
        loadUserAppointments(currentUser.getUid());
    }

    private void initViews() {
        etSubject = findViewById(R.id.etSubject);
        spCategory = findViewById(R.id.spCategory);
        spAppointment = findViewById(R.id.spAppointment);
        etDescription = findViewById(R.id.etDescription);
        btnSubmitSupport = findViewById(R.id.btnSubmitSupport);
        progressBarSupport = findViewById(R.id.progressBarSupport);
    }

    private void setupCategorySpinner() {
        String[] categories = new String[]{"Appointment", "Doctor", "Hospital", "Lab Test", "Health Package", "Account", "Payment", "Technical Issue", "Notification", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_selected, categories);
        adapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        if (spCategory != null) {
            spCategory.setAdapter(adapter);
        }
    }

    private void loadUserAppointments(String userId) {
        appointmentRepository.getPatientAppointments(userId, new AppointmentRepository.OnAppointmentsLoadedListener(){
            @Override
            public void onSuccess(List<Appointment> upcomingList, List<Appointment> historyList, List<Appointment> cancelledList) {
                userAppointments.clear();
                appointmentDisplayList.clear();
                appointmentDisplayList.add("None (General Request)");

                if (upcomingList != null) userAppointments.addAll(upcomingList);
                if (historyList != null) userAppointments.addAll(historyList);

                for (Appointment appt : userAppointments) {
                    String label = appt.getDoctorNameSnapshot() + " (" + appt.getAppointmentDate() + ")";
                    appointmentDisplayList.add(label);
                }

                ArrayAdapter<String> apptAdapter = new ArrayAdapter<>(CustomerSupportActivity.this,
                        R.layout.spinner_item_selected, appointmentDisplayList);
                apptAdapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
                if (spAppointment != null) {
                    spAppointment.setAdapter(apptAdapter);
                }
            }

            @Override
            public void onError(String errorMessage) {
                appointmentDisplayList.clear();
                appointmentDisplayList.add("None (General Request)");
                ArrayAdapter<String> apptAdapter = new ArrayAdapter<>(CustomerSupportActivity.this,
                        R.layout.spinner_item_selected, appointmentDisplayList);
                apptAdapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
                if (spAppointment != null) {
                    spAppointment.setAdapter(apptAdapter);
                }
            }
        });
    }

    private void setupListeners() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnSubmitSupport != null) {
            btnSubmitSupport.setOnClickListener(v -> submitSupportRequest());
        }
    }

    private void submitSupportRequest() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < 1200) return;
        lastClickTime = currentTime;

        String subject = etSubject != null ? etSubject.getText().toString().trim() : "";
        String category = spCategory != null && spCategory.getSelectedItem() != null ? spCategory.getSelectedItem().toString() : "General";
        String description = etDescription != null ? etDescription.getText().toString().trim() : "";

        if (subject.isEmpty() || subject.length() < 4) {
            if (etSubject != null) etSubject.setError("Subject must be at least 4 characters");
            return;
        }

        if (description.isEmpty() || description.length() < 10) {
            if (etDescription != null) etDescription.setError("Please explain your issue in detail (min 10 characters)");
            return;
        }

        // Selected appointment ID verification
        String linkedAppointmentId = "";
        int selectedApptIndex = spAppointment != null ? spAppointment.getSelectedItemPosition() : 0;
        if (selectedApptIndex > 0 && (selectedApptIndex - 1) < userAppointments.size()) {
            linkedAppointmentId = userAppointments.get(selectedApptIndex - 1).getAppointmentId();
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            redirectToLogin();
            return;
        }

        showLoading(true);

        SupportRequest request = new SupportRequest(
                "",
                user.getUid(),
                subject,
                category,
                description,
                linkedAppointmentId
        );

        supportRepository.createSupportRequest(request, new SupportRepository.OnSupportRequestCreatedListener() {
            @Override
            public void onSuccess(String requestId) {
                showLoading(false);
                Toast.makeText(CustomerSupportActivity.this, "Support request submitted successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(CustomerSupportActivity.this, SupportRequestDetailsActivity.class);
                intent.putExtra("requestId", requestId);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                Toast.makeText(CustomerSupportActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (progressBarSupport != null) {
            progressBarSupport.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (btnSubmitSupport != null) {
            btnSubmitSupport.setEnabled(!isLoading);
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(CustomerSupportActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}