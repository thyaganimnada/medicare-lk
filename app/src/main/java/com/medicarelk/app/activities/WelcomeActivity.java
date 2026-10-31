package com.medicarelk.app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.medicarelk.app.R;
import com.medicarelk.app.utils.ImageUtils;

/**
 * Welcome / Onboarding Screen for MediCare LK.
 * Displays full-screen healthcare background with translucent frosted glass onboarding card.
 * Automatically loads local 'bg_welcome' drawable image if provided by user in res/drawable/.
 */
public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Auto-login session check: If already authenticated, bypass onboarding/login
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_welcome);

        View rootView = findViewById(R.id.rootWelcome);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        ImageView ivFullBgImage = findViewById(R.id.ivFullBgImage);
        if (ivFullBgImage != null) {
            // Check for user-provided local drawable 'bg_welcome' or 'welcome_bg' first
            int resId = getResources().getIdentifier("bg_welcome", "drawable", getPackageName());
            if (resId == 0) {
                resId = getResources().getIdentifier("welcome_bg", "drawable", getPackageName());
            }

            if (resId > 0) {
                ivFullBgImage.setImageResource(resId);
            } else {
                ImageUtils.loadImage(this, "https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?w=1080&auto=format&fit=crop&q=80", ivFullBgImage);
            }
        }

        View btnGetStarted = findViewById(R.id.btnGetStarted);
        if (btnGetStarted != null) {
            btnGetStarted.setOnClickListener(v -> {
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);
            });
        }
    }
}