package com.medicarelk.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.medicarelk.app.R;
import com.medicarelk.app.adapters.MedicalHistoryAdapter;
import com.medicarelk.app.models.MedicalRecord;
import com.medicarelk.app.repositories.MedicalHistoryRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Medical History Activity displaying personal stored medical records with strict patient privacy.
 */
public class MedicalHistoryActivity extends AppCompatActivity {

    private MedicalHistoryRepository repository;
    private MedicalHistoryAdapter adapter;

    private List<MedicalRecord> recordList = new ArrayList<>();

    private RecyclerView rvMedicalHistory;
    private ProgressBar progressBarMedHistory;
    private View layoutEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Security check
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_medical_history);

        View rootView = findViewById(R.id.rootMedicalHistory);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        initViews();
        repository = new MedicalHistoryRepository();

        setupRecyclerView();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            loadMedicalRecords(currentUser.getUid());
        }
    }

    private void initViews() {
        rvMedicalHistory = findViewById(R.id.rvMedicalHistory);
        progressBarMedHistory = findViewById(R.id.progressBarMedHistory);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
    }

    private void setupRecyclerView() {
        adapter = new MedicalHistoryAdapter(recordList, record -> {
            Intent intent = new Intent(MedicalHistoryActivity.this, MedicalRecordDetailsActivity.class);
            intent.putExtra("recordId", record.getRecordId());
            intent.putExtra("conditionName", record.getConditionName());
            intent.putExtra("diagnosisDate", record.getDiagnosisDate());
            intent.putExtra("status", record.getStatus());
            intent.putExtra("description", record.getDescription());
            intent.putExtra("notes", record.getNotes());
            startActivity(intent);
        });

        if (rvMedicalHistory != null) {
            rvMedicalHistory.setAdapter(adapter);
        }
    }

    private void loadMedicalRecords(String userId) {
        showLoading(true);

        repository.getMedicalRecords(userId, new MedicalHistoryRepository.OnMedicalRecordsLoadedListener() {
            @Override
            public void onSuccess(List<MedicalRecord> records) {
                showLoading(false);
                recordList.clear();
                if (records != null) {
                    recordList.addAll(records);
                }

                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }

                if (recordList.isEmpty()) {
                    if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
                    if (rvMedicalHistory != null) rvMedicalHistory.setVisibility(View.GONE);
                } else {
                    if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
                    if (rvMedicalHistory != null) rvMedicalHistory.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                Toast.makeText(MedicalHistoryActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        View btnAddRecord = findViewById(R.id.btnAddRecord);
        if (btnAddRecord != null) {
            btnAddRecord.setOnClickListener(v ->
                    startActivity(new Intent(MedicalHistoryActivity.this, AddMedicalRecordActivity.class)));
        }
    }

    private void showLoading(boolean isLoading) {
        if (progressBarMedHistory != null) {
            progressBarMedHistory.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MedicalHistoryActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}