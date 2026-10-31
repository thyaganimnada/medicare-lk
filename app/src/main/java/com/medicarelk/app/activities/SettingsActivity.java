package com.medicarelk.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.medicarelk.app.R;

/**
 * Settings & Account Security Activity.
 */
public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_settings);

        View rootView = findViewById(R.id.rootSettings);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        setupListeners();
    }

    private void setupListeners() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        TextView optEditProfile = findViewById(R.id.optEditProfile);
        if (optEditProfile != null) {
            optEditProfile.setOnClickListener(v ->
                    startActivity(new Intent(SettingsActivity.this, EditProfileActivity.class)));
        }

        TextView optChangePassword = findViewById(R.id.optChangePassword);
        if (optChangePassword != null) {
            optChangePassword.setOnClickListener(v ->
                    startActivity(new Intent(SettingsActivity.this, ChangePasswordActivity.class)));
        }

        TextView optPrivacyPolicy = findViewById(R.id.optPrivacyPolicy);
        if (optPrivacyPolicy != null) {
            optPrivacyPolicy.setOnClickListener(v ->
                    startActivity(new Intent(SettingsActivity.this, PrivacyPolicyActivity.class)));
        }

        TextView optDeleteAccount = findViewById(R.id.optDeleteAccount);
        if (optDeleteAccount != null) {
            optDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
        }

        TextView optFaq = findViewById(R.id.optFaq);
        if (optFaq != null) {
            optFaq.setOnClickListener(v ->
                    startActivity(new Intent(SettingsActivity.this, FaqActivity.class)));
        }

        TextView optAbout = findViewById(R.id.optAbout);
        if (optAbout != null) {
            optAbout.setOnClickListener(v ->
                    startActivity(new Intent(SettingsActivity.this, AboutActivity.class)));
        }
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete MediCare LK Account?")
                .setMessage("This action permanently removes your profile and personal information. Are you sure you want to proceed?")
                .setPositiveButton("Delete Account", (dialog, which) -> performAccountDeletion())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void performAccountDeletion() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            redirectToLogin();
            return;
        }

        String uid = user.getUid();

        // 1. Remove patient data from database
        try {
            FirebaseDatabase.getInstance().getReference("Users").child(uid).removeValue();
        } catch (Exception ignored) {}

        // 2. Delete Firebase Auth User Account
        user.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(SettingsActivity.this, "Your account has been deleted.", Toast.LENGTH_LONG).show();
                mAuth.signOut();
                redirectToLogin();
            } else {
                Toast.makeText(SettingsActivity.this, "Re-authentication required. Please log in again to delete your account.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}