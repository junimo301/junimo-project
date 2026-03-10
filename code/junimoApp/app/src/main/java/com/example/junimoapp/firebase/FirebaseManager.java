package com.example.junimoapp.firebase;

import com.example.junimoapp.Organizer.OrganizerEvent;
import com.example.junimoapp.models.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.atomic.AtomicBoolean;

public class FirebaseManager {
    public FirebaseManager() {}

    //get firestore instance
    public static FirebaseFirestore getDB() {
        return FirebaseFirestore.getInstance();
    }
    public boolean AddEvent(OrganizerEvent event, CollectionReference eventsRef) {
        AtomicBoolean check = new AtomicBoolean(false);
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.set(event).addOnSuccessListener(unused->{
            check.set(true);});
        return check.get();
    }
    public boolean AddUser(User user, CollectionReference usersRef) {
        AtomicBoolean check = new AtomicBoolean(false);
        DocumentReference docRef = usersRef.document(user.getDeviceId());
        docRef.set(user).addOnSuccessListener(unused->{
            check.set(true);});
        return check.get();
    }
}