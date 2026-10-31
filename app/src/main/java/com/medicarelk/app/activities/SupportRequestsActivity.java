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

import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.medicarelk.app.R;
import com.medicarelk.app.adapters.SupportRequestAdapter;
import com.medicarelk.app.models.SupportRequest;
import com.medicarelk.app.repositories.SupportRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * My Support Requests Activity displaying patient's submitted support tickets.
 */
public class SupportRequestsActivity extends AppCompatActivity {

    private SupportRepository repository;
    private SupportRequestAdapter adapter;

    private List<SupportRequest> allRequests = new ArrayList<>();
    private final List<SupportRequest> filteredRequests = new ArrayList<>();

    private RecyclerView rvSupportRequests;
    private ProgressBar progressBarRequests;
    private View layoutEmptyState;
    private ChipGroup chipGroupStatus;

    private String selectedStatus = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Security check
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_support_requests);

        View rootView = findViewById(R.id.rootSupportRequests);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        initViews();
        repository = new SupportRepository();

        setupRecyclerView();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            loadSupportRequests(currentUser.getUid());
        }
    }

    private void initViews() {
        rvSupportRequests = findViewById(R.id.rvSupportRequests);
        progressBarRequests = findViewById(R.id.progressBarRequests);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        chipGroupStatus = findViewById(R.id.chipGroupStatus);
    }

    private void setupRecyclerView() {
        adapter = new SupportRequestAdapter(filteredRequests, request -> {
            Intent intent = new Intent(SupportRequestsActivity.this, SupportRequestDetailsActivity.class);
            intent.putExtra("requestId", request.getRequestId());
            startActivity(intent);
        });

        if (rvSupportRequests != null) {
            rvSupportRequests.setAdapter(adapter);
        }
    }

    private void loadSupportRequests(String userId) {
        showLoading(true);

        repository.getPatientSupportRequests(userId, new SupportRepository.OnSupportRequestsLoadedListener() {
            @Override
            public void onSuccess(List<SupportRequest> requests) {
                showLoading(false);
                allRequests = requests != null ? requests : new ArrayList<>();
                applyFilters();
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                Toast.makeText(SupportRequestsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (chipGroupStatus != null) {
            chipGroupStatus.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.chipStatusOpen) {
                    selectedStatus = SupportRequest.STATUS_OPEN;
                } else if (checkedId == R.id.chipStatusProgress) {
                    selectedStatus = SupportRequest.STATUS_IN_PROGRESS;
                } else if (checkedId == R.id.chipStatusResolved) {
                    selectedStatus = SupportRequest.STATUS_RESOLVED;
                } else if (checkedId == R.id.chipStatusClosed) {
                    selectedStatus = SupportRequest.STATUS_CLOSED;
                } else {
                    selectedStatus = "ALL";
                }
                applyFilters();
            });
        }
    }

    private void applyFilters() {
        filteredRequests.clear();

        for (SupportRequest req : allRequests) {
            boolean matchesStatus = selectedStatus.equalsIgnoreCase("ALL") ||
                    req.getStatus().equalsIgnoreCase(selectedStatus);

            if (matchesStatus) {
                filteredRequests.add(req);
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        if (filteredRequests.isEmpty()) {
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
            if (rvSupportRequests != null) rvSupportRequests.setVisibility(View.GONE);
        } else {
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
            if (rvSupportRequests != null) rvSupportRequests.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean isLoading) {
        if (progressBarRequests != null) {
            progressBarRequests.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(SupportRequestsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}