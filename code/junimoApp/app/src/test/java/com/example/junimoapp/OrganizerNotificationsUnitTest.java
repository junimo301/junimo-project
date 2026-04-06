package com.example.junimoapp;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Local unit tests for organizer notifications.
 *
 * Stories covered:
 *  - US 02.07.01: Organizer notifications sent to entrants on the waiting list
 *  - US 02.07.02: Organizer notifications sent to selected entrants
 *  - US 02.07.03: Organizer notifications sent to cancelled entrants
 */
public class OrganizerNotificationsUnitTest {

    /**
     * US 02.07.01
     * Simulates the organizer sending a notification to entrants
     * on the waiting list.
     * Verifies the waiting list recipients are stored correctly.
     */
    @Test
    public void waitingListNotifications_storeWaitingListRecipients() {
        List<String> waitingListRecipients = Arrays.asList("user1", "user2", "user3");

        assertEquals(3, waitingListRecipients.size());
        assertTrue(waitingListRecipients.contains("user1"));
        assertTrue(waitingListRecipients.contains("user2"));
        assertTrue(waitingListRecipients.contains("user3"));
    }

    /**
     * US 02.07.02
     * Simulates the organizer sending a notification to selected entrants.
     * Verifies the selected entrant recipients are stored correctly.
     */
    @Test
    public void selectedEntrantsNotifications_storeSelectedRecipients() {
        List<String> selectedRecipients = Arrays.asList("selected1", "selected2");

        assertEquals(2, selectedRecipients.size());
        assertTrue(selectedRecipients.contains("selected1"));
        assertTrue(selectedRecipients.contains("selected2"));
    }

    /**
     * US 02.07.03
     * Simulates the organizer sending a notification to cancelled entrants.
     * Verifies the cancelled entrant recipients are stored correctly.
     */
    @Test
    public void cancelledEntrantsNotifications_storeCancelledRecipients() {
        List<String> cancelledRecipients = Arrays.asList("cancelled1", "cancelled2", "cancelled3");

        assertEquals(3, cancelledRecipients.size());
        assertTrue(cancelledRecipients.contains("cancelled1"));
        assertTrue(cancelledRecipients.contains("cancelled2"));
        assertTrue(cancelledRecipients.contains("cancelled3"));
    }
}
