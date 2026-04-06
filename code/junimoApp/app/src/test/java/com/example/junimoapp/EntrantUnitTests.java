package com.example.junimoapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.junimoapp.models.Event;
import com.example.junimoapp.models.User;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * US 02.06.02 As an organizer I want to see a list of all the cancelled entrants.
 * US 02.06.03 As an organizer I want to see a final list of entrants who enrolled for the event.
 * US 02.06.04 As an organizer I want to cancel entrants that did not sign up for the event
 * */
public class EntrantUnitTests {

    private final String eventID = "event-uuid-001";
    private final Event event = new Event(
            "Swimming Lessons",    // title
            "Beginner swim class", // description
            "2025-01-01",          // startDate
            "2025-01-07",          // endDate
            "2025-01-15",          // dateEvent
            20,                    // maxCapacity
            0,                     // waitingListLimit (0 = no limit)
            60.0,                  // price
            false,                  // geoLocation
            "",                    // poster
            eventID,      // eventID
            "Rec Centre",          // eventLocation
            "organizer-device-id", // organizerID
            ""                     // tag (none)
    );

    private List<User> makeUser() {
        List<User> users = new ArrayList<>();
        users.add(new User("decive1", "farzana", "farzana@gmail.com", "", "", "", ""));
        users.add(new User("decive2", "Ayema", "ayema@gmail.com", "", "", "", ""));
        users.add(new User("decive3", "Anica", "anica@gmail.com", "", "", "", ""));
        return users;
    }

    /**
     * tests cancelled entrants
     */
    @Test
    public void testCancelledEntrants() {
        List<User> users = makeUser();
        users.get(0).cancelUser(event);
        List<User> cancelledEntrants = new ArrayList<>();
        for (User user : users) {
            if (user.isCancelled(eventID)) cancelledEntrants.add(user);
        }
        assertEquals("Should have a cancelled entrant", 1, cancelledEntrants.size());
    }

    /**
     * Tests cancelled entrants who did not sign up for the event
     *
     */
    @Test
    public void testCancelledEntrantsNotEnrolled() {
        List<User> users = makeUser();
        users.get(0).cancelUser(event);
        assertTrue("Farzana should be cancelled", users.get(0).isCancelled(eventID));
        assertFalse("Ayema should not be cancelled", users.get(1).isCancelled(eventID));
        assertFalse("Anica should not be cancelled", users.get(2).isCancelled(eventID));
    }

    /**
     * Tests enrolled entrants
     */
    @Test
    public void testEnrolledEntrants() {
        List<User> users = makeUser();
        assertEquals("Should have 3 users in enrolled", 3, users.size());
        assertFalse("Enrolled list should not be empty", users.isEmpty());

    }

}