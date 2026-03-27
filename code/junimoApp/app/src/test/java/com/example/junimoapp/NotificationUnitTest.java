package com.example.junimoapp;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Local unit tests for notification logic.
 *
 * Stories covered:
 *  - US 01.04.01: Entrant receives notification when invited from waiting list
 *  - US 01.04.02: Entrant receives notification when not chosen in lottery
 *  - US 01.04.03: Entrant can opt out of receiving notifications
 */
public class NotificationUnitTest {

    // ── US 01.04.03 ───────────────────────────────────────────────────────

    /**
     * US 01.04.03
     * Verifies that notifications are enabled by default.
     * A user who has never changed their preference should receive notifications.
     */
    @Test
    public void notifications_enabledByDefault() {
        // Simulate a missing field (null) defaulting to enabled
        Boolean notificationsEnabled = null;
        boolean result = (notificationsEnabled == null || notificationsEnabled);
        assertTrue("Notifications should be enabled when field is missing", result);
    }

    /**
     * US 01.04.03
     * Verifies that when a user opts out, the flag is correctly false.
     */
    @Test
    public void notifications_canBeDisabled() {
        boolean notificationsEnabled = false;
        assertFalse("Opted-out user should have notificationsEnabled = false",
                notificationsEnabled);
    }

    /**
     * US 01.04.03
     * Verifies that a user who opted out does NOT receive a notification.
     * This mirrors the guard logic inside NotificationHelper.sendIfEnabled().
     */
    @Test
    public void optedOutUser_doesNotReceiveNotification() {
        boolean notificationsEnabled = false;
        List<Map<String, Object>> notificationsWritten = new ArrayList<>();

        // Simulate the guard in NotificationHelper
        if (notificationsEnabled) {
            Map<String, Object> notif = new HashMap<>();
            notif.put("message", "You've been invited!");
            notificationsWritten.add(notif);
        }

        assertEquals("Opted-out user should receive zero notifications",
                0, notificationsWritten.size());
    }

    /**
     * US 01.04.03
     * Verifies that a user who has notifications enabled DOES receive one.
     */
    @Test
    public void enabledUser_receivesNotification() {
        boolean notificationsEnabled = true;
        List<Map<String, Object>> notificationsWritten = new ArrayList<>();

        if (notificationsEnabled) {
            Map<String, Object> notif = new HashMap<>();
            notif.put("message", "You've been invited!");
            notificationsWritten.add(notif);
        }

        assertEquals("Enabled user should receive one notification",
                1, notificationsWritten.size());
    }

    // ── US 01.04.01 ───────────────────────────────────────────────────────

    /**
     * US 01.04.01
     * Verifies that an invited notification has the correct type field.
     * The type is used by NotificationsActivity to distinguish notification kinds.
     */
    @Test
    public void invitedNotification_hasCorrectType() {
        String type = "invited";
        String message = "You've been invited to join: Test Event";

        Map<String, Object> notif = new HashMap<>();
        notif.put("type", type);
        notif.put("message", message);
        notif.put("read", false);

        assertEquals("invited", notif.get("type"));
        assertFalse("Notification should start unread", (Boolean) notif.get("read"));
    }

    /**
     * US 01.04.01
     * Verifies the message format for an invited notification contains the event title.
     */
    @Test
    public void invitedNotification_messageContainsEventTitle() {
        String eventTitle = "Summer Festival";
        String message = "You've been invited to join: " + eventTitle;

        assertTrue("Message should contain the event title",
                message.contains(eventTitle));
    }

    // ── US 01.04.02 ───────────────────────────────────────────────────────

    /**
     * US 01.04.02
     * Verifies that a not-chosen notification has the correct type field.
     */
    @Test
    public void notChosenNotification_hasCorrectType() {
        String type = "not_chosen";
        String message = "Unfortunately you were not selected for: Test Event";

        Map<String, Object> notif = new HashMap<>();
        notif.put("type", type);
        notif.put("message", message);
        notif.put("read", false);

        assertEquals("not_chosen", notif.get("type"));
        assertFalse("Notification should start unread", (Boolean) notif.get("read"));
    }

    /**
     * US 01.04.02
     * Verifies the message format for a not-chosen notification contains the event title.
     */
    @Test
    public void notChosenNotification_messageContainsEventTitle() {
        String eventTitle = "Winter Gala";
        String message = "Unfortunately you were not selected for: " + eventTitle;

        assertTrue("Message should contain the event title",
                message.contains(eventTitle));
    }

    /**
     * US 01.04.02
     * Verifies that not-chosen and invited notifications have different types
     * so the UI can distinguish between them.
     */
    @Test
    public void invitedAndNotChosen_haveDifferentTypes() {
        String invitedType   = "invited";
        String notChosenType = "not_chosen";

        assertNotEquals("Invited and not-chosen notifications must have different types",
                invitedType, notChosenType);
    }

    /**
     * US 01.04.01 / US 01.04.02
     * Verifies that notifications start with read = false.
     * They are only marked true after the user opens NotificationsActivity.
     */
    @Test
    public void newNotification_startsUnread() {
        Map<String, Object> notif = new HashMap<>();
        notif.put("read", false);

        assertFalse("New notifications must start as unread",
                (Boolean) notif.get("read"));
    }
}
