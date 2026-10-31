package com.medicarelk.app.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
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
import com.medicarelk.app.models.MedicalRecord;
import com.medicarelk.app.repositories.MedicalHistoryRepository;

import java.util.Calendar;
import java.util.Locale;

/**
 * Add / Edit Medical Record Activity.
 */
public class AddMedicalRecordActivity extends AppCompatActivity {

    private MedicalHistoryRepository repository;

    private EditText etConditionName;
    private EditText etDiagnosisDate;
    private Spinner spStatus;
    private EditText etDescription;
    private EditText etNotes;
    private Button btnSaveRecord;
    private ProgressBar progressBarAddRecord;

    private String existingRecordId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Security check
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_add_medical_record);

        View rootView = findViewById(R.id.rootAddMedicalRecord);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        initViews();
        setupStatusSpinner();

        repository = new MedicalHistoryRepository();

        setupListeners();
        checkEditMode();
    }

    private void initViews() {
        etConditionName = findViewById(R.id.etConditionName);
        etDiagnosisDate = findViewById(R.id.etDiagnosisDate);
        spStatus = findViewById(R.id.spStatus);
        etDescription = findViewById(R.id.etDescription);
        etNotes = findViewById(R.id.etNotes);
        btnSaveRecord = findViewById(R.id.btnSaveRecord);
        progressBarAddRecord = findViewById(R.id.progressBarAddRecord);
    }

    private void setupStatusSpinner() {
        String[] statuses = new String[]{MedicalRecord.STATUS_ACTIVE, MedicalRecord.STATUS_RESOLVED, MedicalRecord.STATUS_HISTORICAL};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statuses);
        if (spStatus != null) {
            spStatus.setAdapter(adapter);
        }
    }

    private void checkEditMode() {
        existingRecordId = getIntent().getStringExtra("recordId");
        if (existingRecordId != null && !existingRecordId.isEmpty()) {
            String cond = getIntent().getStringExtra("conditionName");
            String date = getIntent().getStringExtra("diagnosisDate");
            String desc = getIntent().getStringExtra("description");
            String notes = getIntent().getStringExtra("notes");

            if (etConditionName != null && cond != null) etConditionName.setText(cond);
            if (etDiagnosisDate != null && date != null) etDiagnosisDate.setText(date);
            if (etDescription != null && desc != null) etDescription.setText(desc);
            if (etNotes != null && notes != null) etNotes.setText(notes);
        }
    }

    private void setupListeners() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (etDiagnosisDate != null) {
            etDiagnosisDate.setOnClickListener(v -> showDatePicker());
        }

        if (btnSaveRecord != null) {
            btnSaveRecord.setOnClickListener(v -> saveRecord());
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    if (etDiagnosisDate != null) etDiagnosisDate.setText(formattedDate);
                }, year, month, day);

        // Block future diagnosis dates
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void saveRecord() {
        String cond = etConditionName != null ? etConditionName.getText().toString().trim() : "";
        String date = etDiagnosisDate != null ? etDiagnosisDate.getText().toString().trim() : "";
        String status = spStatus != null && spStatus.getSelectedItem() != null ? spStatus.getSelectedItem().toString() : MedicalRecord.STATUS_ACTIVE;
        String desc = etDescription != null ? etDescription.getText().toString().trim() : "";
        String notes = etNotes != null ? etNotes.getText().toString().trim() : "";

        if (cond.isEmpty()) {
            if (etConditionName != null) etConditionName.setError("Condition name is required");
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            redirectToLogin();
            return;
        }

        showLoading(true);

        MedicalRecord record = new MedicalRecord(
                existingRecordId != null ? existingRecordId : "",
                user.getUid(),
                cond,
                desc,
                date,
                status,
                notes
        );

        if (existingRecordId != null && !existingRecordId.isEmpty()) {
            repository.updateMedicalRecord(record, new MedicalHistoryRepository.OnMedicalRecordUpdatedListener() {
                @Override
                public void onSuccess() {
                    showLoading(false);
                    Toast.makeText(AddMedicalRecordActivity.this, "Record updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String errorMessage) {
                    showLoading(false);
                    Toast.makeText(AddMedicalRecordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            repository.addMedicalRecord(record, new MedicalHistoryRepository.OnMedicalRecordUpdatedListener() {
                @Override
                public void onSuccess() {
                    showLoading(false);
                    Toast.makeText(AddMedicalRecordActivity.this, "Record saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String errorMessage) {
                    showLoading(false);
                    Toast.makeText(AddMedicalRecordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void showLoading(boolean isLoading) {
        if (progressBarAddRecord != null) {
            progressBarAddRecord.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (btnSaveRecord != null) {
            btnSaveRecord.setEnabled(!isLoading);
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(AddMedicalRecordActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}