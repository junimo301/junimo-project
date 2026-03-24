package com.example.junimoapp.models;

import com.example.junimoapp.firebase.FirebaseManager;
import com.google.firebase.firestore.CollectionReference;

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

    public User(String deviceId, String name, String email, String phone) {
        this.deviceId = deviceId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.organizer = true; //starts as true, or maybe should change to true when an event is created?
        this.admin = false; //set as true only if device id matches ours
        this.organizedEvents = new ArrayList<Event>();
        this.invitedEvents = new ArrayList<Event>();
        this.waitListedEvents = new ArrayList<Event>();
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

    public void inviteUser(Event event){
        invitedEvents.add(event);
    }
    public boolean isInvited(Event event){
        return invitedEvents.contains(event);
    }
    public void cancelUser(Event event) {
        invitedEvents.remove(event);
    }
    public void addOrganizedEvent(Event event){
        organizedEvents.add(event); //not sure when to call this...
    }

    public void removeOrganizedEvent(Event event){
        organizedEvents.add(event);
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

    /**
     * Demotes an organizer to a user and deletes all events created by the user
     */
    public void demoteOrganizer() {
        organizer = false;
        //get rid of all their events from firestore
        FirebaseManager firebase = new FirebaseManager();
        CollectionReference eventsRef = firebase.getDB().collection("events");
        for(Event event : organizedEvents){
            firebase.deleteEvent(event,eventsRef);
        }
        organizedEvents.clear();
    }
}