package com.medicarelk.app.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.medicarelk.app.R;
import com.medicarelk.app.models.Patient;
import com.medicarelk.app.repositories.ProfileRepository;
import com.medicarelk.app.utils.ImageUtils;
import com.medicarelk.app.utils.ValidationUtils;

import java.util.Calendar;
import java.util.Locale;

/**
 * Edit Profile Activity allowing patients to update personal details, photo, Blood Group, and Emergency Contact.
 */
public class EditProfileActivity extends AppCompatActivity {

    private ProfileRepository profileRepository;
    private Patient currentPatient;

    private ImageView ivProfilePhoto;
    private View btnChangePhoto;
    private EditText etFullName;
    private EditText etEmail;
    private EditText etPhone;
    private Spinner spBloodGroup;
    private EditText etWeight;
    private EditText etDob;
    private Spinner spGender;
    private EditText etAddress;
    private EditText etEmergencyName;
    private EditText etEmergencyRel;
    private EditText etEmergencyPhone;
    private Button btnSaveProfile;
    private ProgressBar progressBarEditProfile;

    private String pendingImageBase64 = null;
    private final String[] bloodGroups = new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};

    private final ActivityResultLauncher<String> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    onPhotoSelected(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_edit_profile);

        View rootView = findViewById(R.id.rootEditProfile);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        initViews();
        setupGenderSpinner();
        setupBloodGroupSpinner();

        profileRepository = new ProfileRepository();

        setupListeners();
        loadProfile(currentUser.getUid());
    }

    private void initViews() {
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        spBloodGroup = findViewById(R.id.spBloodGroup);
        etWeight = findViewById(R.id.etWeight);
        etDob = findViewById(R.id.etDob);
        spGender = findViewById(R.id.spGender);
        etAddress = findViewById(R.id.etAddress);
        etEmergencyName = findViewById(R.id.etEmergencyName);
        etEmergencyRel = findViewById(R.id.etEmergencyRel);
        etEmergencyPhone = findViewById(R.id.etEmergencyPhone);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        progressBarEditProfile = findViewById(R.id.progressBarEditProfile);
    }

    private void setupGenderSpinner() {
        String[] genders = new String[]{"Male", "Female", "Other", "Prefer not to say"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_selected, genders);
        adapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        if (spGender != null) {
            spGender.setAdapter(adapter);
        }
    }

    private void setupBloodGroupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_selected, bloodGroups);
        adapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        if (spBloodGroup != null) {
            spBloodGroup.setAdapter(adapter);
        }
    }

    private void loadProfile(String uid) {
        showLoading(true);

        profileRepository.getProfile(uid, new ProfileRepository.OnProfileLoadedListener() {
            @Override
            public void onSuccess(Patient patient) {
                showLoading(false);
                currentPatient = patient;

                if (etFullName != null) etFullName.setText(patient.getFullName());
                if (etEmail != null) etEmail.setText(patient.getEmail());
                if (etPhone != null) etPhone.setText(patient.getPhone());
                if (etWeight != null) etWeight.setText(patient.getWeight());
                if (etDob != null) etDob.setText(patient.getDateOfBirth());
                if (etAddress != null) etAddress.setText(patient.getAddress());
                if (etEmergencyName != null) etEmergencyName.setText(patient.getEmergencyContactName());
                if (etEmergencyRel != null) etEmergencyRel.setText(patient.getEmergencyContactRelationship());
                if (etEmergencyPhone != null) etEmergencyPhone.setText(patient.getEmergencyContactPhone());

                if (ivProfilePhoto != null) {
                    ImageUtils.loadProfileImage(EditProfileActivity.this, patient.getProfileImageUrl(), ivProfilePhoto);
                }

                // Set Blood Group selection
                if (spBloodGroup != null && patient.getBloodGroup() != null) {
                    for (int i = 0; i < bloodGroups.length; i++) {
                        if (bloodGroups[i].equalsIgnoreCase(patient.getBloodGroup().trim())) {
                            spBloodGroup.setSelection(i);
                            break;
                        }
                    }
                }

                // Set Gender selection
                if (spGender != null && patient.getGender() != null) {
                    String[] genders = new String[]{"Male", "Female", "Other", "Prefer not to say"};
                    for (int i = 0; i < genders.length; i++) {
                        if (genders[i].equalsIgnoreCase(patient.getGender().trim())) {
                            spGender.setSelection(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                Toast.makeText(EditProfileActivity.this, "Unable to load profile data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnChangePhoto != null) {
            btnChangePhoto.setOnClickListener(v -> photoPickerLauncher.launch("image/*"));
        }

        if (etDob != null) {
            etDob.setOnClickListener(v -> showDatePicker());
        }

        if (btnSaveProfile != null) {
            btnSaveProfile.setOnClickListener(v -> saveProfileChanges());
        }
    }

    private void onPhotoSelected(Uri imageUri) {
        String base64Image = ImageUtils.convertUriToBase64(this, imageUri);
        if (base64Image != null) {
            pendingImageBase64 = base64Image;
            if (ivProfilePhoto != null) {
                ImageUtils.loadProfileImage(this, base64Image, ivProfilePhoto);
            }
            Toast.makeText(this, "Photo updated! Tap Save Changes to keep.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Unable to process selected image.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR) - 25;
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    if (etDob != null) etDob.setText(formattedDate);
                }, year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void saveProfileChanges() {
        if (currentPatient == null) return;

        String name = etFullName != null ? etFullName.getText().toString().trim() : "";
        String email = etEmail != null ? etEmail.getText().toString().trim() : "";
        String phone = etPhone != null ? etPhone.getText().toString().trim() : "";
        String blood = spBloodGroup != null && spBloodGroup.getSelectedItem() != null ? spBloodGroup.getSelectedItem().toString() : "B+";
        String weight = etWeight != null ? etWeight.getText().toString().trim() : "";
        String dob = etDob != null ? etDob.getText().toString().trim() : "";
        String gender = spGender != null && spGender.getSelectedItem() != null ? spGender.getSelectedItem().toString() : "Male";
        String address = etAddress != null ? etAddress.getText().toString().trim() : "";
        String emName = etEmergencyName != null ? etEmergencyName.getText().toString().trim() : "";
        String emRel = etEmergencyRel != null ? etEmergencyRel.getText().toString().trim() : "";
        String emPhone = etEmergencyPhone != null ? etEmergencyPhone.getText().toString().trim() : "";

        if (name.isEmpty()) {
            if (etFullName != null) etFullName.setError("Full name is required");
            return;
        }

        if (!phone.isEmpty() && !ValidationUtils.isValidSriLankanPhone(phone)) {
            if (etPhone != null) etPhone.setError("Enter a valid Sri Lankan phone number (e.g., 0712345678)");
            return;
        }

        if (!emPhone.isEmpty() && !ValidationUtils.isValidSriLankanPhone(emPhone)) {
            if (etEmergencyPhone != null) etEmergencyPhone.setError("Enter a valid emergency contact phone number");
            return;
        }

        showLoading(true);

        currentPatient.setFullName(name);
        if (!email.isEmpty()) currentPatient.setEmail(email);
        currentPatient.setPhone(phone);
        currentPatient.setBloodGroup(blood);
        if (!weight.isEmpty()) currentPatient.setWeight(weight);
        currentPatient.setDateOfBirth(dob);
        currentPatient.setGender(gender);
        currentPatient.setAddress(address);
        currentPatient.setEmergencyContactName(emName);
        currentPatient.setEmergencyContactRelationship(emRel);
        currentPatient.setEmergencyContactPhone(emPhone);

        if (pendingImageBase64 != null) {
            currentPatient.setProfileImageUrl(pendingImageBase64);
        }

        profileRepository.updateProfile(currentPatient, new ProfileRepository.OnProfileUpdatedListener() {
            @Override
            public void onSuccess() {
                showLoading(false);
                Toast.makeText(EditProfileActivity.this, "Profile updated successfully! ✅", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                Toast.makeText(EditProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (progressBarEditProfile != null) {
            progressBarEditProfile.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (btnSaveProfile != null) {
            btnSaveProfile.setEnabled(!isLoading);
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(EditProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}