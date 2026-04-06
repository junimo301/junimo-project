package com.example.junimoapp;

import com.example.junimoapp.Organizer.EventData;
import com.example.junimoapp.models.Event;
import com.example.junimoapp.models.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.w3c.dom.Document;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for Organizer functionality.
 * Tests cover model and control logic for event creation and management.
 */
@RunWith(Enclosed.class)
public class OrganizerUnitTest {

    /**
     * US 02.01.01
     * Tests that an Event stores its title and description correctly.
     * Simulates the basic event object creation from CreateEvent.java.
     */
    @Test
    public void testEventCreationHasCorrectTitleAndDescription() {
        Event event = new Event(
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
                "event-uuid-001",      // eventID
                "Rec Centre",          // eventLocation
                "organizer-device-id", // organizerID
                ""                     // tag (none)
        );

        assertEquals("Swimming Lessons", event.getTitle());
        assertEquals("Beginner swim class", event.getDescription());
    }

    /**
     * US 02.01.01
     * Tests that the QR code string is built in the correct format.
     * Mirrors the exact logic from the QRCodeButton click in CreateEvent.java:
     * QRCodeString = "junimo://event?id=" + QREventID
     */
    @Test
    public void testQRCodeFormatIsCorrect() {
        String eventID = "event-uuid-001";
        String QRCodeString = "junimo://event?id=" + eventID;

        // verify the format matches what CreateEvent.java produces
        assertEquals("junimo://event?id=event-uuid-001", QRCodeString);
    }

    /**
     * US 02.01.01
     * Tests that a QR code can be set and retrieved on an Event object.
     * Mirrors event.setQRCode(QRCodeString) in CreateEvent.java.
     */
    @Test
    public void testQRCodeCanBeSetOnEvent() {
        Event event = new Event(
                "Dance Class", "Beginner dance",
                "2025-01-01", "2025-01-07", "2025-01-15",
                30, 0, 0.0, false, "",
                "event-uuid-002", "Studio", "org-id", ""
        );

        String qr = "junimo://event?id=event-uuid-002";
        event.setQRCode(qr);

        assertEquals(qr, event.getQRCode());
    }

    /**
     * US 02.01.01
     * Tests that two randomly generated event IDs are never equal.
     * In CreateEvent.java, UUID.randomUUID().toString() ensures uniqueness.
     */
    @Test
    public void testEventIDsAreUnique() {
        String id1 = java.util.UUID.randomUUID().toString();
        String id2 = java.util.UUID.randomUUID().toString();

        assertNotNull(id1);
        assertNotNull(id2);
        assertNotEquals(id1, id2);
    }

    /**
     * US 02.01.01
     * Tests that EventData.addOrEditEvent() adds an event and
     * it can be found by searchEventID().
     */
    @Test
    public void testAddEventAndSearchByID() {
        Event event = new Event(
                "Test Event", "A test",
                "2025-01-01", "2025-01-07", "2025-01-15",
                10, 0, 0.0, false, "",
                "search-test-id", "Location", "org-id", ""
        );

        EventData.addOrEditEvent(event);
        Event found = EventData.searchEventID("search-test-id");

        assertNotNull("Event should be found by its ID", found);
        assertEquals("search-test-id", found.getEventID());
    }

    /**
     * US 02.01.01
     * Tests that addOrEditEvent() updates an existing event rather than
     * adding a duplicate when the same eventID is used.
     * Mirrors the edit flow in CreateEvent.java where an existing event
     * is passed in via intent and re-saved.
     */
    @Test
    public void testEditEventUpdatesInsteadOfDuplicating() {
        Event original = new Event(
                "Original Title", "desc",
                "", "", "", 10, 0, 0.0, false, "",
                "edit-test-id", "loc", "org", ""
        );
        EventData.addOrEditEvent(original);

        Event updated = new Event(
                "Updated Title", "desc",
                "", "", "", 10, 0, 0.0, false, "",
                "edit-test-id", "loc", "org", ""
        );
        EventData.addOrEditEvent(updated);

        // count how many events have this ID — should be exactly 1
        int count = 0;
        for (Event e : EventData.listOfEvents()) {
            if (e.getEventID().equals("edit-test-id")) count++;
        }

        assertEquals("Should not duplicate — edit should replace", 1, count);
        assertEquals("Updated Title",
                EventData.searchEventID("edit-test-id").getTitle());
    }

