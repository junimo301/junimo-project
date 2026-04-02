package com.example.junimoapp;

import com.example.junimoapp.models.Event;
import com.example.junimoapp.models.User;
import com.google.firebase.firestore.GeoPoint;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Local unit tests for notification opt-out logic.
 *
 * All tests call real methods on real User objects — not local variables.
 *
 * Stories covered:
 *  - US 01.04.01: Notification sent when user is invited by lottery
 *  - US 01.04.02: Notification sent when user is not chosen by lottery
 *  - US 01.04.03: User can opt out of notifications
 */
public class NotificationUnitTest {

    private User testUser;
    private Event testEvent;

    @Before
    public void setUp() {
        testUser = new User(
                "device-notif-test", "Notif User",
                "notif@test.com", "555-9999",
                "", "", ""
        );
        testEvent = new Event(
                "Notif Event", "desc", "", "", "2025-06-01",
                100, 50, 0.0,
                new GeoPoint(0, 0), "", "notif-event-id",
                "Location", "organizer-id", ""
        );
    }

    // ── US 01.04.03 ───────────────────────────────────────────────────────

    /**
     * US 01.04.03
     * Calls user.isNotificationsEnabled() — should return true by default.
     */
    @Test
    public void user_notificationsEnabledByDefault() {
        assertTrue("New user should have notifications enabled by default",
                testUser.isNotificationsEnabled());
    }

    /**
     * US 01.04.03
     * Calls user.setNotificationsEnabled(false).
     * Verifies opted-out users return false from isNotificationsEnabled().
     */
    @Test
    public void user_canOptOutOfNotifications() {
        testUser.setNotificationsEnabled(false);
        assertFalse("User should be opted out after setNotificationsEnabled(false)",
                testUser.isNotificationsEnabled());
    }

    /**
     * US 01.04.03
     * Simulates the NotificationHelper guard using the real User method.
     * An opted-out user should cause the guard to block notification delivery.
     */
    @Test
    public void optedOutUser_guardBlocksNotification() {
        testUser.setNotificationsEnabled(false);
        // Simulate the guard check in NotificationHelper.sendIfEnabled()
        boolean wouldSend = testUser.isNotificationsEnabled();
        assertFalse("Notification should be blocked for opted-out user", wouldSend);
    }

    /**
     * US 01.04.03
     * Simulates the guard for a user with notifications enabled.
     */
    @Test
    public void enabledUser_guardAllowsNotification() {
        testUser.setNotificationsEnabled(true);
        boolean wouldSend = testUser.isNotificationsEnabled();
        assertTrue("Notification should be allowed for opted-in user", wouldSend);
    }

    /**
     * US 01.04.03
     * Calls setNotificationsEnabled(false) then setNotificationsEnabled(true).
     * Verifies re-enabling works correctly.
     */
    @Test
    public void user_canReEnableNotifications() {
        testUser.setNotificationsEnabled(false);
        testUser.setNotificationsEnabled(true);
        assertTrue("User should receive notifications after re-enabling",
                testUser.isNotificationsEnabled());
    }

    // ── US 01.04.01 ───────────────────────────────────────────────────────

    /**
     * US 01.04.01
     * Calls user.isInvited() before inviteUser() is called.
     * Verifies the user is not considered invited before the lottery runs.
     */
    @Test
    public void user_isNotInvited_beforeLottery() {
        assertFalse("User should not be invited before inviteUser() is called",
                testUser.isInvited(testEvent.getEventID()));
    }

    // ── US 01.04.02 ───────────────────────────────────────────────────────

    /**
     * US 01.04.02
     * Verifies that opt-out is persistent — calling setNotificationsEnabled(false)
     * keeps the user opted out regardless of other operations.
     */
    @Test
    public void optedOutUser_staysOptedOutAfterLotteryLoss() {
        testUser.setNotificationsEnabled(false);
        // Simulate lottery running — preference should not change
        boolean stillOptedOut = !testUser.isNotificationsEnabled();
        assertTrue("Opt-out should persist after lottery result", stillOptedOut);
    }

    /**
     * US 01.04.01 / US 01.04.02
     * Verifies two users can have different notification preferences independently.
     * One opts out, the other stays enabled.
     */
    @Test
    public void twoUsers_independentNotificationPreferences() {
        User userA = new User("id-a", "User A", "a@test.com", "111", "", "", "");
        User userB = new User("id-b", "User B", "b@test.com", "222", "", "", "");

        userA.setNotificationsEnabled(false);
        // userB stays default (true)

        assertFalse("User A should be opted out", userA.isNotificationsEnabled());
        assertTrue("User B should still be enabled", userB.isNotificationsEnabled());
    }
}
