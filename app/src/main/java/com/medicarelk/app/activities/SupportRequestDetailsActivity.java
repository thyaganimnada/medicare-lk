package com.medicarelk.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.medicarelk.app.R;
import com.medicarelk.app.models.SupportRequest;
import com.medicarelk.app.repositories.SupportRepository;

/**
 * Support Request Details Activity displaying ticket status and support staff responses.
 */
public class SupportRequestDetailsActivity extends AppCompatActivity {

    private SupportRepository repository;
    private String requestId;

    private TextView tvRefId;
    private TextView tvStatusBadge;
    private TextView tvSubject;
    private TextView tvCategory;
    private TextView tvCreatedDate;
    private TextView tvDescription;
    private TextView tvNoReplies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Security check
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_support_request_details);

        View rootView = findViewById(R.id.rootRequestDetails);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        requestId = getIntent().getStringExtra("requestId");
        repository = new SupportRepository();

        initViews();
        setupListeners();

        loadRequestDetails();
    }

    private void initViews() {
        tvRefId = findViewById(R.id.tvRefId);
        tvStatusBadge = findViewById(R.id.tvStatusBadge);
        tvSubject = findViewById(R.id.tvSubject);
        tvCategory = findViewById(R.id.tvCategory);
        tvCreatedDate = findViewById(R.id.tvCreatedDate);
        tvDescription = findViewById(R.id.tvDescription);
        tvNoReplies = findViewById(R.id.tvNoReplies);
    }

    private void loadRequestDetails() {
        repository.getSupportRequestById(requestId, new SupportRepository.OnSupportRequestLoadedListener() {
            @Override
            public void onSuccess(SupportRequest request) {
                bindRequestData(request);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(SupportRequestDetailsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void bindRequestData(SupportRequest request) {
        String refText = "#" + (request.getRequestId().length() > 8 ? request.getRequestId().substring(request.getRequestId().length() - 8) : request.getRequestId());
        if (tvRefId != null) tvRefId.setText("Ticket " + refText);
        if (tvSubject != null) tvSubject.setText(request.getSubject());
        if (tvCategory != null) tvCategory.setText("Category: " + request.getCategory());

        CharSequence dateStr = DateFormat.format("yyyy-MM-dd hh:mm a", request.getCreatedAt());
        if (tvCreatedDate != null) tvCreatedDate.setText("Submitted on: " + dateStr);
        if (tvDescription != null) tvDescription.setText(request.getDescription());

        String status = request.getFormattedStatus();
        if (tvStatusBadge != null) {
            tvStatusBadge.setText(status);
            if (SupportRequest.STATUS_OPEN.equalsIgnoreCase(request.getStatus())) {
                tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.brand_blue));
            } else if (SupportRequest.STATUS_IN_PROGRESS.equalsIgnoreCase(request.getStatus())) {
                tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.primary_teal_dark));
            } else if (SupportRequest.STATUS_RESOLVED.equalsIgnoreCase(request.getStatus())) {
                tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.success_green));
            }
        }
    }

    private void setupListeners() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    private void redirectToLogin() {
        Intent intent = new Intent(SupportRequestDetailsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}