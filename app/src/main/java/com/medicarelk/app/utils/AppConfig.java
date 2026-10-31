package com.medicarelk.app.utils;

/**
 * Centralized application configuration constants for business rules and policies.
 */
public class AppConfig {

    // Cancellation Policy: Hours before consultation time when cancellation is restricted
    public static final int CANCELLATION_RESTRICTED_HOURS = 2;

    // Rescheduling Policy: Hours before consultation time when rescheduling is restricted
    public static final int RESCHEDULE_RESTRICTED_HOURS = 2;

    // Slot Duration in minutes
    public static final int SLOT_DURATION_MINUTES = 30;

    // Default Consultation Fee
    public static final double DEFAULT_CONSULTATION_FEE = 3500.0;

    private AppConfig() {
        // Prevent instantiation
    }
}