package com.medicarelk.app.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.medicarelk.app.R;
import com.medicarelk.app.utils.QRCodeGenerator;

/**
 * Booking Success Activity matching prototype s-booking-success layout.
 * Generates a real, scannable QR Code for hospital reception check-in.
 */
public class BookingSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_success);

        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        String apptId = getIntent().getStringExtra("appointmentId");
        String docName = getIntent().getStringExtra("doctorName");
        String date = getIntent().getStringExtra("appointmentDate");
        String time = getIntent().getStringExtra("appointmentTime");
        String hospital = getIntent().getStringExtra("hospitalName");

        TextView tvSuccessDoctorName = findViewById(R.id.tvSuccessDoctorName);
        TextView tvRefId = findViewById(R.id.tvRefId);
        TextView tvSuccessDateTime = findViewById(R.id.tvSuccessDateTime);
        TextView tvSuccessHospital = findViewById(R.id.tvSuccessHospital);
        ImageView ivQRCode = findViewById(R.id.ivQRCode);

        String doctorTitle = (docName != null && !docName.trim().isEmpty()) ? docName : "Dr. Selini Hettiarachchi";
        if (tvSuccessDoctorName != null) {
            tvSuccessDoctorName.setText("Your appointment with " + doctorTitle + " has been booked successfully.");
        }

        String token = "A-042";
        if (apptId != null && !apptId.trim().isEmpty()) {
            token = "A-" + apptId.substring(Math.max(0, apptId.length() - 3));
        }

        if (tvRefId != null) {
            tvRefId.setText(token);
        }

        if (tvSuccessDateTime != null && date != null && time != null) {
            tvSuccessDateTime.setText(date + " • " + time);
        }

        if (tvSuccessHospital != null && hospital != null) {
            tvSuccessHospital.setText(hospital);
        }

        // Generate REAL scannable QR Code containing full appointment verification payload
        if (ivQRCode != null) {
            String qrPayload = "MEDICARE-LK APPOINTMENT VERIFICATION\n" +
                    "Queue Token: " + token + "\n" +
                    "Appointment ID: " + (apptId != null ? apptId : "MC-1001") + "\n" +
                    "Doctor: " + doctorTitle + "\n" +
                    "Date & Time: " + (date != null ? date : "") + " " + (time != null ? time : "") + "\n" +
                    "Hospital: " + (hospital != null ? hospital : "MediCare Hospital");

            int sizePx = (int) (180 * getResources().getDisplayMetrics().density);
            Bitmap qrBitmap = QRCodeGenerator.generateQRCode(qrPayload, sizePx, sizePx);
            if (qrBitmap != null) {
                ivQRCode.setImageBitmap(qrBitmap);
            }
        }

        View btnCalendar = findViewById(R.id.btnCalendar);
        if (btnCalendar != null) {
            btnCalendar.setOnClickListener(v ->
                    Toast.makeText(BookingSuccessActivity.this, "Added to calendar 📅", Toast.LENGTH_SHORT).show());
        }

        View btnReceipt = findViewById(R.id.btnReceipt);
        if (btnReceipt != null) {
            btnReceipt.setOnClickListener(v ->
                    Toast.makeText(BookingSuccessActivity.this, "Receipt downloaded ⬇️", Toast.LENGTH_SHORT).show());
        }

        Button btnViewAppointment = findViewById(R.id.btnViewAppointment);
        if (btnViewAppointment != null) {
            btnViewAppointment.setOnClickListener(v -> {
                Intent intent = new Intent(BookingSuccessActivity.this, AppointmentsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
}