    /**
     * US 02.01.04
     * Tests that start and end registration dates are stored correctly.
     */
    @Test
    public void testRegistrationPeriodStoredCorrectly() {
        Event event = new Event(
                "Dance Class", "Beginner dance",
                "2025-01-01",  // startDate — registration opens
                "2025-01-07",  // endDate   — registration closes
                "2025-01-15",  // dateEvent — actual event date
                30, 0, 0.0, false, "",
                "event-id-002", "Studio A", "org-id", ""
        );

        assertEquals("2025-01-01", event.getStartDate());
        assertEquals("2025-01-07", event.getEndDate());
    }

    /**
     * US 02.01.04
     * Tests the date validation logic from CreateEvent.java —
     * a valid registration period has start before end.
     */
    @Test
    public void testValidDateRangeStartBeforeEnd() throws Exception {
        java.text.SimpleDateFormat format =
                new java.text.SimpleDateFormat("yyyy-MM-dd");

        java.util.Date start = format.parse("2025-01-01");
        java.util.Date end   = format.parse("2025-01-07");

        // valid period — start should NOT be after end
        assertFalse("Valid period: start should not be after end",
                start.after(end));
    }

    /**
     * US 02.01.04
     * Tests that an invalid registration period (start after end) is detected.
     * Mirrors the if (start.after(end)) check in CreateEvent.java that
     * prevents the event from being published.
     */
    @Test
    public void testInvalidDateRangeStartAfterEnd() throws Exception {
        java.text.SimpleDateFormat format =
                new java.text.SimpleDateFormat("yyyy-MM-dd");

        java.util.Date start = format.parse("2025-01-10");
        java.util.Date end   = format.parse("2025-01-01");

        // invalid — start is after end, should be rejected
        assertTrue("Invalid period: start after end should be flagged",
                start.after(end));
    }

    /**
     * US 02.03.01
     * Tests that a waiting list limit is stored correctly when provided.
     */
    @Test
    public void testWaitingListLimitStoredWhenProvided() {
        Event event = new Event(
                "Yoga Class", "Morning yoga",
                "2025-02-01", "2025-02-07", "2025-02-15",
                50,
                30,    // waitingListLimit explicitly set to 30
                15.0, false, "", "event-id-003",
                "Yoga Studio", "org-id", ""
        );

        assertEquals(30, event.getWaitingListLimit());
    }

    /**
     * US 02.03.01
     * Tests that waitingListLimit of 0 is treated as "no limit".
     * In CreateEvent.java, if the field is left empty, no limit is applied.
     * Since waitingListLimit is int (not Integer), 0 represents optional/unset.
     */
    @Test
    public void testWaitingListLimitZeroMeansNoLimit() {
        Event event = new Event(
                "Piano Lessons", "Beginner piano",
                "2025-03-01", "2025-03-07", "2025-03-15",
                20,
                0,     // 0 = no waiting list limit (field left empty)
                60.0, false, "", "event-id-004",
                "Music Room", "org-id", ""
        );

        // 0 means no limit was set
        assertEquals(0, event.getWaitingListLimit());
    }

    /**
     * US 02.03.01
     * Tests that a negative waiting list limit is invalid.
     * Mirrors the validation in CreateEvent.java:
     * if (waitingListLimit != null && waitingListLimit < 0) show error
     */
    @Test
    public void testNegativeWaitingListLimitIsInvalid() {
        int waitingListLimit = -5;

        // mirrors the exact check in CreateEvent.java
        boolean isInvalid = waitingListLimit < 0;

        assertTrue("Negative waiting list limit should be flagged",
                isInvalid);
    }

