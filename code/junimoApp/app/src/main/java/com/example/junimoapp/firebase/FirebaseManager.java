package com.example.junimoapp.firebase;

import android.util.Log;

import com.example.junimoapp.models.Event;
import com.example.junimoapp.models.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
    public boolean addEvent(Event event, CollectionReference eventsRef) {
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
    public boolean updateEvent(CollectionReference eventsRef, Event event, String field, String newValue) {
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
    public boolean updateEvent(CollectionReference eventsRef, Event event, String field, Long newValue) {
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
    public boolean updateEvent(CollectionReference eventsRef, Event event, String field, GeoPoint newValue) {
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
    public boolean updateEvent(CollectionReference eventsRef, Event event, String field, ArrayList<String> newValue) {
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
    public boolean updateEvent(CollectionReference eventsRef, Event event, String field, double newValue) {
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

    /**
     * Loads in a list of all events from firestore
     * @param eventsRef
     * collection reference to events
     * @return
     * returns list of events
     */
    public ArrayList<Event> getEvents(CollectionReference eventsRef){
        ArrayList<Event> eventArrayList = new ArrayList<>();
        eventsRef.addSnapshotListener((value,error)-> {
            if(error != null){
                Log.e("Firestore", error.toString());
            }
            if(value != null && !value.isEmpty()){
                eventArrayList.clear();
                for(QueryDocumentSnapshot snapshot : value){
                    //Fields in events
                    String title = snapshot.getString("Title");
                    String description = snapshot.getString("Description");
                    String startDate = snapshot.getString("startDate");
                    String endDate = snapshot.getString("endDate");
                    String dateEvent = snapshot.getString("dateEvent");
                    int maxCapacity = (snapshot.getLong("maxCapacity")).intValue();
                    int waitingListLimit = (snapshot.getLong("waitingListLimit")).intValue();
                    double price = snapshot.getDouble("price");
                    GeoPoint geoLocation = snapshot.getGeoPoint("geoLocation"); //geoPoint is a type apparently? seems helpful??
                    String poster = snapshot.getString("poster");
                    String eventID = snapshot.getString("eventID");
                    String eventLocation = snapshot.getString("eventLocation");

                    String organizerID = snapshot.getString("organizerID");

                    eventArrayList.add(new Event(title,description,startDate,endDate,dateEvent,maxCapacity,waitingListLimit,price,geoLocation,poster,eventID,eventLocation,organizerID));
                }
            }
        });
        return eventArrayList;
    }

    /**
     * Loads in a list of all users from firestore
     * @param usersRef
     * A collection reference to users
     * @return
     * a list of all users
     */
    public ArrayList<User> getUsers(CollectionReference usersRef){
        ArrayList<User> userArrayList= new ArrayList<>();
        usersRef.addSnapshotListener((value, error)->{
            if(error != null){
                Log.e("Firestore",error.toString());
            }
            if(value!=null && !value.isEmpty()){
                userArrayList.clear();
                for(QueryDocumentSnapshot snapshot : value){
                    String deviceId = snapshot.getString("deviceId");
                    String name = snapshot.getString("name");
                    String email = snapshot.getString("email");
                    String phone = snapshot.getString("phone");

                    userArrayList.add(new User(deviceId,name,email,phone));
                }
            }
        });
        return userArrayList;
    }

    //to do add load in users and load in events
}