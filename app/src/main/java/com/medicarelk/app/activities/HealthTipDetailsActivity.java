package com.medicarelk.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.medicarelk.app.R;

/**
 * Health Tip Details Activity displaying health article content and prominent Medical Disclaimer.
 */
public class HealthTipDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Security check
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_health_tip_details);

        View rootView = findViewById(R.id.rootHealthTipDetails);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        String title = getIntent().getStringExtra("title");
        String category = getIntent().getStringExtra("category");
        String author = getIntent().getStringExtra("author");
        String content = getIntent().getStringExtra("content");

        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvCategory = findViewById(R.id.tvCategory);
        TextView tvAuthor = findViewById(R.id.tvAuthor);
        TextView tvContent = findViewById(R.id.tvContent);

        if (title != null && tvTitle != null) tvTitle.setText(title);
        if (category != null && tvCategory != null) tvCategory.setText(category);
        if (author != null && tvAuthor != null) tvAuthor.setText("By " + author);
        if (content != null && tvContent != null) tvContent.setText(content);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    private void redirectToLogin() {
        Intent intent = new Intent(HealthTipDetailsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}