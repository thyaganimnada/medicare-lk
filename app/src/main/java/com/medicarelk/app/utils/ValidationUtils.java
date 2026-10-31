package com.medicarelk.app.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utility class containing reusable input validation methods for MediCare LK.
 */
public class ValidationUtils {

    // Regex for Email address validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );

    // Regex for Sri Lankan phone numbers: 07XXXXXXXX or +947XXXXXXXX or 947XXXXXXXX
    private static final Pattern SRI_LANKAN_PHONE_PATTERN =
            Pattern.compile("^(?:\\+94|0)?7[0-9]{8}$");

    // Regex for checking if string contains only digits
    private static final Pattern NUMERIC_ONLY_PATTERN = Pattern.compile("^[0-9]+$");

    // Date format for Date of Birth
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    private ValidationUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Validates if email is non-empty and matches valid email pattern.
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String trimmed = email.trim();
        return !trimmed.isEmpty() && EMAIL_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Validates minimum password length for Login.
     */
    public static boolean isValidLoginPassword(String password) {
        if (password == null) {
            return false;
        }
        return password.length() >= 8;
    }

    /**
     * Validates password strength for Registration or Change Password.
     * Minimum 8 characters, at least 1 uppercase, 1 lowercase, 1 number.
     */
    public static boolean isValidRegistrationPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasUppercase = !password.equals(password.toLowerCase(Locale.ROOT));
        boolean hasLowercase = !password.equals(password.toUpperCase(Locale.ROOT));
        boolean hasDigit = password.matches(".*[0-9].*");

        return hasUppercase && hasLowercase && hasDigit;
    }

    public static boolean isValidPassword(String password) {
        return isValidRegistrationPassword(password);
    }

    /**
     * Checks whether confirm password matches password.
     */
    public static boolean isPasswordMatching(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }

    /**
     * Validates full name.
     * Required, minimum 2 characters, cannot contain only numbers.
     */
    public static boolean isValidName(String name) {
        if (name == null) {
            return false;
        }
        String trimmed = name.trim();
        if (trimmed.length() < 2) {
            return false;
        }
        return !NUMERIC_ONLY_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Validates Sri Lankan phone number format (07XXXXXXXX or +947XXXXXXXX).
     */
    public static boolean isValidSriLankanPhone(String phone) {
        if (phone == null) {
            return false;
        }
        String trimmed = phone.trim().replaceAll("\\s+", "");
        return SRI_LANKAN_PHONE_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Validates Date of Birth string format and ensures it is not in the future.
     */
    public static boolean isValidDateOfBirth(String dobString) {
        if (dobString == null || dobString.trim().isEmpty()) {
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        sdf.setLenient(false);
        try {
            Date dob = sdf.parse(dobString.trim());
            if (dob == null) {
                return false;
            }
            // Check if date is in the future
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 23);
            today.set(Calendar.MINUTE, 59);
            today.set(Calendar.SECOND, 59);

            return !dob.after(today.getTime());
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Validates Gender selection.
     */
    public static boolean isValidGender(String gender) {
        if (gender == null) {
            return false;
        }
        String trimmed = gender.trim();
        return !trimmed.isEmpty() && !trimmed.equalsIgnoreCase("Select Gender");
    }

    /**
     * Validates Address string.
     */
    public static boolean isValidAddress(String address) {
        if (address == null) {
            return false;
        }
        return !address.trim().isEmpty();
    }

    /**
     * Validates terms and conditions checkbox.
     */
    public static boolean isTermsAccepted(boolean isChecked) {
        return isChecked;
    }
}