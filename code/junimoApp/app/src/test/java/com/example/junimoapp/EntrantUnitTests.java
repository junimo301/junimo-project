package com.example.junimoapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

    private void disableFirebase(User user) {
        try {
            java.lang.reflect.Field dbField = User.class.getDeclaredField("db");
            dbField.setAccessible(true);
            dbField.set(user, null);

            java.lang.reflect.Field firebaseField = User.class.getDeclaredField("firebase");
            firebaseField.setAccessible(true);
            firebaseField.set(user, null);

            // prevent crash from invitedEventsList.remove()
            java.lang.reflect.Field listField = User.class.getDeclaredField("invitedEventsList");
            listField.setAccessible(true);
            listField.set(user, new ArrayList<>());

        } catch (Exception e) {
            fail("Reflection failed");
        }
    }
    private List<User> makeUser() {
        List<User> users = new ArrayList<>();
        User user1 = new User("decive1", "farzana","farzana@gmail.com", "", "", "", "");
        User user2 = new User("decive2", "Ayema","ayema@gmail.com", "", "", "", "");
        User user3 = new User("decive3", "Anica","anica@gmail.com", "", "", "", "");
        disableFirebase(user1);
        disableFirebase(user2);
        disableFirebase(user3);

        users.add(user1);
        users.add(user2);
        users.add(user3);
        return users;
    }

    /**
     * tests cancelled entrants
     * Simulates the cancellation of an entrant
     */
    @Test
    public void testCancelledEntrants() {
        List<User> users = makeUser();
        users.get(0).setInvitedEvents(eventID + ",");
        users.get(0).cancelUser(event);

        List<User> cancelledEntrants = new ArrayList<>();
        for (User user : users) {
            if (user.isCancelled(eventID)) cancelledEntrants.add(user);
        }
        assertEquals("Should have a cancelled entrant", 1, cancelledEntrants.size());
    }

    /**
     * Tests cancelling
     * */
    @Test
    public void testCancelledEntrantsNotEnrolled() {
        List<User> users = makeUser();
        users.get(0).setInvitedEvents(eventID + ",");
        users.get(0).cancelUser(event);

        assertTrue(users.get(0).isCancelled(eventID));
        assertFalse(users.get(1).isCancelled(eventID));
        assertFalse(users.get(2).isCancelled(eventID));
    }

    /**
     * Tests enrolled entrants
     */
    @Test
    public void testEnrolledEntrants() {
        List<User> users = makeUser();
        for (User user : users) {
            user.setInvitedEvents(eventID + ",");
        }
        int enrolled = 0;
        for (User user : users) {
            if (user.isInvited(eventID)) enrolled++;
        }
        assertEquals("Should have 3 enrolled entrants", 3, enrolled);
    }

}
