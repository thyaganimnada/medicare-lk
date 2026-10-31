package com.medicarelk.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.medicarelk.app.R;

/**
 * Help & Support Dashboard Activity matching prototype s-support layout.
 */
public class HelpSupportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_help_support);

        View rootView = findViewById(R.id.rootHelpSupport);
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

        View cardCallHotline = findViewById(R.id.cardCallHotline);
        if (cardCallHotline != null) {
            cardCallHotline.setOnClickListener(v -> {
                try {
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                    dialIntent.setData(Uri.parse("tel:+94112345678"));
                    startActivity(dialIntent);
                } catch (Exception e) {
                    Toast.makeText(HelpSupportActivity.this, "Calling +94 11 234 5678...", Toast.LENGTH_SHORT).show();
                }
            });
        }

        View cardFaq = findViewById(R.id.cardFaq);
        if (cardFaq != null) {
            cardFaq.setOnClickListener(v ->
                    startActivity(new Intent(HelpSupportActivity.this, FaqActivity.class)));
        }

        View cardContactSupport = findViewById(R.id.cardContactSupport);
        if (cardContactSupport != null) {
            cardContactSupport.setOnClickListener(v ->
                    startActivity(new Intent(HelpSupportActivity.this, CustomerSupportActivity.class)));
        }

        View cardMyRequests = findViewById(R.id.cardMyRequests);
        if (cardMyRequests != null) {
            cardMyRequests.setOnClickListener(v ->
                    startActivity(new Intent(HelpSupportActivity.this, SupportRequestsActivity.class)));
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(HelpSupportActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}