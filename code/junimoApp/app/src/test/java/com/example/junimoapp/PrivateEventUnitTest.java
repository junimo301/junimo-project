package com.example.junimoapp;

import com.example.junimoapp.models.Event;
import com.example.junimoapp.models.User;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Local unit tests for private event and notification opt-out logic.
 *
 * All tests call real methods on real model objects — not local variables.
 *
 * Stories covered:
 *  - US 02.01.02: Organizer creates a private event (no public listing, no QR)
 *  - US 01.04.03: Entrant can opt out of notifications
 */
public class PrivateEventUnitTest {

    private Event testEvent;
    private User testUser;

    @Before
    public void setUp() {
        // Real Event object using the same constructor as the rest of the app
        testEvent = new Event(
                "Test Event", "desc", "2025-01-01", "2025-01-10",
                "2025-02-01", 50, 20, 10.0,
                true, "", "event-test-id",
                "Test Location", "organizer-test-id", ""
        );
        // Real User object using the same constructor as the rest of the app
        testUser = new User(
                "device-test-id", "Test User",
                "test@email.com", "555-1234",
                "", "", ""
        );
    }

    // ── US 02.01.02 ───────────────────────────────────────────────────────

    /**
     * US 02.01.02
     * Calls event.isPrivate() on a freshly constructed Event.
     * Verifies the method returns false by default — events start public.
     */
    @Test
    public void event_isPrivate_defaultsToFalse() {
        assertFalse("isPrivate() should return false on a new Event",
                testEvent.isPrivate());
    }

    /**
     * US 02.01.02
     * Calls event.setPrivate(true) then event.isPrivate().
     * Verifies the getter reflects the value set by the setter.
     */
    @Test
    public void event_setPrivateTrue_isPrivateReturnsTrue() {
        testEvent.setPrivate(true);
        assertTrue("isPrivate() should return true after setPrivate(true)",
                testEvent.isPrivate());
    }

    /**
     * US 02.01.02
     * Calls event.setPrivate(false) after setting it to true.
     * Verifies the event can be switched back to public.
     */
    @Test
    public void event_setPrivateFalse_isPrivateReturnsFalse() {
        testEvent.setPrivate(true);
        testEvent.setPrivate(false);
        assertFalse("isPrivate() should return false after setPrivate(false)",
                testEvent.isPrivate());
    }

    /**
     * US 02.01.02
     * Calls event.setPrivate(true), then simulates the CreateEvent logic:
     * only call setQRCode() when the event is NOT private.
     * Verifies getQRCode() returns null for private events.
     */
    @Test
    public void privateEvent_qrCodeRemainsNull() {
        testEvent.setPrivate(true);

        // Simulate CreateEvent.uploadButton logic
        String qrString = "junimo://event?id=event-test-id";
        if (!testEvent.isPrivate()) {
            testEvent.setQRCode(qrString);
        }

        assertNull("Private event should have null QR code",
                testEvent.getQRCode());
    }

    /**
     * US 02.01.02
     * Calls event.setPrivate(false), then calls event.setQRCode().
     * Verifies getQRCode() returns the set value for public events.
     */
    @Test
    public void publicEvent_qrCodeIsSet() {
        testEvent.setPrivate(false);
        String qrString = "junimo://event?id=event-test-id";

        if (!testEvent.isPrivate()) {
            testEvent.setQRCode(qrString);
        }

        assertEquals("Public event should store the QR code string",
                qrString, testEvent.getQRCode());
    }

    // ── US 01.04.03 ───────────────────────────────────────────────────────

    /**
     * US 01.04.03
     * Calls user.isNotificationsEnabled() on a freshly constructed User.
     * Verifies it returns true by default.
     */
    @Test
    public void user_notificationsEnabled_defaultsToTrue() {
        assertTrue("isNotificationsEnabled() should return true by default",
                testUser.isNotificationsEnabled());
    }

    /**
     * US 01.04.03
     * Calls user.setNotificationsEnabled(false) then isNotificationsEnabled().
     * Verifies the user can opt out.
     */
    @Test
    public void user_setNotificationsEnabledFalse_returnsFalse() {
        testUser.setNotificationsEnabled(false);
        assertFalse("isNotificationsEnabled() should return false after opt-out",
                testUser.isNotificationsEnabled());
    }

    /**
     * US 01.04.03
     * Calls setNotificationsEnabled(false) then setNotificationsEnabled(true).
     * Verifies the user can re-enable notifications.
     */
    @Test
    public void user_reEnableNotifications_returnsTrue() {
        testUser.setNotificationsEnabled(false);
        testUser.setNotificationsEnabled(true);
        assertTrue("isNotificationsEnabled() should return true after re-enabling",
                testUser.isNotificationsEnabled());
    }
}
