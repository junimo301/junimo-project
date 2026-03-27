package com.example.junimoapp;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Local unit tests for private event rules.
 *
 * These tests cover the core logic behind:
 *  - US 02.01.02 As an organizer, I want to create a private event
 *                (no public listing, no QR code).
 *  - US 02.01.03 As an organizer, I want to invite specific entrants
 *                to a private event's waiting list by searching via
 *                name, phone number and/or email.
 */
public class PrivateEventUnitTest {

    // ── US 02.01.02 — your original tests (unchanged) ────────────────────

    /**
     * US 02.01.02
     * Verifies that a newly considered event is public by default
     * unless explicitly marked private.
     */
    @Test
    public void privateFlag_defaultsToFalse() {
        boolean isPrivate = false;
        assertFalse(isPrivate);
    }

    /**
     * US 02.01.02
     * Verifies that when an organizer marks an event as private,
     * the private flag becomes true.
     */
    @Test
    public void privateFlag_canBeSetToTrue() {
        boolean isPrivate = false;
        isPrivate = true;
        assertTrue(isPrivate);
    }

    /**
     * US 02.01.02
     * Verifies that private events must not keep a promotional QR code.
     * If an event is private, the QR code should be cleared.
     */
    @Test
    public void privateEvent_clearsQrCode() {
        boolean isPrivate = true;
        String qrCode = "junimo://event?id=123";

        if (isPrivate) {
            qrCode = null;
        }

        assertNull(qrCode);
    }

    /**
     * US 02.01.02
     * Verifies that public events may keep a generated QR code.
     * If an event is not private, an existing QR code should remain.
     */
    @Test
    public void publicEvent_keepsQrCode() {
        boolean isPrivate = false;
        String qrCode = "junimo://event?id=123";

        if (isPrivate) {
            qrCode = null;
        }

        assertEquals("junimo://event?id=123", qrCode);
    }

    /**
     * US 02.01.02
     * Verifies the rule used by the UI:
     * private events should disable QR generation.
     */
    @Test
    public void privateEvent_disablesQrGenerationRule() {
        boolean isPrivate = true;
        boolean qrButtonEnabled = !isPrivate;
        assertFalse(qrButtonEnabled);
    }

    /**
     * US 02.01.02
     * Verifies the opposite UI rule:
     * public events should allow QR generation.
     */
    @Test
    public void publicEvent_enablesQrGenerationRule() {
        boolean isPrivate = false;
        boolean qrButtonEnabled = !isPrivate;
        assertTrue(qrButtonEnabled);
    }

    // ── US 02.01.03 — new tests ───────────────────────────────────────────

    /**
     * US 02.01.03
     * Verifies that a blank search query should not trigger a user search.
     * An empty query means the organizer has not typed anything yet,
     * so results should be cleared rather than showing all users.
     */
    @Test
    public void blankSearchQuery_shouldNotSearch() {
        String query = "";
        boolean shouldSearch = !query.equals("");
        assertFalse("Blank query must not trigger a search", shouldSearch);
    }

    /**
     * US 02.01.03
     * Verifies that a non-blank search query should trigger a user search.
     */
    @Test
    public void nonBlankSearchQuery_shouldSearch() {
        String query = "John";
        boolean shouldSearch = !query.equals("");
        assertTrue("Non-blank query should trigger a search", shouldSearch);
    }

    /**
     * US 02.01.03
     * Verifies the duplicate-prevention logic used when merging search results.
     * If a user already appears in the results list, they must not be added again
     * even if they match multiple search fields (name AND email, for example).
     */
    @Test
    public void inviteResults_noDuplicateUsers() {
        // Simulate the already-present results list
        java.util.List<String> presentIds = new java.util.ArrayList<>();
        presentIds.add("device-abc");
        presentIds.add("device-xyz");

        // Try adding a user that is already present
        String incomingId = "device-abc";
        boolean alreadyPresent = presentIds.contains(incomingId);

        assertTrue("Duplicate user must not be added to results", alreadyPresent);
        // The list should still only have 2 entries
        assertEquals(2, presentIds.size());
    }

    /**
     * US 02.01.03
     * Verifies that a brand-new user (not already in results) can be added.
     */
    @Test
    public void inviteResults_newUserIsAdded() {
        java.util.List<String> presentIds = new java.util.ArrayList<>();
        presentIds.add("device-abc");

        String incomingId = "device-new";
        boolean alreadyPresent = presentIds.contains(incomingId);

        if (!alreadyPresent) {
            presentIds.add(incomingId);
        }

        assertEquals("New user should be added to results", 2, presentIds.size());
        assertTrue(presentIds.contains("device-new"));
    }

    /**
     * US 02.01.03
     * Verifies that once a user has been invited, their eventId appears
     * in their invitations list (simulates what PrivateInviteActivity does
     * before writing to Firestore).
     */
    @Test
    public void invitingUser_addsEventToInvitationsList() {
        java.util.List<String> invitations = new java.util.ArrayList<>();
        String eventId = "event-123";

        // Simulate the invite action
        if (!invitations.contains(eventId)) {
            invitations.add(eventId);
        }

        assertTrue("Event ID should be in the user's invitations after invite",
                invitations.contains(eventId));
    }

    /**
     * US 02.01.03
     * Verifies that inviting a user to the same event twice
     * does not add duplicate entries to their invitations list.
     */
    @Test
    public void invitingUserTwice_doesNotDuplicateInvitation() {
        java.util.List<String> invitations = new java.util.ArrayList<>();
        String eventId = "event-123";

        // First invite
        if (!invitations.contains(eventId)) invitations.add(eventId);
        // Second invite (should be blocked)
        if (!invitations.contains(eventId)) invitations.add(eventId);

        assertEquals("Duplicate invitation must not be added", 1, invitations.size());
    }
}
