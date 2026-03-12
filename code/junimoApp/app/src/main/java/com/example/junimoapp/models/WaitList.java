package com.example.junimoapp.models;

import com.example.junimoapp.models.Event;
import com.example.junimoapp.firebase.FirebaseManager;
import com.google.firebase.firestore.CollectionReference;

import java.util.ArrayList;

public class WaitList {
    private ArrayList<User> users;
    private int maxCapacity;
    private int waitListLimit;
    private String eventID;

    public WaitList(Event event) {
        this.maxCapacity = event.getMaxCapacity();
        this.waitListLimit = event.getWaitingListLimit();
        this.eventID = event.getEventID();
        populateWaitList(event);
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public int getWaitListLimit() {
        return waitListLimit;
    }

    public void setWaitListLimit(int waitListLimit) {
        this.waitListLimit = waitListLimit;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    /**
     * Gets all users from user ids in event class waitList
     * should be called on any change in waitList
     * @param event
     * the event associated with the waitlist
     */
    public void populateWaitList(Event event){
        users.clear();
        ArrayList<String> deviceIDs=event.getWaitList();
        FirebaseManager firebase= new FirebaseManager();
        CollectionReference usersRef = firebase.getDB().collection("users");

        for(String deviceID : deviceIDs){
            User user = firebase.getUserById(deviceID,usersRef);
            users.add(user);
        }
    }

}
