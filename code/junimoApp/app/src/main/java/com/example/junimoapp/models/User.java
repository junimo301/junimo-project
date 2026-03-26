package com.example.junimoapp.models;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.junimoapp.firebase.FirebaseManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

/**
 * Class defining user, includes profile information and role
 */
public class User {

    private String deviceId;
    private String name;
    private String email;
    private String phone;
    private boolean organizer;
    private boolean admin;
    private ArrayList<Event> organizedEvents;
    private ArrayList<Event> waitListedEvents;
    private ArrayList<Event> invitedEvents;
    private String organizedEventsString;
    private String waitlistedEventsString;
    private String invitedEventsString;

    FirebaseManager firebase = new FirebaseManager();
    FirebaseFirestore db = firebase.getDB();

    public User(String deviceId, String name, String email, String phone,String organized, String invited, String waitlisted) {
        this.deviceId = deviceId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.organizer = true; //starts as true, or maybe should change to true when an event is created?
        this.admin = false; //set as true only if device id matches ours
        this.organizedEventsString = organized;
        this.invitedEventsString = invited;
        this.waitlistedEventsString = waitlisted;

        initializeEvents();
    }
    public String getDeviceId() {
        return deviceId;
    }
    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public String getPhone() {
        return phone;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isOrganizer() {
        return organizer;
    }

    public void setOrganizer(boolean organizer) {
        this.organizer = organizer;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public ArrayList<Event> getOrganizedEvents() {
        return organizedEvents;
    }

    public ArrayList<Event> getWaitListedEvents() {
        return waitListedEvents;
    }

    public ArrayList<Event> getInvitedEvents() {
        return invitedEvents;
    }

    public void inviteUser(Event event){
        invitedEventsString= invitedEventsString + (event.getEventID())+",";
        firebase.updateUser(db.collection("users"),this,"invitedEvents",invitedEventsString);
        invitedEvents.add(event);
    }
    public boolean isInvited(Event event){
        return invitedEvents.contains(event);
    }
    public void cancelUser(Event event) {
        invitedEvents.remove(event);
    }
    public void addOrganizedEvent(Event event){
        organizedEvents.add(event);
    }

    public void removeOrganizedEvent(Event event){
        organizedEvents.remove(event);
    }

    /**
     * Adds a user to an event waitList by calling a method in the event
     * @param event
     * the event to join the waitlist for
     * @return
     * true or false on success/failure
     */
    public void joinEventWaitList(Event event){
        boolean check=event.enrollInWaitList(deviceId);
        if(check){
            waitListedEvents.add(event);
        }
    }
    public void leaveEventWaitList(Event event){
        boolean check=event.removeFromWaitList(deviceId);
        if(check){
            waitListedEvents.remove(event);
        }
    }

    /**
     * Demotes an organizer to a user and deletes all events created by the user
     */
    public void demoteOrganizer() {
        organizer = false;
        //get rid of all their events from firestore
        CollectionReference eventsRef = db.collection("events");
        for(Event event : organizedEvents){
            firebase.deleteEvent(event,eventsRef);
        }
        organizedEvents.clear();
    }

    public void initializeEvents(){
        this.organizedEvents = new ArrayList<Event>();
        this.invitedEvents = new ArrayList<Event>();
        this.waitListedEvents = new ArrayList<Event>();
        CollectionReference usersRef = db.collection("users");
        usersRef.document(deviceId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("Firestore", "DocumentSnapshot data: " + document.getData());
                        organizedEventsString=document.getString("organizedEvents");
                        invitedEventsString=document.getString("invitedEvents");
                        waitlistedEventsString=document.getString("waitlistedEvents");
                        String[] organized = organizedEventsString.split(",");
                        String[] invited = invitedEventsString.split(",");
                        String[] waitlisted = waitlistedEventsString.split(",");
                        readStringList(organized,organizedEvents);
                        readStringList(invited,invitedEvents);
                        readStringList(waitlisted,waitListedEvents);

                    } else {
                        Log.d("Firestore", "No such document");
                    }
                } else {
                    Log.d("Firestore", "get failed with ", task.getException());
                }
            }
        });

    }
    public void readStringList(String[] stringList, ArrayList<Event> eventList) {
        if (stringList.length >= 1) {
            CollectionReference eventsRef = db.collection("events");
            for (String eventID : stringList) {
                if (eventID != null && eventID != "") {
                    Log.d("populating user event list", eventID);
                    eventsRef.document(eventID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot doc = task.getResult();
                                if (doc.exists()) {
                                    Log.d("Firestore", "DocumentSnapshot data: " + doc.getData());
                                    String title = doc.getString("title");
                                    String description = doc.getString("description");
                                    String startDate = doc.getString("startDate");
                                    String endDate = doc.getString("endDate");
                                    String dateEvent = doc.getString("dateEvent");
                                    int maxCapacity = (doc.getLong("maxCapacity")).intValue();
                                    int waitingListLimit = (doc.getLong("waitingListLimit")).intValue();
                                    double price = doc.getDouble("price");
                                    GeoPoint geoLocation = doc.getGeoPoint("geoLocation"); //geoPoint is a type apparently? seems helpful??
                                    String poster = doc.getString("poster");
                                    String eventID = doc.getString("eventID");
                                    String eventLocation = doc.getString("eventLocation");
                                    String organizerID = doc.getString("organizerID");

                                    Event event = new Event(title, description, startDate, endDate, dateEvent, maxCapacity, waitingListLimit, price, geoLocation, poster, eventID, eventLocation, organizerID);
                                    eventList.add(event);

                                } else {
                                    Log.d("Firestore", "No such document");
                                }
                            } else {
                                Log.d("Firestore", "get failed with ", task.getException());
                            }
                        }

                    });
                }
            }

        }
    }
}