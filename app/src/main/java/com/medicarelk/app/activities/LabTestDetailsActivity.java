package com.medicarelk.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.medicarelk.app.R;
import com.medicarelk.app.utils.ImageUtils;

import java.util.Locale;

/**
 * Lab Test Details Activity displaying test specifications, preparation instructions, and locations.
 */
public class LabTestDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Security check
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_lab_test_details);

        View rootView = findViewById(R.id.rootLabTestDetails);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        String name = getIntent().getStringExtra("name");
        String category = getIntent().getStringExtra("category");
        String description = getIntent().getStringExtra("description");
        double price = getIntent().getDoubleExtra("price", 1800.0);
        String resultTime = getIntent().getStringExtra("resultTime");
        String preparation = getIntent().getStringExtra("preparation");
        String locations = getIntent().getStringExtra("locations");
        String imageUrl = getIntent().getStringExtra("imageUrl");

        ImageView ivTestImage = findViewById(R.id.ivTestImage);
        TextView tvTestName = findViewById(R.id.tvTestName);
        TextView tvCategory = findViewById(R.id.tvCategory);
        TextView tvPrice = findViewById(R.id.tvPrice);
        TextView tvDescription = findViewById(R.id.tvDescription);
        TextView tvResultTime = findViewById(R.id.tvResultTime);
        TextView tvPreparation = findViewById(R.id.tvPreparation);
        TextView tvLocations = findViewById(R.id.tvLocations);

        if (ivTestImage != null && imageUrl != null) {
            ImageUtils.loadRoundedImage(this, imageUrl, ivTestImage, 16);
        }
        if (name != null && tvTestName != null) tvTestName.setText(name);
        if (category != null && tvCategory != null) tvCategory.setText(category);
        if (tvPrice != null) tvPrice.setText(String.format(Locale.US, "LKR %,.0f", price));
        if (description != null && tvDescription != null) tvDescription.setText(description);
        if (resultTime != null && tvResultTime != null) tvResultTime.setText(resultTime);
        if (preparation != null && tvPreparation != null) tvPreparation.setText(preparation);
        if (locations != null && tvLocations != null) tvLocations.setText(locations);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        Button btnBookLabTest = findViewById(R.id.btnBookLabTest);
        if (btnBookLabTest != null) {
            btnBookLabTest.setOnClickListener(v -> {
                Intent intent = new Intent(LabTestDetailsActivity.this, LabTestBookingActivity.class);
                intent.putExtra("labTestId", getIntent().getStringExtra("labTestId"));
                intent.putExtra("name", name != null ? name : "Full Blood Count (FBC)");
                intent.putExtra("category", category != null ? category : "Blood Tests");
                intent.putExtra("price", price > 0 ? price : 1800.0);
                intent.putExtra("preparation", preparation);
                intent.putExtra("locations", locations);
                intent.putExtra("imageUrl", imageUrl);
                startActivity(intent);
            });
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(LabTestDetailsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
