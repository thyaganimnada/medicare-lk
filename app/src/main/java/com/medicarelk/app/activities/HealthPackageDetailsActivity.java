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
 * Health Package Details Activity displaying package specifications, included diagnostics, and locations.
 */
public class HealthPackageDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Security check
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_health_package_details);

        View rootView = findViewById(R.id.rootHealthPackageDetails);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        String name = getIntent().getStringExtra("name");
        String description = getIntent().getStringExtra("description");
        double price = getIntent().getDoubleExtra("price", 18500.0);
        String duration = getIntent().getStringExtra("duration");
        String eligibility = getIntent().getStringExtra("eligibility");
        String locations = getIntent().getStringExtra("locations");
        String includedTests = getIntent().getStringExtra("includedTests");
        String imageUrl = getIntent().getStringExtra("imageUrl");

        ImageView ivPackageImage = findViewById(R.id.ivPackageImage);
        TextView tvPackageName = findViewById(R.id.tvPackageName);
        TextView tvEligibility = findViewById(R.id.tvEligibility);
        TextView tvPrice = findViewById(R.id.tvPrice);
        TextView tvDescription = findViewById(R.id.tvDescription);
        TextView tvIncludedTests = findViewById(R.id.tvIncludedTests);
        TextView tvDuration = findViewById(R.id.tvDuration);
        TextView tvLocations = findViewById(R.id.tvLocations);

        if (ivPackageImage != null && imageUrl != null) {
            ImageUtils.loadRoundedImage(this, imageUrl, ivPackageImage, 16);
        }
        if (name != null && tvPackageName != null) tvPackageName.setText(name);
        if (eligibility != null && tvEligibility != null) tvEligibility.setText(eligibility);
        if (tvPrice != null) tvPrice.setText(String.format(Locale.US, "LKR %,.0f", price));
        if (description != null && tvDescription != null) tvDescription.setText(description);
        if (includedTests != null && tvIncludedTests != null) {
            String bulletText = "• " + includedTests;
            tvIncludedTests.setText(bulletText);
        }
        if (duration != null && tvDuration != null) tvDuration.setText(duration);
        if (locations != null && tvLocations != null) tvLocations.setText(locations);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        Button btnBookPackage = findViewById(R.id.btnBookPackage);
        if (btnBookPackage != null) {
            btnBookPackage.setOnClickListener(v -> {
                Intent intent = new Intent(HealthPackageDetailsActivity.this, PackageBookingActivity.class);
                intent.putExtra("packageId", getIntent().getStringExtra("packageId"));
                intent.putExtra("name", name != null ? name : "Executive Health Check");
                intent.putExtra("price", price > 0 ? price : 18500.0);
                intent.putExtra("duration", duration);
                intent.putExtra("eligibility", eligibility);
                intent.putExtra("locations", locations);
                intent.putExtra("includedTests", includedTests);
                intent.putExtra("imageUrl", imageUrl);
                startActivity(intent);
            });
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(HealthPackageDetailsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
