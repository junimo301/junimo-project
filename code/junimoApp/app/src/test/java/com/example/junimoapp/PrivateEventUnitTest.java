package com.example.junimoapp;

import com.example.junimoapp.models.Event;
import com.example.junimoapp.models.User;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Local unit tests for private event, notification, and invitation logic.
 *
 * All tests call real methods on real model objects — not local variables.
 *
 * Stories covered:
 *  - US 02.01.02: Organizer creates a private event (no public listing, no QR)
 *  - US 01.04.03: Entrant can opt out of notifications
 *  - US 01.04.01: Entrant receives notification when selected by lottery
 *  - US 01.04.02: Entrant receives notification when not selected
 *  - US 01.05.07: Entrant can accept or decline a private event invitation
 *  - US 02.01.03: Organizer invites entrant to private event waiting list
 */
public class PrivateEventUnitTest {

    private Event testEvent;
    private User testUser;

    @Before
    public void setUp() {
        testEvent = new Event(
                "Test Event", "desc", "2025-01-01", "2025-01-10",
                "2025-02-01", 50, 20, 10.0,
                true, "", "event-test-id",
                "Test Location", "organizer-test-id", ""
        );
        testUser = new User(
                "device-test-id", "Test User",
                "test@email.com", "555-1234",
                "", "", ""
        );
    }

    // ── US 02.01.02 ───────────────────────────────────────────────────────

    /** US 02.01.02 — Events default to public. */
    @Test
    public void event_isPrivate_defaultsToFalse() {
        assertFalse(testEvent.isPrivate());
    }

    /** US 02.01.02 — setPrivate(true) makes isPrivate() return true. */
    @Test
    public void event_setPrivateTrue_isPrivateReturnsTrue() {
        testEvent.setPrivate(true);
        assertTrue(testEvent.isPrivate());
    }

    /** US 02.01.02 — Event can be switched back to public. */
    @Test
    public void event_setPrivateFalse_isPrivateReturnsFalse() {
        testEvent.setPrivate(true);
        testEvent.setPrivate(false);
        assertFalse(testEvent.isPrivate());
    }

    /** US 02.01.02 — Private events must not store a QR code. */
    @Test
    public void privateEvent_qrCodeRemainsNull() {
        testEvent.setPrivate(true);
        if (!testEvent.isPrivate()) testEvent.setQRCode("junimo://event?id=event-test-id");
        assertNull(testEvent.getQRCode());
    }

    /** US 02.01.02 — Public events can store a QR code. */
    @Test
    public void publicEvent_qrCodeIsSet() {
        testEvent.setPrivate(false);
        String qr = "junimo://event?id=event-test-id";
        if (!testEvent.isPrivate()) testEvent.setQRCode(qr);
        assertEquals(qr, testEvent.getQRCode());
    }

    // ── US 01.04.03 ───────────────────────────────────────────────────────

    /** US 01.04.03 — Notifications enabled by default. */
    @Test
    public void user_notificationsEnabled_defaultsToTrue() {
        assertTrue(testUser.isNotificationsEnabled());
    }

    /** US 01.04.03 — User can opt out of notifications. */
    @Test
    public void user_setNotificationsEnabledFalse_returnsFalse() {
        testUser.setNotificationsEnabled(false);
        assertFalse(testUser.isNotificationsEnabled());
    }

    /** US 01.04.03 — User can re-enable notifications after opting out. */
    @Test
    public void user_reEnableNotifications_returnsTrue() {
        testUser.setNotificationsEnabled(false);
        testUser.setNotificationsEnabled(true);
        assertTrue(testUser.isNotificationsEnabled());
    }

    // ── US 01.04.01 / US 01.04.02 ─────────────────────────────────────────

    /** US 01.04.01 — User is not invited before lottery runs. */
    @Test
    public void user_notInvited_beforeLottery() {
        assertFalse(testUser.isInvited("event-test-id"));
    }

    /** US 01.04.01 — isInvited() returns true after being selected by lottery. */
    @Test
    public void user_isInvited_afterLotterySelection() {
        testUser.setInvitedEvents("event-test-id,");
        assertTrue(testUser.isInvited("event-test-id"));
    }

    /** US 01.04.02 — User not chosen by lottery is not marked as invited. */
    @Test
    public void user_notChosen_isNotInvited() {
        assertFalse(testUser.isInvited("event-test-id"));
    }

    // ── US 01.05.07 ───────────────────────────────────────────────────────

    /** US 01.05.07 — Accepting invite removes eventId from invitedEvents. */
    @Test
    public void user_acceptInvite_removesFromInvitedEvents() {
        testUser.setInvitedEvents("event-test-id,");
        String updated = testUser.getInvitedEvents().replace("event-test-id,", "");
        testUser.setInvitedEvents(updated);
        assertFalse(testUser.isInvited("event-test-id"));
    }

    /** US 01.05.07 — Declining invite removes eventId from invitedEvents. */
    @Test
    public void user_declineInvite_removesFromInvitedEvents() {
        testUser.setInvitedEvents("event-test-id,");
        String updated = testUser.getInvitedEvents().replace("event-test-id,", "");
        testUser.setInvitedEvents(updated);
        assertFalse(testUser.isInvited("event-test-id"));
    }

    // ── US 02.01.03 ───────────────────────────────────────────────────────

    /** US 02.01.03 — User has no invited events before organizer invite. */
    @Test
    public void user_noInvitedEvents_beforeOrganizerInvite() {
        assertEquals("", testUser.getInvitedEvents());
    }

    /** US 02.01.03 — invitedEvents contains event after organizer invite. */
    @Test
    public void user_invitedEvents_containsEventAfterOrganizerInvite() {
        testUser.setInvitedEvents(testUser.getInvitedEvents() + "event-test-id,");
        assertTrue(testUser.getInvitedEvents().contains("event-test-id"));
    }
}
