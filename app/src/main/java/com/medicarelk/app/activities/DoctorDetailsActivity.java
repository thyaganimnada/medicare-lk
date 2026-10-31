package com.medicarelk.app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.medicarelk.app.R;
import com.medicarelk.app.utils.ImageUtils;

import java.util.Locale;

/**
 * Doctor Details Activity featuring modern structured Material cards for credentials,
 * qualifications, hospital practice, and consultation details.
 */
public class DoctorDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_doctor_details);

        View rootView = findViewById(R.id.rootDoctorDetails);
        View toolbarDoctorDetails = findViewById(R.id.toolbarDoctorDetails);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                if (toolbarDoctorDetails != null && systemBars.top > 0) {
                    int basePadding = (int) (72 * getResources().getDisplayMetrics().density);
                    int topPadding = Math.max(systemBars.top + (int) (16 * getResources().getDisplayMetrics().density), basePadding);
                    toolbarDoctorDetails.setPadding(
                            toolbarDoctorDetails.getPaddingLeft(),
                            topPadding,
                            toolbarDoctorDetails.getPaddingRight(),
                            toolbarDoctorDetails.getPaddingBottom()
                    );
                }
                v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        String doctorId = getIntent().getStringExtra("doctorId");
        String name = getIntent().getStringExtra("doctorName");
        String specialty = getIntent().getStringExtra("doctorSpecialty");
        String bio = getIntent().getStringExtra("bio");
        String hospital = getIntent().getStringExtra("hospitalName");
        String location = getIntent().getStringExtra("location");
        String address = getIntent().getStringExtra("address");
        double fee = getIntent().getDoubleExtra("consultationFee", 3500.0);
        String qualification = getIntent().getStringExtra("qualification");
        int exp = getIntent().getIntExtra("experienceYears", 5);
        String languages = getIntent().getStringExtra("languages");
        String profileImageUrl = getIntent().getStringExtra("profileImageUrl");

        ImageView ivDoctorImage = findViewById(R.id.ivDoctorImage);
        TextView tvDoctorName = findViewById(R.id.tvDoctorName);
        TextView tvDoctorSpecialty = findViewById(R.id.tvDoctorSpecialty);
        TextView tvHospitalLocation = findViewById(R.id.tvHospitalLocation);
        TextView tvExperienceStat = findViewById(R.id.tvExperienceStat);
        TextView tvDoctorBio = findViewById(R.id.tvDoctorBio);
        TextView tvQualifications = findViewById(R.id.tvQualifications);
        TextView tvHospitalAddress = findViewById(R.id.tvHospitalAddress);
        TextView tvConsultationFee = findViewById(R.id.tvConsultationFee);
        TextView tvLanguages = findViewById(R.id.tvLanguages);

        if (ivDoctorImage != null && profileImageUrl != null && !profileImageUrl.isEmpty()) {
            ImageUtils.loadRoundedImage(this, profileImageUrl, ivDoctorImage, 16);
        }

        if (name != null && tvDoctorName != null) tvDoctorName.setText(name);
        if (specialty != null && tvDoctorSpecialty != null) tvDoctorSpecialty.setText(specialty);

        if (hospital != null && location != null && tvHospitalLocation != null) {
            tvHospitalLocation.setText(hospital + " • " + location);
        }

        if (tvExperienceStat != null) {
            tvExperienceStat.setText(exp + " Yrs+");
        }

        if (bio != null && tvDoctorBio != null) {
            tvDoctorBio.setText(bio);
        }

        if (qualification != null && tvQualifications != null) {
            tvQualifications.setText(qualification);
        }

        if (tvHospitalAddress != null) {
            String fullAddr = hospital != null ? hospital : "MediCare Hospital";
            if (address != null && !address.isEmpty()) {
                fullAddr += ", " + address;
            } else if (location != null && !location.isEmpty()) {
                fullAddr += ", " + location;
            }
            tvHospitalAddress.setText(fullAddr);
        }

        if (tvConsultationFee != null) {
            tvConsultationFee.setText(String.format(Locale.US, "LKR %,.0f per consultation", fee > 0 ? fee : 3500.0));
        }

        if (languages != null && tvLanguages != null) {
            tvLanguages.setText(languages);
        }

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Navigate directly to BookingActivity
        View btnBookAppointment = findViewById(R.id.btnBookAppointment);
        if (btnBookAppointment != null) {
            btnBookAppointment.setOnClickListener(v -> {
                Intent intent = new Intent(DoctorDetailsActivity.this, BookingActivity.class);
                String docId = doctorId != null && !doctorId.trim().isEmpty() ? doctorId : "doc101";
                intent.putExtra("doctorId", docId);
                intent.putExtra("doctorName", name != null ? name : "Dr. Sarah Perera");
                intent.putExtra("doctorSpecialty", specialty != null ? specialty : "General Practitioner");
                intent.putExtra("hospitalName", hospital != null ? hospital : "MediCare Hospital");
                intent.putExtra("location", location != null ? location : "Colombo");
                intent.putExtra("consultationFee", fee > 0 ? fee : 3500.0);
                intent.putExtra("profileImageUrl", profileImageUrl);
                startActivity(intent);
            });
        }
    }
}