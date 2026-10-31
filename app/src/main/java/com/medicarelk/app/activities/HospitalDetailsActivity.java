package com.medicarelk.app.activities;

import android.content.Intent;
import android.net.Uri;
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
 * Hospital Details Activity displaying full hospital address, contact details, services, and action triggers.
 */
public class HospitalDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Security check
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_hospital_details);

        View rootView = findViewById(R.id.rootHospitalDetails);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        String name = getIntent().getStringExtra("name");
        String address = getIntent().getStringExtra("address");
        String city = getIntent().getStringExtra("city");
        String location = getIntent().getStringExtra("location");
        String contactNumber = getIntent().getStringExtra("contactNumber");
        String openingHours = getIntent().getStringExtra("openingHours");
        boolean emergencyAvailable = getIntent().getBooleanExtra("emergencyAvailable", true);
        String services = getIntent().getStringExtra("services");

        TextView tvHospitalName = findViewById(R.id.tvHospitalName);
        TextView tvLocation = findViewById(R.id.tvLocation);
        TextView tvEmergencyBadge = findViewById(R.id.tvEmergencyBadge);
        TextView tvAddress = findViewById(R.id.tvAddress);
        TextView tvContactNumber = findViewById(R.id.tvContactNumber);
        TextView tvOpeningHours = findViewById(R.id.tvOpeningHours);
        TextView tvServicesList = findViewById(R.id.tvServicesList);

        if (name != null && tvHospitalName != null) tvHospitalName.setText(name);

        String locText = (city != null ? city : "Colombo") + ", " + (location != null ? location : "Western Province");
        if (tvLocation != null) tvLocation.setText(locText);

        if (tvEmergencyBadge != null) {
            tvEmergencyBadge.setVisibility(emergencyAvailable ? View.VISIBLE : View.GONE);
        }

        if (address != null && tvAddress != null) tvAddress.setText(address);
        if (contactNumber != null && tvContactNumber != null) tvContactNumber.setText(contactNumber);
        if (openingHours != null && tvOpeningHours != null) tvOpeningHours.setText(openingHours);
        if (services != null && tvServicesList != null) tvServicesList.setText(services);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        Button btnViewDoctors = findViewById(R.id.btnViewDoctors);
        if (btnViewDoctors != null) {
            btnViewDoctors.setOnClickListener(v -> {
                Intent intent = new Intent(HospitalDetailsActivity.this, DoctorsActivity.class);
                intent.putExtra("hospitalId", getIntent().getStringExtra("hospitalId"));
                intent.putExtra("hospitalName", name);
                intent.putExtra("city", city);
                startActivity(intent);
            });
        }

        Button btnViewLabTests = findViewById(R.id.btnViewLabTests);
        if (btnViewLabTests != null) {
            btnViewLabTests.setOnClickListener(v -> {
                Intent intent = new Intent(HospitalDetailsActivity.this, LabTestsActivity.class);
                intent.putExtra("hospitalId", getIntent().getStringExtra("hospitalId"));
                intent.putExtra("hospitalName", name);
                intent.putExtra("city", city);
                startActivity(intent);
            });
        }

        Button btnContactHospital = findViewById(R.id.btnContactHospital);
        if (btnContactHospital != null) {
            btnContactHospital.setOnClickListener(v -> {
                String phone = contactNumber != null ? contactNumber : "+94112688888";
                try {
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                    dialIntent.setData(Uri.parse("tel:" + phone.replaceAll("[^0-9+]", "")));
                    startActivity(dialIntent);
                } catch (Exception e) {
                    Toast.makeText(HospitalDetailsActivity.this, "Unable to launch dialer.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(HospitalDetailsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}