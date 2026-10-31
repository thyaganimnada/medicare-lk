package com.medicarelk.app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.medicarelk.app.R;
import com.medicarelk.app.utils.ValidationUtils;

/**
 * Login Activity for MediCare LK.
 * Handles user authentication via Firebase Auth and redirects to HomeActivity.
 */
public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private EditText etEmail;
    private EditText etPassword;
    private CheckBox cbRememberMe;
    private TextView tvForgotPassword;
    private Button btnLogin;
    private TextView tvSignUpLink;
    private ProgressBar progressBarLogin;

    private long lastClickTime = 0;
    private static final long CLICK_THRESHOLD_MS = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_login);

        View rootView = findViewById(R.id.rootLogin);
        if (rootView == null) {
            rootView = findViewById(android.R.id.content);
        }
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUpLink = findViewById(R.id.tvSignUpLink);
        progressBarLogin = findViewById(R.id.progressBarLogin);
    }

    private void setupListeners() {
        if (tvSignUpLink != null) {
            tvSignUpLink.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            });
        }

        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            });
        }

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> attemptLogin());
        }
    }

    private void attemptLogin() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < CLICK_THRESHOLD_MS) {
            return;
        }
        lastClickTime = currentTime;

        String email = etEmail != null && etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword != null && etPassword.getText() != null ? etPassword.getText().toString() : "";

        // Email validation
        if (email.isEmpty()) {
            if (etEmail != null) etEmail.setError(getString(R.string.error_email_required));
            return;
        } else if (!ValidationUtils.isValidEmail(email)) {
            if (etEmail != null) etEmail.setError(getString(R.string.error_email_invalid));
            return;
        }

        // Password validation
        if (password.isEmpty()) {
            if (etPassword != null) etPassword.setError(getString(R.string.error_password_required));
            return;
        } else if (!ValidationUtils.isValidLoginPassword(password)) {
            if (etPassword != null) etPassword.setError(getString(R.string.error_password_min_length));
            return;
        }

        showLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Authentication failed";
                        Toast.makeText(LoginActivity.this, "Login failed: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showLoading(boolean isLoading) {
        if (progressBarLogin != null) {
            progressBarLogin.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (btnLogin != null) {
            btnLogin.setEnabled(!isLoading);
        }
    }
}