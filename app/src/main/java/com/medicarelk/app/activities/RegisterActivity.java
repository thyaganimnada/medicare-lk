package com.medicarelk.app.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.medicarelk.app.R;
import com.medicarelk.app.models.Patient;
import com.medicarelk.app.models.User;
import com.medicarelk.app.utils.ValidationUtils;

import java.util.Calendar;
import java.util.Locale;

/**
 * Registration Activity for MediCare LK.
 * Collects patient details using the exact white card input design & custom spinners matching Edit Profile.
 */
public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private EditText etFullName;
    private EditText etEmail;
    private EditText etPhone;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Spinner spBloodGroup;
    private EditText etWeight;
    private EditText etDob;
    private Spinner spGender;
    private EditText etAddress;
    private EditText etEmergencyContact;
    private CheckBox cbTerms;
    private Button btnRegister;
    private TextView tvLoginLink;
    private ProgressBar progressBarRegister;

    private long lastClickTime = 0;
    private static final long CLICK_THRESHOLD_MS = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_register);

        View rootView = findViewById(R.id.rootRegister);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupGenderDropdown();
        setupBloodGroupDropdown();
        setupDatePicker();
        setupListeners();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        spBloodGroup = findViewById(R.id.spBloodGroup);
        etWeight = findViewById(R.id.etWeight);
        etDob = findViewById(R.id.etDob);
        spGender = findViewById(R.id.spGender);
        etAddress = findViewById(R.id.etAddress);
        etEmergencyContact = findViewById(R.id.etEmergencyContact);
        cbTerms = findViewById(R.id.cbTerms);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);
        progressBarRegister = findViewById(R.id.progressBarRegister);
    }

    private void setupGenderDropdown() {
        String[] genders = getResources().getStringArray(R.array.gender_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_selected, genders);
        adapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        if (spGender != null) {
            spGender.setAdapter(adapter);
        }
    }

    private void setupBloodGroupDropdown() {
        String[] bloodGroups = getResources().getStringArray(R.array.blood_group_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_selected, bloodGroups);
        adapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        if (spBloodGroup != null) {
            spBloodGroup.setAdapter(adapter);
        }
    }

    private void setupDatePicker() {
        if (etDob != null) {
            etDob.setOnClickListener(v -> showDatePicker());
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR) - 18;
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                RegisterActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.US, "%04d-%02d-%02d",
                            selectedYear, selectedMonth + 1, selectedDay);
                    if (etDob != null) etDob.setText(formattedDate);
                },
                year, month, day
        );

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void setupListeners() {
        if (tvLoginLink != null) {
            tvLoginLink.setOnClickListener(v -> {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }

        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> attemptRegistration());
        }
    }

    private void attemptRegistration() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < CLICK_THRESHOLD_MS) {
            return;
        }
        lastClickTime = currentTime;

        String fullName = getInputValue(etFullName);
        String email = getInputValue(etEmail);
        String phone = getInputValue(etPhone);
        String password = etPassword != null && etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirmPassword = etConfirmPassword != null && etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";
        String bloodGroup = spBloodGroup != null && spBloodGroup.getSelectedItem() != null ? spBloodGroup.getSelectedItem().toString() : "B+";
        String weight = getInputValue(etWeight);
        String dob = getInputValue(etDob);
        String gender = spGender != null && spGender.getSelectedItem() != null ? spGender.getSelectedItem().toString() : "Male";
        String address = getInputValue(etAddress);
        String emergencyContact = getInputValue(etEmergencyContact);
        boolean isTermsAccepted = cbTerms != null && cbTerms.isChecked();

        // 1. Full Name
        if (fullName.isEmpty()) {
            if (etFullName != null) etFullName.setError(getString(R.string.error_full_name_required));
            return;
        } else if (fullName.length() < 2) {
            if (etFullName != null) etFullName.setError(getString(R.string.error_full_name_min_length));
            return;
        } else if (!ValidationUtils.isValidName(fullName)) {
            if (etFullName != null) etFullName.setError(getString(R.string.error_full_name_numeric));
            return;
        }

        // 2. Email
        if (email.isEmpty()) {
            if (etEmail != null) etEmail.setError(getString(R.string.error_email_required));
            return;
        } else if (!ValidationUtils.isValidEmail(email)) {
            if (etEmail != null) etEmail.setError(getString(R.string.error_email_invalid));
            return;
        }

        // 3. Phone Number
        if (phone.isEmpty()) {
            if (etPhone != null) etPhone.setError(getString(R.string.error_phone_required));
            return;
        } else if (!ValidationUtils.isValidSriLankanPhone(phone)) {
            if (etPhone != null) etPhone.setError(getString(R.string.error_phone_invalid));
            return;
        }

        // 4. Password
        if (password.isEmpty()) {
            if (etPassword != null) etPassword.setError(getString(R.string.error_password_required));
            return;
        } else if (password.length() < 8) {
            if (etPassword != null) etPassword.setError(getString(R.string.error_password_min_length));
            return;
        } else if (!ValidationUtils.isValidRegistrationPassword(password)) {
            if (etPassword != null) etPassword.setError(getString(R.string.error_password_complexity));
            return;
        }

        // 5. Confirm Password
        if (confirmPassword.isEmpty()) {
            if (etConfirmPassword != null) etConfirmPassword.setError(getString(R.string.error_confirm_password_required));
            return;
        } else if (!ValidationUtils.isPasswordMatching(password, confirmPassword)) {
            if (etConfirmPassword != null) etConfirmPassword.setError(getString(R.string.error_password_mismatch));
            return;
        }

        // 6. Date of Birth
        if (dob.isEmpty()) {
            if (etDob != null) etDob.setError(getString(R.string.error_dob_required));
            return;
        } else if (!ValidationUtils.isValidDateOfBirth(dob)) {
            if (etDob != null) etDob.setError(getString(R.string.error_dob_future));
            return;
        }

        // 7. Address
        if (!ValidationUtils.isValidAddress(address)) {
            if (etAddress != null) etAddress.setError(getString(R.string.error_address_required));
            return;
        }

        // 8. Emergency Contact
        if (emergencyContact.isEmpty()) {
            if (etEmergencyContact != null) etEmergencyContact.setError(getString(R.string.error_emergency_contact_required));
            return;
        } else if (!ValidationUtils.isValidSriLankanPhone(emergencyContact)) {
            if (etEmergencyContact != null) etEmergencyContact.setError(getString(R.string.error_emergency_contact_invalid));
            return;
        }

        // 9. Terms and Conditions
        if (!isTermsAccepted) {
            Toast.makeText(this, getString(R.string.error_terms_required), Toast.LENGTH_SHORT).show();
            return;
        }

        // Create User & Patient model objects
        User user = new User(fullName, email, phone, dob, gender, address, emergencyContact);
        Patient patient = new Patient("", fullName, email, phone, "", "PATIENT");
        patient.setDateOfBirth(dob);
        patient.setGender(gender);
        patient.setAddress(address);
        patient.setEmergencyContact(emergencyContact);
        if (!bloodGroup.isEmpty()) patient.setBloodGroup(bloodGroup);
        if (!weight.isEmpty()) patient.setWeight(weight);

        showLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();
                            user.setUid(uid);
                            patient.setUid(uid);

                            // Save to Cloud Firestore users/{uid}
                            FirebaseFirestore.getInstance().collection("users").document(uid).set(patient);

                            // Save to Firebase Realtime Database Users/{uid}
                            DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users");
                            database.child(uid).setValue(patient)
                                    .addOnCompleteListener(dbTask -> {
                                        showLoading(false);
                                        Toast.makeText(RegisterActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    });
                        }
                    } else {
                        showLoading(false);
                        String error = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String getInputValue(EditText editText) {
        return editText != null && editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private void showLoading(boolean isLoading) {
        if (progressBarRegister != null) {
            progressBarRegister.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (btnRegister != null) {
            btnRegister.setEnabled(!isLoading);
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}