package com.example.junimoapp;

import com.example.junimoapp.models.User;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * unit tests related to ProfileActivity logic
 * tests run locally and verify user creation
 */
public class ProfileActivityTest {
    //test that user data is created
    @Test
    public void userCreation_isCorrect() {

        String deviceId = "device123";
        String name = "dummyname";
        String email = "dummyname@test.com";
        String phone = "123456789";

        User user = new User(deviceId, name, email, phone, "", "", "");

        assertEquals(deviceId, user.getDeviceId());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(phone, user.getPhone());
    }
    //test that un-written data in optional fields (no email or phone for signup) is accepted
    @Test
    public void userCreation_emptyFieldsAllowed() {

        String deviceId = "device456";
        String name = "";
        String email = "";
        String phone = "";

        User user = new User(deviceId, name, email, phone, "", "", "");

        assertEquals(deviceId, user.getDeviceId());
        assertEquals("", user.getName());
        assertEquals("", user.getEmail());
        assertEquals("", user.getPhone());
    }
    //tests that data is properly stored
    @Test
    public void userCreation_deviceIdStoredCorrectly() {

        String deviceId = "uniqueDeviceID";

        User user = new User(deviceId, "Test", "test@email.com", "111111", "", "", "");

        assertTrue(user.getDeviceId().equals(deviceId));
    }
}
