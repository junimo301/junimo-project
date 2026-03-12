package com.example.junimoapp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.example.junimoapp.admin.AdminUserAdapter;

/**
 * Unit tests for admin functionality.
 * These tests cover the model and logic used by the admin browse/delete features.
 */
public class AdminUnitTest {
    /**
     * US 03.04.01
     * - As an administrator, I want to be able to browse events.
     * Tests that an EventItem is created correctly with the expected field values.
     * This simulates what happens when we load an event from Firestore and
     * wrap it in an EventItem for the admin to browse.
     */
    @Test
    public void testEventItemCreation() {
        // create an EventItem the same way AdminBrowseEventsActivity does
        // when it reads a document from Firestore
        com.example.junimoapp.admin.AdminEventAdapter.EventItem event =
                new com.example.junimoapp.admin.AdminEventAdapter.EventItem(
                        "doc123",           // Firestore document ID
                        "Swimming Lessons", // event title
                        "Beginner swim"     // event description
                );

        // verify all fields were stored correctly
        assertEquals("doc123", event.documentId);
        assertEquals("Swimming Lessons", event.title);
        assertEquals("Beginner swim", event.description);
    }

    /**
     * US 03.04.01
     * Tests that when an event has no title or description (missing Firestore fields),
     * our placeholder logic correctly substitutes readable fallback strings.
     * This mirrors the null-handling code in AdminBrowseEventsActivity.loadEvents().
     */
    @Test
    public void testEventItemHandlesMissingFields() {
        // simulate what AdminBrowseEventsActivity does when fields are null
        String title = null;
        String description = null;

        // this is the exact same null-handling logic from loadEvents()
        if (title == null || title.isEmpty()) title = "(no title)";
        if (description == null || description.isEmpty()) description = "(no description)";

        // verify placeholders are substituted correctly
        assertEquals("(no title)", title);
        assertEquals("(no description)", description);
    }

    /**
     * US 03.04.01
     * Tests that when an event has an empty string for title,
     * the placeholder logic still correctly substitutes the fallback.
     */
    @Test
    public void testEventItemHandlesEmptyFields() {
        String title = "";
        String description = "";

        if (title == null || title.isEmpty()) title = "(no title)";
        if (description == null || description.isEmpty()) description = "(no description)";

        assertEquals("(no title)", title);
        assertEquals("(no description)", description);
    }


    /**
     * US 03.01.01
     * - As an administrator, I want to be able to remove events.
     * Tests that an EventItem intended for deletion has a valid non-null documentId.
     * The documentId is what gets passed to Firestore's .delete() call, so if it's
     * null or empty, deletion would fail silently or crash.
     */
    @Test
    public void testEventItemHasValidDocumentIdForDeletion() {
        com.example.junimoapp.admin.AdminEventAdapter.EventItem event =
                new com.example.junimoapp.admin.AdminEventAdapter.EventItem(
                        "abc456",
                        "Dance Class",
                        "Beginner dance"
                );

        // documentId must not be null or empty for Firestore deletion to work
        assertNotNull(event.documentId);
        assertFalse(event.documentId.isEmpty());
    }

    /**
     * US 03.01.01
     * Tests that after simulating a deletion from a list,
     * the event is actually removed and the list size decreases by 1.
     * This mirrors the logic in AdminBrowseEventsActivity.deleteEventFromFirestore()
     * where we remove the item from the local list after Firestore confirms deletion.
     */
    @Test
    public void testEventRemovedFromListAfterDeletion() {
        // set up a list of events the same way the Activity does
        java.util.List<com.example.junimoapp.admin.AdminEventAdapter.EventItem> eventList =
                new java.util.ArrayList<>();

        com.example.junimoapp.admin.AdminEventAdapter.EventItem event1 =
                new com.example.junimoapp.admin.AdminEventAdapter.EventItem(
                        "id1", "Yoga Class", "Morning yoga");

        com.example.junimoapp.admin.AdminEventAdapter.EventItem event2 =
                new com.example.junimoapp.admin.AdminEventAdapter.EventItem(
                        "id2", "Piano Lessons", "Beginner piano");

        eventList.add(event1);
        eventList.add(event2);

        // confirm we start with 2 events
        assertEquals(2, eventList.size());

        // simulate what happens in deleteEventFromFirestore() on success:
        // find the position and remove it from the local list
        int position = eventList.indexOf(event1);
        if (position != -1) {
            eventList.remove(position);
        }

        // verify the list now has 1 item and the deleted event is gone
        assertEquals(1, eventList.size());
        assertFalse(eventList.contains(event1));
        // verify the other event is still there
        assertTrue(eventList.contains(event2));
    }