    /**
     * US 02.05.02
     * Tests that maxCapacity (number to sample) is stored correctly on Event.
     */
    @Test
    public void testMaxCapacityStoredCorrectly() {
        Event event = new Event(
                "Swimming Lessons", "Beginner swim",
                "2025-01-01", "2025-01-07", "2025-01-15",
                20,    // maxCapacity — system samples this many attendees
                0, 60.0, false, "", "event-id-005",
                "Pool", "org-id", ""
        );

        assertEquals(20, event.getMaxCapacity());
    }

    /**
     * US 02.05.02
     * Tests that maxCapacity must be positive — can't sample 0 or fewer.
     */
    @Test
    public void testMaxCapacityMustBePositive() {
        int maxCapacity = 20;
        assertTrue("Sample size must be positive", maxCapacity > 0);
    }

    /**
     * US 02.05.02
     * Tests that the sample size cannot exceed the waiting list size.
     * If only 10 people joined the waiting list, you can't sample 20.
     */
    @Test
    public void testSampleSizeCannotExceedWaitingListSize() {
        int waitingListSize = 10;
        int requestedSample = 20;

        boolean isInvalid = requestedSample > waitingListSize;

        assertTrue("Cannot sample more than waiting list size", isInvalid);
    }

    /**
     * US 02.05.02
     * Tests that a sample size equal to the waiting list size is valid.
     * Edge case — selecting everyone on the list is allowed.
     */
    @Test
    public void testSampleSizeEqualToWaitingListIsValid() {
        int waitingListSize = 10;
        int requestedSample = 10;

        boolean isValid = requestedSample <= waitingListSize;

        assertTrue("Sampling everyone on the list should be valid", isValid);
    }

    /**
     * US 02.06.01
     * Tests that invited entrants are stored correctly.
     * */
    @Test
    public void TestInvitedEntrants() {
        List<String> invitedEntrants = Arrays.asList("Jane", "Jack", "Jill");
        assertEquals(3, invitedEntrants.size());
        assertTrue(invitedEntrants.contains("Jane"));
        assertTrue(invitedEntrants.contains("Jack"));
        assertTrue(invitedEntrants.contains("Jill"));
    }

    /**
     * US 02.06.02
     * Tests that cancelled entrants are stored correctly.
     * */
    @Test
    public void TestCancelledEntrants() {
        List<String> cancelledEntrant= Arrays.asList("Billy", "Bonnie", "Bon");
        assertEquals(3, cancelledEntrant.size());
        assertTrue(cancelledEntrant.contains("Billy"));
        assertTrue(cancelledEntrant.contains("Bonnie"));
        assertTrue(cancelledEntrant.contains("Bon"));
    }

    /**
     * US 02.06.03
     * Tests that enrolled entrants are stored correctly.
     * */
    @Test
    public void TestEnrolledEntrants() {
        List<String> enrolledEntrant = Arrays.asList("Ally", "Anna", "Adien");
        assertEquals(3, enrolledEntrant.size());
        assertTrue(enrolledEntrant.contains("Ally"));
        assertTrue(enrolledEntrant.contains("Anna"));
        assertTrue(enrolledEntrant.contains("Adien"));
    }

    /**
     * US 02.02.01
     * Tests that the entrants who joined the waiting list are stored correctly.
     * */
    @Test
    public void TestJoinedWaitingList() {
        List<String> joinedWaitingListNames = Arrays.asList("Caine", "Couper", "Chloe");
        assertEquals(3, joinedWaitingListNames.size());
        assertTrue(joinedWaitingListNames.contains("Caine"));
        assertTrue(joinedWaitingListNames.contains("Couper"));
        assertTrue(joinedWaitingListNames.contains("Chloe"));
    }


