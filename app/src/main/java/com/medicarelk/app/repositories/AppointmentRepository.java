package com.medicarelk.app.repositories;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.medicarelk.app.models.Appointment;
import com.medicarelk.app.models.Doctor;
import com.medicarelk.app.models.HealthPackage;
import com.medicarelk.app.models.LabTest;
import com.medicarelk.app.models.Notification;
import com.medicarelk.app.utils.AppConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Repository using Firebase Realtime Database & Cloud Firestore for appointments.
 * Integrates automatic in-app and system tray notification triggers on appointment creation, rescheduling, completion, and cancellation.
 */
public class AppointmentRepository {

    public interface OnAppointmentCreatedListener {
        void onSuccess(String appointmentId);
        void onDoubleBookingError();
        void onError(String errorMessage);
    }

    public interface OnAppointmentsLoadedListener {
        void onSuccess(List<Appointment> upcomingList, List<Appointment> historyList, List<Appointment> cancelledList);
        void onError(String errorMessage);
    }

    public interface OnAppointmentDetailsLoadedListener {
        void onSuccess(Appointment appointment);
        void onError(String errorMessage);
    }

    public interface OnAppointmentUpdatedListener {
        void onSuccess();
        void onError(String errorMessage);
    }

    public interface OnBookedSlotsLoadedListener {
        void onSuccess(List<String> bookedSlots);
    }

    private final NotificationRepository notificationRepository;
    private static final Set<String> completedNotifSent = new HashSet<>();

    public AppointmentRepository() {
        this.notificationRepository = new NotificationRepository();
    }

