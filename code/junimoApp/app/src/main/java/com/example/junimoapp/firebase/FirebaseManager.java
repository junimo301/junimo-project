package com.example.junimoapp.firebase;

import com.example.junimoapp.models.Event;
import com.example.junimoapp.models.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Class for defining firebase methods and loading in the firebase instance
 */
public class FirebaseManager {
    public FirebaseManager() {}

    /** Get firestore instance */
    public static FirebaseFirestore getDB() {
        return FirebaseFirestore.getInstance();
    }

    /**
     * Adds an event to firebase
     * @param event the event to be added
     * @param eventsRef a reference to the events collection
     */
    public void addEvent(Event event, CollectionReference eventsRef) {
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.set(event);
    }

    /**
     * Deletes an event from firebase
     * @param event event to be deleted
     * @param eventsRef collection reference to events collection
     */
    public void deleteEvent(Event event, CollectionReference eventsRef) {
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.delete();
    }

    /**
     * Adds a user to firebase
     * @param user the user to be added
     * @param usersRef a reference to the users collection
     */
    public void addUser(User user, CollectionReference usersRef) {
        DocumentReference docRef = usersRef.document(user.getDeviceId());
        docRef.set(user);
        user.initializeEvents();
    }

    public void deleteUser(User user, CollectionReference usersRef) {
        for (Event event : user.getWaitlistedEventsList()) {
            user.leaveEventWaitList(event);
        }
        for (Event event : user.getOrganizedEventsList()) {
            deleteEvent(event, getDB().collection("events"));
        }
        for (Event event : user.getInvitedEventsList()) {
            user.cancelUser(event);
        }
        DocumentReference docRef = usersRef.document(user.getDeviceId());
        docRef.delete();
    }

    // ── Event update overloads (all unchanged) ────────────────────────────

    public static void updateEvent(CollectionReference eventsRef, Event event,
                                   String field, String newValue) {
        eventsRef.document(event.getEventID()).update(field, newValue);
    }

    public void updateEvent(CollectionReference eventsRef, Event event,
                            String field, Long newValue) {
        eventsRef.document(event.getEventID()).update(field, newValue);
    }

    public void updateEvent(CollectionReference eventsRef, Event event,
                            String field, boolean newValue) {
        eventsRef.document(event.getEventID()).update(field, newValue);
    }

    public static void updateEvent(CollectionReference eventsRef, Event event,
                                   String field, ArrayList<String> newValue) {
        eventsRef.document(event.getEventID()).update(field, newValue);
    }

    public void updateEvent(CollectionReference eventsRef, Event event,
                            String field, double newValue) {
        eventsRef.document(event.getEventID()).update(field, newValue);
    }

    // ── User update overloads ─────────────────────────────────────────────

    /**
     * Updates specified user field with a String value
     * @param usersRef a reference to the users collection
     * @param user the user to be updated
     * @param field the field to be updated
     * @param newValue the new String value
     */
    public void updateUser(CollectionReference usersRef, User user,
                           String field, String newValue) {
        DocumentReference docRef = usersRef.document(user.getDeviceId());
        docRef.update(field, newValue);
    }

    // ─────────────────────────────────────────────────────────────────────
    // US 01.04.03
    // Boolean overload of updateUser — needed to persist the
    // notificationsEnabled flag to Firestore.
    // Called by User.setNotificationsEnabled().
    // Follows the same pattern as the String overload above.
    // ─────────────────────────────────────────────────────────────────────
    /**
     * Updates specified user field with a boolean value
     * @param usersRef a reference to the users collection
     * @param user the user to be updated
     * @param field the field to be updated
     * @param newValue the new boolean value
     */
    public void updateUser(CollectionReference usersRef, User user,
                           String field, boolean newValue) {
        DocumentReference docRef = usersRef.document(user.getDeviceId());
        docRef.update(field, newValue);
    }
}