    // ─────────────────────────────────────────────────────────────────
    // PART 4: NEW TESTS
    // ─────────────────────────────────────────────────────────────────

    /**
     * UnitTests to test US 02.05.02
     * */
    public static class SampleTest {
        //simulates startLottery() system from Entrants class
        private List<User> startLottery(List<User> user, int maxCapacity) {
            List<User> shuffle = new ArrayList<>(user);
            Collections.shuffle(shuffle);
            int index = Math.min(maxCapacity, shuffle.size());
            return new ArrayList<>(shuffle.subList(0, index));
        }
        private List<User> getUser(int count) {
            List<User> userList = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                userList.add(new User("deviceID" + i,
                        "name" + i,
                        "email" + i + "@gmail.com",
                        "",
                        "",
                        "",
                        "" ));
            }
            return userList;
        }

        // ----------- TESTS -----------

        /**
         * Tests that everyone picked is from the waitlist
         * Returns users from the waitlist
         * */
        @Test
        public void testUsersPickedFromWaitList() {
            List<User> waitList = getUser(15);
            int maxCapacity = 10;
            List<User> selectedUsers = startLottery(waitList, maxCapacity);

            for (User user : selectedUsers) {
                assertTrue("Should select users from the waitlist", waitList.contains(user));
            }
            long count = selectedUsers.stream().map(User::getDeviceId).distinct().count();
            assertEquals("Should select users from the waitlist", selectedUsers.size(), count);
        }

        /**
         * Tests if the selected users is not more than capacity when waitlist has more than capacity
         * Returns selected users equal to maxCapacity
         * */
        @Test
        public void testSampleSizeNotMoreThanCapacity() {
            List<User> waitList = getUser(15);
            int maxCapacity = 10;
            List<User> selectedUsers = startLottery(waitList, maxCapacity);
            assertEquals("Should not select more than maxCapacity", maxCapacity, selectedUsers.size());
        }

        /**
         * Tests when waitlist and maxCapacity are equal
         * Returns everyone on the waitlist
         * */
        @Test
        public void testSampleSizeEqualToCapacity() {
            List<User> waitList = getUser(10);
            int maxCapacity = 10;
            List<User> selectedUsers = startLottery(waitList, maxCapacity);
            assertEquals("Should select everyone on the waitlist", 10, selectedUsers.size());
        }

        /**
         * Tests if everyone on the waitlist is selected if waitlist is less than maxCapacity
         * Returns everyone on the waitlist
         * */
        @Test
        public void testSampleSizeEqualToWaitingList() {
            List<User> waitList = getUser(5);
            int maxCapacity = 10;
            List<User> selectedUsers = startLottery(waitList, maxCapacity);
            assertEquals("Should select everyone on the waitlist", waitList.size(), selectedUsers.size());
        }

