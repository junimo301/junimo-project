package com.example.junimoapp.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.junimoapp.models.Event;
import com.example.junimoapp.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class for defining firebase methods
 */
public class FirebaseManager {
    public FirebaseManager() {}

    //get firestore instance
    public static FirebaseFirestore getDB() {
        return FirebaseFirestore.getInstance();
    }

    /**
     * Adds an event to firebase
     * @param event
     * the event to be added
     * @param eventsRef
     * a reference to the events collection
     */
    public void addEvent(Event event, CollectionReference eventsRef) {
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.set(event);
    }

    /**
     * Deletes an event from firebase
     * @param event
     * event to be deleted
     * @param eventsRef
     * collection reference to events collection
     */
    public void deleteEvent(Event event, CollectionReference eventsRef) {
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.delete();
    }

    /**
     * Adds a user to firebase
     * @param user
     * the user to be added
     * @param usersRef
     * a reference to the users collection
     */
    public void addUser(User user, CollectionReference usersRef) {
        DocumentReference docRef = usersRef.document(user.getDeviceId());
        docRef.set(user);
    }

    //updates to various different event fields (newValue type changes)

    /**
     * Updates specified event field with specified value
     * @param eventsRef
     * a reference to the events collection
     * @param event
     * the event to be updated
     * @param field
     * the field to be updated
     * @param newValue
     * the new value to be added
     */
    public void updateEvent(CollectionReference eventsRef, Event event, String field, String newValue) {
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.update(field, newValue);
    }
    /**
     * Updates specified event field with specified value
     * @param eventsRef
     * a reference to the events collection
     * @param event
     * the event to be updated
     * @param field
     * the field to be updated
     * @param newValue
     * the new value to be added
     */
    public void updateEvent(CollectionReference eventsRef, Event event, String field, Long newValue) {
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.update(field, newValue);
    }
    /**
     * Updates specified event field with specified value
     * @param eventsRef
     * a reference to the events collection
     * @param event
     * the event to be updated
     * @param field
     * the field to be updated
     * @param newValue
     * the new value to be added
     */
    public void updateEvent(CollectionReference eventsRef, Event event, String field, GeoPoint newValue) {
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.update(field, newValue);
    }
    /**
     * Updates specified event field with specified value
     * @param eventsRef
     * a reference to the events collection
     * @param event
     * the event to be updated
     * @param field
     * the field to be updated
     * @param newValue
     * the new value to be added
     */
    public static void updateEvent(CollectionReference eventsRef, Event event, String field, ArrayList<String> newValue) {
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.update(field, newValue);
    }
    /**
     * Updates specified event field with specified value
     * @param eventsRef
     * a reference to the events collection
     * @param event
     * the event to be updated
     * @param field
     * the field to be updated
     * @param newValue
     * the new value to be added
     */
    public void updateEvent(CollectionReference eventsRef, Event event, String field, double newValue) {
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.update(field, newValue);
    }

    //updates to user
    /**
     * Updates specified user field with specified value
     * @param usersRef
     * a reference to the users collection
     * @param user
     * the event to be updated
     * @param field
     * the field to be updated
     * @param newValue
     * the new value to be added
     */
    public void updateUser(CollectionReference usersRef, User user, String field, String newValue) {
        DocumentReference docRef = usersRef.document(user.getDeviceId());
        docRef.update(field,newValue);
    }

}