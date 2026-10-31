package com.medicarelk.app.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.medicarelk.app.R;
import com.medicarelk.app.databinding.ActivityForgotPasswordBinding;
import com.medicarelk.app.utils.ValidationUtils;

/**
 * Forgot Password Activity for MediCare LK.
 * Sends password reset email via Firebase Authentication and handles system insets.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private FirebaseAuth mAuth;
    private long lastClickTime = 0;
    private static final long CLICK_THRESHOLD_MS = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Apply Window Insets so system status & navigation bars don't overlap UI
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        setupListeners();
        setupTextWatchers();
    }

    private void setupListeners() {
        // Back buttons
        binding.btnBackHeader.setOnClickListener(v -> finish());
        binding.tvBackToLogin.setOnClickListener(v -> finish());

        // Send reset link button
        binding.btnSendResetLink.setOnClickListener(v -> attemptPasswordReset());
    }

    private void setupTextWatchers() {
        binding.etEmail.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilEmail.setError(null);
            }
        });
    }

    private void attemptPasswordReset() {
        // Prevent rapid repeated clicks
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < CLICK_THRESHOLD_MS) {
            return;
        }
        lastClickTime = currentTime;

        binding.tilEmail.setError(null);

        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";

        if (email.isEmpty()) {
            binding.tilEmail.setError(getString(R.string.error_email_required));
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.setError(getString(R.string.error_email_invalid));
            return;
        }

        // Send reset email via Firebase Authentication
        showLoading(true);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this, "Password reset email sent! Check your inbox.", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Failed to send reset email";
                        Toast.makeText(ForgotPasswordActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            binding.progressBarForgotPassword.setVisibility(View.VISIBLE);
            binding.btnSendResetLink.setEnabled(false);
        } else {
            binding.progressBarForgotPassword.setVisibility(View.GONE);
            binding.btnSendResetLink.setEnabled(true);
        }
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void afterTextChanged(Editable s) {}
    }
}