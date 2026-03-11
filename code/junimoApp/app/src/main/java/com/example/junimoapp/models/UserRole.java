package com.example.junimoapp.models;

import java.util.ArrayList;
import java.util.List;

public class UserRole {
    /*
    * Defines a user's role as either a standard user, organizer or admin
    * Records events of an organizer
    *   - if an organizer is deleted the events can be as well
    */
    private String userID;
    private String userName;

    //User Roles
    private boolean user;
    private boolean organizer;
    private boolean admin;

    //list of the events id the organizer own
    private List<String> organizersEventsID = new ArrayList<>();

    public UserRole(String userID, String userName, boolean user, boolean organizer, boolean admin) {
        this.userID = userID;
        this.userName = userName;

        this.user = user;
        this.organizer = organizer;
        this.admin = admin;
    }

    //getters

    public List<String> getOrganizersEventsID() {
        return organizersEventsID;
    }

    public String getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
    }

    //getters and setters
    public boolean isUser() {
        return user;
    }

    public void setUser(boolean user) {
        this.user = user;
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

    //update the organizers list of event id's if new event created or removed
    public void addedEvent(String eventID) {
        organizersEventsID.add(eventID);
    }
    public void removedEvents(String eventID) {
        organizersEventsID.remove(eventID);
    }

    //change organizer's role to user
    public void changeRole() {
        organizer = false;
        user = true;
        //get rid of all their events
        organizersEventsID.clear();
    }

}
