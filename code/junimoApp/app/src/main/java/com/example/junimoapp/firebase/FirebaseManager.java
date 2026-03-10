package com.example.junimoapp.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.junimoapp.Organizer.OrganizerEvent;
import com.example.junimoapp.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class FirebaseManager {
    public FirebaseManager() {}

    //get firestore instance
    public static FirebaseFirestore getDB() {
        return FirebaseFirestore.getInstance();
    }
    public boolean addEvent(OrganizerEvent event, CollectionReference eventsRef) {
        AtomicBoolean check = new AtomicBoolean(false);
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.set(event).addOnSuccessListener(unused->{
            check.set(true);
        });
        return check.get();
    }
    public boolean addUser(User user, CollectionReference usersRef) {
        AtomicBoolean check = new AtomicBoolean(false);
        DocumentReference docRef = usersRef.document(user.getDeviceId());
        docRef.set(user).addOnSuccessListener(unused->{
            check.set(true);});
        return check.get();
    }

    //updates to various different event fields (newValue type changes)
    public boolean updateEvent(CollectionReference eventsRef, OrganizerEvent event, String field, String newValue) {
        AtomicBoolean check = new AtomicBoolean(false);
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.update(field, newValue).addOnSuccessListener(unused->{
            check.set(true);
        });
        return check.get();
    }
    public boolean updateEvent(CollectionReference eventsRef, OrganizerEvent event, String field, Long newValue) {
        AtomicBoolean check = new AtomicBoolean(false);
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.update(field, newValue).addOnSuccessListener(unused->{
            check.set(true);
        });
        return check.get();
    }
    public boolean updateEvent(CollectionReference eventsRef, OrganizerEvent event, String field, GeoPoint newValue) {
        AtomicBoolean check = new AtomicBoolean(false);
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.update(field, newValue).addOnSuccessListener(unused->{
            check.set(true);
        });
        return check.get();
    }
    public boolean updateEvent(CollectionReference eventsRef, OrganizerEvent event, String field, ArrayList<String> newValue) {
        AtomicBoolean check = new AtomicBoolean(false);
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.update(field, newValue).addOnSuccessListener(unused->{
            check.set(true);
        });
        return check.get();
    }
    public boolean updateEvent(CollectionReference eventsRef, OrganizerEvent event, String field, double newValue) {
        AtomicBoolean check = new AtomicBoolean(false);
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.update(field, newValue).addOnSuccessListener(unused->{
            check.set(true);
        });
        return check.get();
    }

    //updates to user
    public boolean updateUser(CollectionReference usersRef, User user, String field, String newValue) {
        AtomicBoolean check = new AtomicBoolean(false);
        DocumentReference docRef = usersRef.document(user.getDeviceId());
        docRef.update(field,newValue).addOnSuccessListener(unused->{
            check.set(true);
        });
        return check.get();
    }

}