package com.medicarelk.app.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.medicarelk.app.R;

/**
 * Promotion Details Activity displaying offer details, promo code copy functionality, and booking trigger.
 */
public class PromotionDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_promotion_details);

        View rootView = findViewById(R.id.rootPromotionDetails);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        String title = getIntent().getStringExtra("title");
        String promoCode = getIntent().getStringExtra("promoCode");
        String description = getIntent().getStringExtra("description");

        TextView tvPromoTitle = findViewById(R.id.tvPromoTitle);
        TextView tvPromoCode = findViewById(R.id.tvPromoCode);
        TextView tvDescription = findViewById(R.id.tvDescription);

        if (title != null && tvPromoTitle != null) tvPromoTitle.setText(title);
        if (promoCode != null && tvPromoCode != null) tvPromoCode.setText(promoCode);
        if (description != null && tvDescription != null) tvDescription.setText(description);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        View btnCopyCode = findViewById(R.id.btnCopyCode);
        if (btnCopyCode != null) {
            btnCopyCode.setOnClickListener(v -> {
                String code = tvPromoCode != null ? tvPromoCode.getText().toString() : "HEALTH20";
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Promo Code", code);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(PromotionDetailsActivity.this, "Code copied! 📋", Toast.LENGTH_SHORT).show();
                }
            });
        }

        Button btnBookNow = findViewById(R.id.btnBookNow);
        if (btnBookNow != null) {
            btnBookNow.setOnClickListener(v -> {
                startActivity(new Intent(PromotionDetailsActivity.this, HealthPackagesActivity.class));
            });
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(PromotionDetailsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}