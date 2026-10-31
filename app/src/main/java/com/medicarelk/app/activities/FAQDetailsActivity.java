package com.medicarelk.app.activities;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.medicarelk.app.R;
import com.medicarelk.app.models.FAQ;
import com.medicarelk.app.repositories.FAQRepository;

/**
 * FAQ Details Activity retrieving FAQ by ID from repository and verifying active state.
 */
public class FAQDetailsActivity extends AppCompatActivity {

    private FAQRepository repository;
    private String faqId;

    private TextView tvCategory;
    private TextView tvQuestion;
    private TextView tvAnswer;
    private Button btnContactSupport;
    private ProgressBar progressBarFaqDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Security check
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_faq_details);

        View rootView = findViewById(R.id.rootFaqDetails);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        faqId = getIntent().getStringExtra("faqId");
        repository = new FAQRepository();

        initViews();
        setupListeners();

        loadFaqDetails();
    }

    private void initViews() {
        tvCategory = findViewById(R.id.tvCategory);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvAnswer = findViewById(R.id.tvAnswer);
        btnContactSupport = findViewById(R.id.btnContactSupport);
        progressBarFaqDetails = findViewById(R.id.progressBarFaqDetails);
    }

    private void loadFaqDetails() {
        showLoading(true);

        repository.getFAQById(faqId, new FAQRepository.OnFAQLoadedListener() {
            @Override
            public void onSuccess(FAQ faq) {
                showLoading(false);

                if (!faq.isActive()) {
                    Toast.makeText(FAQDetailsActivity.this, "This FAQ is no longer available.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                if (tvCategory != null) tvCategory.setText(faq.getCategory());
                if (tvQuestion != null) tvQuestion.setText(faq.getQuestion());
                if (tvAnswer != null) tvAnswer.setText(faq.getAnswer());
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                Toast.makeText(FAQDetailsActivity.this, "This FAQ is no longer available.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void setupListeners() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnContactSupport != null) {
            btnContactSupport.setOnClickListener(v -> {
                Intent intent = new Intent(FAQDetailsActivity.this, CustomerSupportActivity.class);
                startActivity(intent);
            });
        }
    }

    private void showLoading(boolean isLoading) {
        if (progressBarFaqDetails != null) {
            progressBarFaqDetails.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(FAQDetailsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}