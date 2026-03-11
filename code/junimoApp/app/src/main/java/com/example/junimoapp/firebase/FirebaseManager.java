package com.example.junimoapp.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.junimoapp.Organizer.OrganizerEvent;
import com.example.junimoapp.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Pipeline;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.sql.Timestamp;
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
     * Adds an event to firebase, returns true on success, otherwise false
     * @param event
     * the event to be added
     * @param eventsRef
     * a reference to the events collection
     * @return
     * true on success, false on failure
     */
    public boolean addEvent(OrganizerEvent event, CollectionReference eventsRef) {
        AtomicBoolean check = new AtomicBoolean(false);
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.set(event).addOnSuccessListener(unused->{
            check.set(true);
        });
        return check.get();
    }

    /**
     * Adds a user to firebase, returns true on success, otherwise false
     * @param user
     * the user to be added
     * @param usersRef
     * a reference to the users collection
     * @return
     * true on success, false on failure
     */
    public boolean addUser(User user, CollectionReference usersRef) {
        AtomicBoolean check = new AtomicBoolean(false);
        DocumentReference docRef = usersRef.document(user.getDeviceId());
        docRef.set(user).addOnSuccessListener(unused->{
            check.set(true);});
        return check.get();
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
     * @return
     * returns true on success, false on failure
     */
    public boolean updateEvent(CollectionReference eventsRef, OrganizerEvent event, String field, String newValue) {
        AtomicBoolean check = new AtomicBoolean(false);
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.update(field, newValue).addOnSuccessListener(unused->{
            check.set(true);
        });
        return check.get();
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
     * @return
     * returns true on success, false on failure
     */
    public boolean updateEvent(CollectionReference eventsRef, OrganizerEvent event, String field, Long newValue) {
        AtomicBoolean check = new AtomicBoolean(false);
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.update(field, newValue).addOnSuccessListener(unused->{
            check.set(true);
        });
        return check.get();
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
     * @return
     * returns true on success, false on failure
     */
    public boolean updateEvent(CollectionReference eventsRef, OrganizerEvent event, String field, GeoPoint newValue) {
        AtomicBoolean check = new AtomicBoolean(false);
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.update(field, newValue).addOnSuccessListener(unused->{
            check.set(true);
        });
        return check.get();
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
     * @return
     * returns true on success, false on failure
     */
    public boolean updateEvent(CollectionReference eventsRef, OrganizerEvent event, String field, ArrayList<String> newValue) {
        AtomicBoolean check = new AtomicBoolean(false);
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.update(field, newValue).addOnSuccessListener(unused->{
            check.set(true);
        });
        return check.get();
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
     * @return
     * returns true on success, false on failure
     */
    public boolean updateEvent(CollectionReference eventsRef, OrganizerEvent event, String field, double newValue) {
        AtomicBoolean check = new AtomicBoolean(false);
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.update(field, newValue).addOnSuccessListener(unused->{
            check.set(true);
        });
        return check.get();
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
     * @return
     * returns true on success, false on failure
     */
    public boolean updateUser(CollectionReference usersRef, User user, String field, String newValue) {
        AtomicBoolean check = new AtomicBoolean(false);
        DocumentReference docRef = usersRef.document(user.getDeviceId());
        docRef.update(field,newValue).addOnSuccessListener(unused->{
            check.set(true);
        });
        return check.get();
    }
}