    /**
     * US 03.01.01
     * Tests that trying to delete an event that doesn't exist in the list
     * does not crash and leaves the list unchanged.
     * Covers the (position != -1) guard in deleteEventFromFirestore().
     */
    @Test
    public void testDeletingNonExistentEventDoesNotCrash() {
        java.util.List<com.example.junimoapp.admin.AdminEventAdapter.EventItem> eventList =
                new java.util.ArrayList<>();

        com.example.junimoapp.admin.AdminEventAdapter.EventItem existingEvent =
                new com.example.junimoapp.admin.AdminEventAdapter.EventItem(
                        "id1", "Swimming", "Beginner swim");

        com.example.junimoapp.admin.AdminEventAdapter.EventItem ghostEvent =
                new com.example.junimoapp.admin.AdminEventAdapter.EventItem(
                        "id999", "Ghost Event", "Does not exist in list");

        eventList.add(existingEvent);

        // try to find and delete an event that was never added to the list
        int position = eventList.indexOf(ghostEvent);
        if (position != -1) {
            eventList.remove(position);
        }

        // list should be unchanged - still has 1 item
        assertEquals(1, eventList.size());
        assertTrue(eventList.contains(existingEvent));
    }


    //USER RELATED UNIT TESTS

    /**
     * US 03.02.01
     * As an administrator, I want to be able to browse profiles
     * Testing that the UserItem is made correctly with the expected fields
     */
    @Test
    public void testUserItemCreation() {
        //Create a UserItem the same way AdminBrowseProfilesActivity does
        //when reading from Firestore
        com.example.junimoapp.admin.AdminUserAdapter.UserItem user =
                new com.example.junimoapp.admin.AdminUserAdapter.UserItem(
                        "device123", //device ID
                        "Cassie",
                        "test@email.com"
                );

        //verify fields were stored correctly
        assertEquals("device123", user.documentId);
        assertEquals("Cassie", user.name);
        assertEquals("test@email.com", user.email);
    }

    /**
     * US 03.02.01
     * Tests missing Firestore fields (placeholder fallbacks)
     */
    @Test
    public void testUserItemHandlesMissingFields() {
        String name = null;
        String email = null;

        if (name == null || name.isEmpty()) name = "(no name entered)";
        if (email == null || email.isEmpty()) email = "(no email entered)";

        assertEquals("(no name entered)", name);
        assertEquals("(no email entered)", email);

    }

    /**
     * US 03.03.01
     * As an administratos, I want to be able to remove profiles
     * Tests that a UserItem intended for deletion has a documentID since that's what's
     * passed to the Firestore's delete call. If it's null or empty, it will fail
     */
    @Test
    public void testUserItemHasValidDocumentIdForDeletion() {
        com.example.junimoapp.admin.AdminUserAdapter.UserItem user =
                new com.example.junimoapp.admin.AdminUserAdapter.UserItem(
                        "abc123",
                        "Example Name",
                        "example@email.com"
                );

        assertNotNull(user.documentId);
        assertFalse(user.documentId.isEmpty());
    }

    /**
     * US 3.03.01
     * Tests that after a deletion from the list, the user is actually removed
     * and the list size is decreased by 1
     */
    @Test
    public void testUserRemovedFromListAfterDeletion() {
        //set up a list of users the way the activity does
        java.util.List<com.example.junimoapp.admin.AdminUserAdapter.UserItem> userList =
                new java.util.ArrayList<>();

        com.example.junimoapp.admin.AdminUserAdapter.UserItem user1 =
                new com.example.junimoapp.admin.AdminUserAdapter.UserItem(
                        "id1", "One", "one@email.com");
        com.example.junimoapp.admin.AdminUserAdapter.UserItem user2 =
                new com.example.junimoapp.admin.AdminUserAdapter.UserItem(
                        "id2", "Two", "two@email.com");

        userList.add(user1);
        userList.add(user2);

        //confirm we have 2 users
        assertEquals(2, userList.size());

        int position = userList.indexOf(user1);
        if (position != -1) {
            userList.remove(position);
        }

        //verify list has one item and deleted user is gone
        assertEquals(1, userList.size());
        assertFalse(userList.contains(user1));

        //verify other user is still there
        assertTrue(userList.contains(user2));
    }


    /**
     * US 03.05.01
     * As an administrator, I want to remove organizers that violate app policy
     * Simulates organizer creation
     */
    @Test
    public void testOrganizerItemCreationForDemotion() {
        //create OrganizerItem how AdminBrowseOrganizersActivity does when reading from Firestore
        com.example.junimoapp.admin.AdminOrganizerAdapter.OrganizerItem organizer =
                new com.example.junimoapp.admin.AdminOrganizerAdapter.OrganizerItem(
                        "organizer123", //doc ID
                        "John Organizer", //name
                        "organizer@example.com", //email
                        false //placeholder flagged status
                );

        //verify fields are stored correctly
        assertEquals("organizer123", organizer.documentId);
        assertEquals("John Organizer", organizer.name);
        assertEquals("organizer@example.com", organizer.email);
        assertFalse(organizer.flagged);

        //check docId validity (needs to be valid for deletion)
        assertNotNull(organizer.documentId);
        assertFalse(organizer.documentId.isEmpty());
    }

}