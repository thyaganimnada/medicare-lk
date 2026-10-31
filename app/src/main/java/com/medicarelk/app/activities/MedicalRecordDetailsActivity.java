package com.medicarelk.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.medicarelk.app.R;
import com.medicarelk.app.models.MedicalRecord;
import com.medicarelk.app.repositories.MedicalHistoryRepository;

/**
 * Medical Record Details Activity displaying complete diagnostic details with Edit and Delete options.
 */
public class MedicalRecordDetailsActivity extends AppCompatActivity {

    private MedicalHistoryRepository repository;

    private String recordId;
    private String conditionName;
    private String diagnosisDate;
    private String status;
    private String description;
    private String notes;

    private TextView tvConditionName;
    private TextView tvStatusBadge;
    private TextView tvDiagnosisDate;
    private TextView tvDescription;
    private TextView tvNotes;
    private Button btnEditRecord;
    private Button btnDeleteRecord;
    private ProgressBar progressBarRecordDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Security check
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_medical_record_details);

        View rootView = findViewById(R.id.rootRecordDetails);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        recordId = getIntent().getStringExtra("recordId");
        conditionName = getIntent().getStringExtra("conditionName");
        diagnosisDate = getIntent().getStringExtra("diagnosisDate");
        status = getIntent().getStringExtra("status");
        description = getIntent().getStringExtra("description");
        notes = getIntent().getStringExtra("notes");

        repository = new MedicalHistoryRepository();

        initViews();
        bindData();
        setupListeners();
    }

    private void initViews() {
        tvConditionName = findViewById(R.id.tvConditionName);
        tvStatusBadge = findViewById(R.id.tvStatusBadge);
        tvDiagnosisDate = findViewById(R.id.tvDiagnosisDate);
        tvDescription = findViewById(R.id.tvDescription);
        tvNotes = findViewById(R.id.tvNotes);
        btnEditRecord = findViewById(R.id.btnEditRecord);
        btnDeleteRecord = findViewById(R.id.btnDeleteRecord);
        progressBarRecordDetails = findViewById(R.id.progressBarRecordDetails);
    }

    private void bindData() {
        if (conditionName != null && tvConditionName != null) tvConditionName.setText(conditionName);
        if (diagnosisDate != null && tvDiagnosisDate != null) tvDiagnosisDate.setText("Diagnosis Date: " + diagnosisDate);
        if (description != null && tvDescription != null) tvDescription.setText(description);
        if (notes != null && tvNotes != null) tvNotes.setText(notes.isEmpty() ? "No additional notes recorded." : notes);

        if (tvStatusBadge != null) {
            tvStatusBadge.setText(status != null ? status : MedicalRecord.STATUS_ACTIVE);
            if (MedicalRecord.STATUS_ACTIVE.equalsIgnoreCase(status)) {
                tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.brand_blue));
            } else if (MedicalRecord.STATUS_RESOLVED.equalsIgnoreCase(status)) {
                tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.success_green));
            }
        }
    }

    private void setupListeners() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnEditRecord != null) {
            btnEditRecord.setOnClickListener(v -> {
                Intent intent = new Intent(MedicalRecordDetailsActivity.this, AddMedicalRecordActivity.class);
                intent.putExtra("recordId", recordId);
                intent.putExtra("conditionName", conditionName);
                intent.putExtra("diagnosisDate", diagnosisDate);
                intent.putExtra("description", description);
                intent.putExtra("notes", notes);
                startActivity(intent);
                finish();
            });
        }

        if (btnDeleteRecord != null) {
            btnDeleteRecord.setOnClickListener(v -> showDeleteConfirmationDialog());
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Medical Record")
                .setMessage("Are you sure you want to delete this medical record? This action cannot be undone.")
                .setPositiveButton("Delete Record", (dialog, which) -> performDeletion())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void performDeletion() {
        showLoading(true);

        repository.deleteMedicalRecord(recordId, new MedicalHistoryRepository.OnMedicalRecordUpdatedListener() {
            @Override
            public void onSuccess() {
                showLoading(false);
                Toast.makeText(MedicalRecordDetailsActivity.this, "Medical record deleted successfully.", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                Toast.makeText(MedicalRecordDetailsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (progressBarRecordDetails != null) {
            progressBarRecordDetails.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (btnDeleteRecord != null) btnDeleteRecord.setEnabled(!isLoading);
        if (btnEditRecord != null) btnEditRecord.setEnabled(!isLoading);
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MedicalRecordDetailsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}