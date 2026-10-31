package com.medicarelk.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.medicarelk.app.R;
import com.medicarelk.app.utils.ValidationUtils;

/**
 * Change Password Activity enforcing re-authentication and secure Firebase password update.
 */
public class ChangePasswordActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private EditText etCurrentPassword;
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private Button btnUpdatePassword;
    private ProgressBar progressBarChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_change_password);

        View rootView = findViewById(R.id.rootChangePassword);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);
        progressBarChangePassword = findViewById(R.id.progressBarChangePassword);
    }

    private void setupListeners() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnUpdatePassword != null) {
            btnUpdatePassword.setOnClickListener(v -> performPasswordChange());
        }
    }

    private void performPasswordChange() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            redirectToLogin();
            return;
        }

        String currentPass = etCurrentPassword != null ? etCurrentPassword.getText().toString().trim() : "";
        String newPass = etNewPassword != null ? etNewPassword.getText().toString().trim() : "";
        String confirmPass = etConfirmPassword != null ? etConfirmPassword.getText().toString().trim() : "";

        if (currentPass.isEmpty()) {
            if (etCurrentPassword != null) etCurrentPassword.setError("Current password is required");
            return;
        }

        if (!ValidationUtils.isValidPassword(newPass)) {
            if (etNewPassword != null) etNewPassword.setError("Password must be at least 8 characters long and contain uppercase, lowercase, and numbers");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            if (etConfirmPassword != null) etConfirmPassword.setError("New passwords do not match");
            return;
        }

        showLoading(true);

        // Re-authenticate user before changing password
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPass);
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Re-authentication success: update password
                user.updatePassword(newPass).addOnCompleteListener(updateTask -> {
                    showLoading(false);
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(ChangePasswordActivity.this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String err = updateTask.getException() != null ? updateTask.getException().getMessage() : "Password update failed.";
                        Toast.makeText(ChangePasswordActivity.this, err, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                showLoading(false);
                if (etCurrentPassword != null) etCurrentPassword.setError("Incorrect current password");
                Toast.makeText(ChangePasswordActivity.this, "Current password verification failed.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (progressBarChangePassword != null) {
            progressBarChangePassword.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (btnUpdatePassword != null) {
            btnUpdatePassword.setEnabled(!isLoading);
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}