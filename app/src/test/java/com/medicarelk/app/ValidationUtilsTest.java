package com.medicarelk.app;

import com.medicarelk.app.utils.ValidationUtils;
import org.junit.Test;
import static org.junit.Assert.*;

public class ValidationUtilsTest {

    @Test
    public void testEmailValidation() {
        assertTrue(ValidationUtils.isValidEmail("patient@medicare.lk"));
        assertTrue(ValidationUtils.isValidEmail("john.doe@gmail.com"));
        assertFalse(ValidationUtils.isValidEmail("invalid-email"));
        assertFalse(ValidationUtils.isValidEmail(""));
        assertFalse(ValidationUtils.isValidEmail(null));
    }

    @Test
    public void testLoginPasswordValidation() {
        assertTrue(ValidationUtils.isValidLoginPassword("pass1234"));
        assertTrue(ValidationUtils.isValidLoginPassword("StrongPass1"));
        assertFalse(ValidationUtils.isValidLoginPassword("1234567"));
        assertFalse(ValidationUtils.isValidLoginPassword(""));
        assertFalse(ValidationUtils.isValidLoginPassword(null));
    }

    @Test
    public void testRegistrationPasswordValidation() {
        assertTrue(ValidationUtils.isValidRegistrationPassword("MediCare123"));
        assertTrue(ValidationUtils.isValidRegistrationPassword("Pass12345"));
        assertFalse(ValidationUtils.isValidRegistrationPassword("medicare123")); // no uppercase
        assertFalse(ValidationUtils.isValidRegistrationPassword("MEDICARE123")); // no lowercase
        assertFalse(ValidationUtils.isValidRegistrationPassword("MediCareLk"));  // no digit
        assertFalse(ValidationUtils.isValidRegistrationPassword("Pass1"));       // short
    }

    @Test
    public void testPasswordMatching() {
        assertTrue(ValidationUtils.isPasswordMatching("Password123", "Password123"));
        assertFalse(ValidationUtils.isPasswordMatching("Password123", "Password456"));
        assertFalse(ValidationUtils.isPasswordMatching(null, "Password123"));
    }

    @Test
    public void testNameValidation() {
        assertTrue(ValidationUtils.isValidName("Kusal Perera"));
        assertTrue(ValidationUtils.isValidName("Dr. Nimal"));
        assertFalse(ValidationUtils.isValidName("A"));
        assertFalse(ValidationUtils.isValidName("123456"));
        assertFalse(ValidationUtils.isValidName(""));
        assertFalse(ValidationUtils.isValidName(null));
    }

    @Test
    public void testSriLankanPhoneValidation() {
        assertTrue(ValidationUtils.isValidSriLankanPhone("0771234567"));
        assertTrue(ValidationUtils.isValidSriLankanPhone("+94711234567"));
        assertTrue(ValidationUtils.isValidSriLankanPhone("071 123 4567"));
        assertFalse(ValidationUtils.isValidSriLankanPhone("0112345678")); // not 07...
        assertFalse(ValidationUtils.isValidSriLankanPhone("07712345"));   // too short
        assertFalse(ValidationUtils.isValidSriLankanPhone("abcd"));
        assertFalse(ValidationUtils.isValidSriLankanPhone(null));
    }

    @Test
    public void testDateOfBirthValidation() {
        assertTrue(ValidationUtils.isValidDateOfBirth("1995-05-15"));
        assertTrue(ValidationUtils.isValidDateOfBirth("2000-01-01"));
        assertFalse(ValidationUtils.isValidDateOfBirth("2030-01-01")); // future
        assertFalse(ValidationUtils.isValidDateOfBirth("invalid-date"));
        assertFalse(ValidationUtils.isValidDateOfBirth(""));
        assertFalse(ValidationUtils.isValidDateOfBirth(null));
    }

    @Test
    public void testGenderValidation() {
        assertTrue(ValidationUtils.isValidGender("Male"));
        assertTrue(ValidationUtils.isValidGender("Female"));
        assertTrue(ValidationUtils.isValidGender("Other"));
        assertFalse(ValidationUtils.isValidGender("Select Gender"));
        assertFalse(ValidationUtils.isValidGender(""));
        assertFalse(ValidationUtils.isValidGender(null));
    }

    @Test
    public void testAddressValidation() {
        assertTrue(ValidationUtils.isValidAddress("123, Main Street, Colombo 03"));
        assertFalse(ValidationUtils.isValidAddress("   "));
        assertFalse(ValidationUtils.isValidAddress(null));
    }
}