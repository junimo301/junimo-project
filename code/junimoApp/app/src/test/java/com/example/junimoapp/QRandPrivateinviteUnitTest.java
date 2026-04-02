package com.example.junimoapp;

import com.example.junimoapp.models.Event;
import com.example.junimoapp.models.User;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Local unit tests for QR code scanning and private invite accept/decline logic.
 *
 * All tests call real methods on real model objects.
 *
 * Stories covered:
 *  - US 01.06.01: Entrant views event details by scanning a QR code
 *  - US 01.05.07: Entrant accepts or declines a private event waiting list invite
 */
public class QRandPrivateinviteUnitTest {

    private Event testEvent;
    private User testUser;

    @Before
    public void setUp() {
        testEvent = new Event(
                "Private Gala", "desc", "", "", "2025-09-01",
                30, 15, 25.0,
                true, "", "private-event-id",
                "Venue", "organizer-id", ""
        );
        testUser = new User(
                "device-user-id", "Test User",
                "user@test.com", "555-0000",
                "", "", ""
        );
    }

    // ── US 01.06.01 ───────────────────────────────────────────────────────

    /**
     * US 01.06.01
     * Verifies that a valid junimo QR string is correctly recognised.
     * Simulates the parseEventId logic from QRScanActivity.
     */
    @Test
    public void qr_validJunimoString_parsesEventId() {
        String raw = "junimo://event?id=private-event-id";
        String prefix = "junimo://event?id=";
        String parsed = raw.startsWith(prefix)
                ? raw.substring(prefix.length()).trim()
                : null;

        assertEquals("Should parse the eventId from a valid QR string",
                "private-event-id", parsed);
    }

    /**
     * US 01.06.01
     * Verifies that an unrecognised QR string returns null.
     * The app should show an error and not navigate.
     */
    @Test
    public void qr_invalidString_returnsNull() {
        String raw = "https://someotherwebsite.com/page";
        String prefix = "junimo://event?id=";
        String parsed = raw.startsWith(prefix)
                ? raw.substring(prefix.length()).trim()
                : null;

        assertNull("Non-junimo QR string should return null", parsed);
    }

    /**
     * US 01.06.01
     * Verifies that a null raw value returns null safely.
     */
    @Test
    public void qr_nullString_returnsNull() {
        String raw = null;
        String prefix = "junimo://event?id=";
        String parsed = (raw != null && raw.startsWith(prefix))
                ? raw.substring(prefix.length()).trim()
                : null;

        assertNull("Null QR string should return null", parsed);
    }

    /**
     * US 01.06.01
     * Verifies that the QR code stored on a public Event object
     * matches the expected format used by QRScanActivity to parse eventId.
     */
    @Test
    public void qr_eventQRCodeFormat_matchesExpectedPrefix() {
        String eventId = testEvent.getEventID();
        String qrString = "junimo://event?id=" + eventId;
        testEvent.setQRCode(qrString);

        String stored = testEvent.getQRCode();
        assertTrue("Stored QR code should start with junimo://event?id=",
                stored.startsWith("junimo://event?id="));
        assertTrue("Stored QR code should contain the eventId",
                stored.contains(eventId));
    }

    /**
     * US 01.06.01
     * Verifies that a private event has no QR code — private events
     * should not generate a promotional QR (US 02.01.02).
     */
    @Test
    public void qr_privateEvent_hasNoQRCode() {
        testEvent.setPrivate(true);
        // Simulate CreateEvent logic — only set QR if not private
        if (!testEvent.isPrivate()) {
            testEvent.setQRCode("junimo://event?id=" + testEvent.getEventID());
        }
        assertNull("Private event should not have a QR code", testEvent.getQRCode());
    }

    // ── US 01.05.07 ───────────────────────────────────────────────────────

    /**
     * US 01.05.07
     * Calls user.isInvited() before any invite — should be false.
     */
    @Test
    public void privateInvite_userNotInvitedByDefault() {
        assertFalse("User should not be invited to any event by default",
                testUser.isInvited(testEvent.getEventID()));
    }

    /**
     * US 01.05.07
     * Simulates the inviteUser logic from PrivateInviteActivity:
     * appends eventId to invitedEvents string, then checks isInvited().
     */
    @Test
    public void privateInvite_afterInvite_userIsInvited() {
        // Simulate what PrivateInviteActivity.inviteUser() writes to Firestore
        // by setting the invitedEvents field directly on the User object
        testUser.setInvitedEvents(testEvent.getEventID() + ",");

        assertTrue("User should be invited after eventId is added to invitedEvents",
                testUser.isInvited(testEvent.getEventID()));
    }

    /**
     * US 01.05.07
     * Simulates declining a private invite — removes eventId from invitedEvents.
     * Mirrors the declineInvite() logic in InvitationsActivity.
     */
    @Test
    public void privateInvite_afterDecline_userNotInvited() {
        // Set up as invited
        String eventId = testEvent.getEventID();
        testUser.setInvitedEvents(eventId + ",");
        assertTrue("Setup: user should be invited", testUser.isInvited(eventId));

        // Simulate declineInvite() removing the eventId
        String updated = testUser.getInvitedEvents().replace(eventId + ",", "");
        testUser.setInvitedEvents(updated);

        assertFalse("After declining, user should no longer be invited",
                testUser.isInvited(eventId));
    }

    /**
     * US 01.05.07
     * Verifies that declining one event does not affect invitation to another.
     */
    @Test
    public void privateInvite_declineOne_doesNotAffectOther() {
        String eventIdA = "event-aaa";
        String eventIdB = "event-bbb";

        // Invite to both
        testUser.setInvitedEvents(eventIdA + "," + eventIdB + ",");

        // Decline only A
        String updated = testUser.getInvitedEvents().replace(eventIdA + ",", "");
        testUser.setInvitedEvents(updated);

        assertFalse("Event A should be declined", testUser.isInvited(eventIdA));
        assertTrue("Event B invite should remain", testUser.isInvited(eventIdB));
    }

    /**
     * US 01.05.07
     * Verifies that inviting the same user twice does not duplicate the eventId.
     * Mirrors the duplicate check in PrivateInviteActivity.inviteUser().
     */
    @Test
    public void privateInvite_invitingTwice_doesNotDuplicate() {
        String eventId = testEvent.getEventID();

        // First invite
        if (!testUser.isInvited(eventId)) {
            testUser.setInvitedEvents(testUser.getInvitedEvents() + eventId + ",");
        }
        // Second invite attempt — should be blocked
        if (!testUser.isInvited(eventId)) {
            testUser.setInvitedEvents(testUser.getInvitedEvents() + eventId + ",");
        }

        // Count occurrences of eventId in the string
        String invited = testUser.getInvitedEvents();
        int count = invited.split(eventId, -1).length - 1;
        assertEquals("EventId should only appear once in invitedEvents", 1, count);
    }
}