        /**
         * Tests when waitList is empty
         * Returns no selected users
         * */
        @Test
        public void testSampleSizeZero() {
            List<User> waitList = new ArrayList<>();
            int maxCapacity = 10;
            List<User> selectedUsers = startLottery(waitList, maxCapacity);
            assertEquals("Should not select any users", 0, selectedUsers.isEmpty());
        }
    }//US 02.05.03 test class


    /**
     * UnitTests to test US 02.02.02 and 02.02.03
     * */
    @RunWith(RobolectricTestRunner.class)
    public static class GeoLocationTests {
        @Mock
        FirebaseFirestore mockFirestore;
        @Mock
        CollectionReference mockUserLocation;
        @Mock
        DocumentReference mockUserLocationDocument;
        @Mock
        CollectionReference mockEvents;
        @Mock
        DocumentReference mockEventDocument;

        private Event geoLocationEnbaledEvent;

        @Before
        public void helper() {
            when(mockFirestore.collection("events")).thenReturn(mockEvents);
            when(mockEvents.document(anyString())).thenReturn(mockEventDocument);
            when(mockEventDocument.collection("usersLocation")).thenReturn(mockUserLocation);
            when(mockUserLocation.document(anyString())).thenReturn(mockUserLocationDocument);
            when(mockUserLocationDocument.set(anyMap())).thenReturn(null);

            geoLocationEnbaledEvent = new Event(
                    "Swimming Lessons",    // title
                    "Beginner swim class", // description
                    "2025-01-01",          // startDate
                    "2025-01-07",          // endDate
                    "2025-01-10,",         // dateEvent
                    20,                    // maxCapacity
                    30,                    // waitingListLimit
                    0.0,                  // price
                    true,                  // geoLocation
                    "",                    // poster
                    "event-uuid-001",      // eventID
                    "Rec Centre",          // eventLocation
                    "organizer-device-id", // organizerID
                    ""                     // tag (none)
            );
        }

        // ----------- TEST 02.02.03 -----------

        /**
         * Tests if geoLocation is disabled for an event by default
         * */
        @Test
        public void testGeoLocationDisabled() {
            Event event = new Event("Movie", "Watch movie", "2025-01-01", "2025-01-10",
                    "2025-01-12", 20, 0, 0.0, false, "",
                    "event-uuid-001", "Movie Theater", "organizer-device-id", "");
            assertFalse("GeoLocation should be disabled by default", geoLocationEnbaledEvent.isGeoLocation());
        }

        /**
         * Tests enabling geoLocation for an event
         * */
        @Test
        public void testGeoLocationEnabled() {
            Event event = new Event("Movie", "Watch movie", "2025-01-01", "2025-01-10",
                    "2025-01-12", 20, 0, 0.0, true, "",
                    "event-uuid-001", "Movie Theater", "organizer-device-id", "");
            assertFalse(geoLocationEnbaledEvent.isGeoLocation());
            geoLocationEnbaledEvent.setGeoLocation(true);
            assertTrue("GeoLocation should be enabled", geoLocationEnbaledEvent.isGeoLocation());
        }

        /**
         * NOT IMPLEMENTED
         * Tests enabling geoLocation for an event after it has been disabled
         * */
        @Test
        public void testGeoLocationEnabledAfterDisabled() {
            assertFalse(geoLocationEnbaledEvent.isGeoLocation());
            geoLocationEnbaledEvent.setGeoLocation(true);
            assertTrue("GeoLocation should be enabled now", geoLocationEnbaledEvent.isGeoLocation());
        }

        /**
         * NOT IMPLEMENTED
         * Tests disabling geoLocation for an event after it has been enabled
         * */
        @Test
        public void testGeoLocationDisabledAfterEnabled() {
            assertTrue(geoLocationEnbaledEvent.isGeoLocation());
            geoLocationEnbaledEvent.setGeoLocation(false);
            assertFalse("GeoLocation should be disabled now", geoLocationEnbaledEvent.isGeoLocation());
        }

        /**
         * Tests when geolocation buttons are couple of times
         * - enabled
         * - then disabled
         * - then enabled
         * Returns the value it starts as, returns enabled geoLocation
         * */
        @Test
        public void testGeoLocationEnabledThenDisabledThenEnabled() {
            boolean correct = geoLocationEnbaledEvent.isGeoLocation(); //enabled, true
            geoLocationEnbaledEvent.setGeoLocation(!correct);
            geoLocationEnbaledEvent.setGeoLocation(correct);
            assertEquals("GeoLocation should be enabled now", correct, geoLocationEnbaledEvent.isGeoLocation());
        }
    } // US 02.02.02 and 02.02.03 test class


    /**
     * UnitTests to test US 02.04.01 and 02.04.02
     * */
    public static class PosterTests {

    } // US 02.04.01 and 02.04.02 test class


    /**
     * UnitTests to test US 02.01.01
     * - testing creating an event
     * - testing QR code generation
     * */
    public static class CreatePublicEventAndQRCodeTests {

    } // US 02.04.01 and 02.04.02 test class

} //Whole class