    /**
     * Creates an appointment in Firebase Realtime Database & Cloud Firestore and triggers a confirmation notification.
     */
    public void createAppointmentWithTransaction(Appointment appointment, OnAppointmentCreatedListener listener) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            listener.onError("Unauthenticated request.");
            return;
        }

        appointment.setPatientId(user.getUid());

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Appointments");
        String apptId = dbRef.push().getKey();
        if (apptId == null) {
            apptId = "MC-" + System.currentTimeMillis();
        }

        appointment.setAppointmentId(apptId);
        appointment.setCreatedAt(System.currentTimeMillis());
        appointment.setUpdatedAt(System.currentTimeMillis());

        final String finalApptId = apptId;

        // Save to Cloud Firestore appointments/{appointmentId}
        FirebaseFirestore.getInstance().collection("appointments").document(finalApptId).set(appointment);

        // Save to Firebase Realtime Database Appointments/{appointmentId}
        dbRef.child(finalApptId).setValue(appointment)
                .addOnSuccessListener(aVoid -> {
                    // Trigger in-app & mobile confirmation notification
                    Notification notif = new Notification(
                            "",
                            user.getUid(),
                            Notification.TYPE_APPOINTMENT_CONFIRMED,
                            "Appointment Confirmed 📅",
                            "Your appointment with " + appointment.getDoctorNameSnapshot() + " is confirmed for " +
                                    appointment.getAppointmentDate() + " at " + appointment.getAppointmentTime() + ".",
                            finalApptId, "", ""
                    );
                    notificationRepository.createNotification(notif, null);

                    listener.onSuccess(finalApptId);
                })
                .addOnFailureListener(e -> listener.onError("Booking error: " + e.getMessage()));
    }

    /**
     * Retrieves patient appointments and handles completion notifications for past appointments.
     */
    public void getPatientAppointments(String patientId, OnAppointmentsLoadedListener listener) {
        if (patientId == null || patientId.trim().isEmpty()) {
            listener.onError("Invalid patient ID.");
            return;
        }

        // Build active doctor, lab test, and health package image lookup map
        DoctorRepository docRepo = new DoctorRepository();
        docRepo.getActiveDoctors(new DoctorRepository.OnDoctorsLoadedListener() {
            @Override
            public void onSuccess(List<Doctor> doctors) {
                java.util.Map<String, String> imageMap = new java.util.HashMap<>();
                if (doctors != null) {
                    for (Doctor doc : doctors) {
                        if (doc.getDoctorId() != null) {
                            imageMap.put(doc.getDoctorId().toLowerCase(), doc.getProfileImageUrl());
                        }
                        if (doc.getFullName() != null) {
                            imageMap.put(doc.getFullName().toLowerCase().trim(), doc.getProfileImageUrl());
                        }
                    }
                }

                // Add sample lab test & health package image maps
                LabTestRepository labRepo = new LabTestRepository();
                for (LabTest test : labRepo.getSampleLabTests()) {
                    if (test.getName() != null && test.getImageUrl() != null) {
                        imageMap.put(test.getName().toLowerCase().trim(), test.getImageUrl());
                    }
                }

                HealthPackageRepository pkgRepo = new HealthPackageRepository();
                for (HealthPackage pkg : pkgRepo.getSamplePackages()) {
                    if (pkg.getName() != null && pkg.getImageUrl() != null) {
                        imageMap.put(pkg.getName().toLowerCase().trim(), pkg.getImageUrl());
                    }
                }

                FirebaseDatabase.getInstance().getReference("Appointments")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                List<Appointment> upcomingList = new ArrayList<>();
                                List<Appointment> historyList = new ArrayList<>();
                                List<Appointment> cancelledList = new ArrayList<>();

                                if (snapshot.exists()) {
                                    for (DataSnapshot child : snapshot.getChildren()) {
                                        Appointment appt = child.getValue(Appointment.class);
                                        if (appt != null && patientId.equals(appt.getPatientId())) {
                                            appt.setAppointmentId(child.getKey());

                                            // Automatically resolve doctor/lab/package image if snapshot is missing
                                            String img = appt.getDoctorProfileImageUrlSnapshot();
                                            if (img == null || img.trim().isEmpty()) {
                                                String docIdKey = appt.getDoctorId() != null ? appt.getDoctorId().toLowerCase() : "";
                                                String docNameKey = appt.getDoctorNameSnapshot() != null ? appt.getDoctorNameSnapshot().toLowerCase().trim() : "";

                                                String mappedImg = imageMap.get(docIdKey);
                                                if (mappedImg == null || mappedImg.trim().isEmpty()) {
                                                    mappedImg = imageMap.get(docNameKey);
                                                }

                                                // Fuzzy match if name starts with "Diagnostic Laboratory (" or "Health Screening ("
                                                if ((mappedImg == null || mappedImg.trim().isEmpty()) && docNameKey.contains("(")) {
                                                    int start = docNameKey.indexOf("(") + 1;
                                                    int end = docNameKey.indexOf(")");
                                                    if (start > 0 && end > start) {
                                                        String innerName = docNameKey.substring(start, end).trim();
                                                        mappedImg = imageMap.get(innerName);
                                                    }
                                                }

                                                if (mappedImg != null && !mappedImg.trim().isEmpty()) {
                                                    appt.setDoctorProfileImageUrlSnapshot(mappedImg);
                                                }
                                            }

                                            String effStatus = appt.getEffectiveStatus();
                                            if (Appointment.STATUS_CANCELLED.equalsIgnoreCase(effStatus) ||
                                                    Appointment.STATUS_CANCELLED.equalsIgnoreCase(appt.getStatus())) {
                                                cancelledList.add(appt);
                                            } else if (Appointment.STATUS_COMPLETED.equalsIgnoreCase(effStatus) || appt.isPast()) {
                                                historyList.add(appt);

                                                // Trigger completion notification if not sent in this session
                                                if (appt.getAppointmentId() != null && !completedNotifSent.contains(appt.getAppointmentId())) {
                                                    completedNotifSent.add(appt.getAppointmentId());
                                                    Notification endNotif = new Notification(
                                                            "",
                                                            patientId,
                                                            Notification.TYPE_APPOINTMENT_REMINDER,
                                                            "Appointment Completed ✅",
                                                            "Your appointment with " + appt.getDoctorNameSnapshot() + " on " + appt.getAppointmentDate() + " has ended. Thank you for choosing MediCare LK!",
                                                            appt.getAppointmentId(), "", ""
                                                    );
                                                    notificationRepository.createNotification(endNotif, null);
                                                }
                                            } else {
                                                upcomingList.add(appt);
                                            }
                                        }
                                    }
                                }

                                // Sort all appointment lists in descending order (latest/newest booked appointment at the TOP)
                                java.util.Comparator<Appointment> newestFirstComparator = (a1, a2) -> {
                                    long t1 = a1.getCreatedAt() > 0 ? a1.getCreatedAt() : a1.getAppointmentTimestamp();
                                    long t2 = a2.getCreatedAt() > 0 ? a2.getCreatedAt() : a2.getAppointmentTimestamp();
                                    return Long.compare(t2, t1);
                                };

                                upcomingList.sort(newestFirstComparator);
                                historyList.sort(newestFirstComparator);
                                cancelledList.sort(newestFirstComparator);

                                listener.onSuccess(upcomingList, historyList, cancelledList);
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                listener.onSuccess(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                            }
                        });
            }

            @Override
            public void onError(String errorMessage) {
                listener.onError(errorMessage);
            }
        });
    }

    /**
     * Retrieves upcoming appointment for home screen preview.
     */
    public void getUpcomingAppointment(String patientId, OnAppointmentDetailsLoadedListener listener) {
        getPatientAppointments(patientId, new OnAppointmentsLoadedListener() {
            @Override
            public void onSuccess(List<Appointment> upcomingList, List<Appointment> historyList, List<Appointment> cancelledList) {
                if (upcomingList != null && !upcomingList.isEmpty()) {
                    listener.onSuccess(upcomingList.get(0));
                } else {
                    listener.onError("No upcoming appointments.");
                }
            }

            @Override
            public void onError(String errorMessage) {
                listener.onError("No upcoming appointments.");
            }
        });
    }

    /**
     * Retrieves appointment by ID.
     */
    public void getAppointmentById(String appointmentId, OnAppointmentDetailsLoadedListener listener) {
        if (appointmentId == null || appointmentId.trim().isEmpty()) {
            listener.onError("Invalid appointment ID.");
            return;
        }

        FirebaseDatabase.getInstance().getReference("Appointments").child(appointmentId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Appointment appt = snapshot.getValue(Appointment.class);
                            if (appt != null) {
                                appt.setAppointmentId(appointmentId);
                                listener.onSuccess(appt);
                                return;
                            }
                        }
                        listener.onError("Appointment record not found.");
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError("Error loading appointment details.");
                    }
                });
    }

    /**
     * Cancels an appointment in Realtime Database & Cloud Firestore and triggers cancellation notification.
     */
    public void cancelAppointment(Appointment appointment, OnAppointmentUpdatedListener listener) {
        if (appointment == null || appointment.getAppointmentId() == null) {
            listener.onError("Invalid appointment data.");
            return;
        }

        long currentTime = System.currentTimeMillis();
        long restrictedTimeMs = AppConfig.CANCELLATION_RESTRICTED_HOURS * 3600 * 1000L;
        if (appointment.getAppointmentTimestamp() > 0 &&
                (appointment.getAppointmentTimestamp() - currentTime) < restrictedTimeMs) {
            listener.onError("This appointment can no longer be cancelled because it is too close to the consultation time.");
            return;
        }

        appointment.setStatus(Appointment.STATUS_CANCELLED);
        appointment.setUpdatedAt(System.currentTimeMillis());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Appointments")
                .child(appointment.getAppointmentId());
        ref.child("status").setValue(Appointment.STATUS_CANCELLED);
        ref.child("updatedAt").setValue(System.currentTimeMillis());

        // Update Cloud Firestore as well
        FirebaseFirestore.getInstance().collection("appointments").document(appointment.getAppointmentId())
                .update("status", Appointment.STATUS_CANCELLED, "updatedAt", System.currentTimeMillis());

        // Trigger cancellation notification
        Notification notif = new Notification(
                "",
                appointment.getPatientId(),
                Notification.TYPE_APPOINTMENT_CANCELLED,
                "Appointment Cancelled ❌",
                "Your appointment with " + appointment.getDoctorNameSnapshot() + " scheduled for " + appointment.getAppointmentDate() + " at " + appointment.getAppointmentTime() + " has been cancelled.",
                appointment.getAppointmentId(), "", ""
        );
        notificationRepository.createNotification(notif, null);

        listener.onSuccess();
    }

    /**
     * Reschedules an appointment in Realtime Database & Cloud Firestore and triggers rescheduled notification.
     */
    public void rescheduleAppointment(String appointmentId, String doctorId, String newDate,
                                       String newTime, long newTimestamp, OnAppointmentUpdatedListener listener) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Appointments")
                .child(appointmentId);
        ref.child("appointmentDate").setValue(newDate);
        ref.child("appointmentTime").setValue(newTime);
        ref.child("appointmentTimestamp").setValue(newTimestamp);
        ref.child("status").setValue(Appointment.STATUS_CONFIRMED);
        ref.child("updatedAt").setValue(System.currentTimeMillis());

        // Update Cloud Firestore as well
        FirebaseFirestore.getInstance().collection("appointments").document(appointmentId)
                .update("appointmentDate", newDate, "appointmentTime", newTime, "appointmentTimestamp", newTimestamp, "status", Appointment.STATUS_CONFIRMED, "updatedAt", System.currentTimeMillis());

        // Trigger rescheduled notification
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Notification notif = new Notification(
                    "",
                    user.getUid(),
                    Notification.TYPE_APPOINTMENT_RESCHEDULED,
                    "Appointment Rescheduled 🕒",
                    "Your appointment has been rescheduled to " + newDate + " at " + newTime + ".",
                    appointmentId, "", ""
            );
            notificationRepository.createNotification(notif, null);
        }

        listener.onSuccess();
    }

    /**
     * Retrieves list of booked time slots.
     */
    public void getBookedTimeSlots(String doctorId, String date, OnBookedSlotsLoadedListener listener) {
        FirebaseDatabase.getInstance().getReference("Appointments")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<String> bookedSlots = new ArrayList<>();
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                Appointment appt = child.getValue(Appointment.class);
                                if (appt != null && doctorId.equals(appt.getDoctorId()) &&
                                        date.equalsIgnoreCase(appt.getAppointmentDate()) &&
                                        !Appointment.STATUS_CANCELLED.equalsIgnoreCase(appt.getStatus())) {
                                    bookedSlots.add(appt.getAppointmentTime());
                                }
                            }
                        }
                        listener.onSuccess(bookedSlots);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onSuccess(new ArrayList<>());
                    }
                });
    }
}