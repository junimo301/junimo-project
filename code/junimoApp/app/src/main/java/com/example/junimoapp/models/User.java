package com.example.junimoapp.models;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.junimoapp.firebase.FirebaseManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
    private String organizedEvents;
    private String waitlistedEvents;
    private String invitedEvents;
    private String cancelledEvents;
    private ArrayList<Event> organizedEventsList;
    private ArrayList<Event> waitlistedEventsList;
    private ArrayList<Event> invitedEventsList;
    private String coOrganizerInvites;

    // ─────────────────────────────────────────────────────────────────────
    // US 01.04.03
    // Stores whether this user wants to receive in-app notifications.
    // Defaults to true — notifications are on unless the user opts out.
    // Persisted to Firestore via FirebaseManager.updateUser() inside
    // setNotificationsEnabled(). Loaded from Firestore in initializeEvents().
    // ─────────────────────────────────────────────────────────────────────
    private boolean notificationsEnabled = true;

    FirebaseManager firebase;
    FirebaseFirestore db;

    /**
     * Initializes  user details, personal information, their role, and their events.
     * @param deviceId device id of the user
     * @param name use rename
     * @param email email
     * @param phone phone number
     * @param organized events that the user has organized (as organizer)
     * @param invited events that the user been invited to
     * @param waitlisted events that the user has waitlisted
     * */
    public User(String deviceId, String name, String email, String phone,
                String organized, String invited, String waitlisted) {
        this.deviceId = deviceId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.organizer = true;
        this.admin = false;
        this.organizedEvents = organized;
        this.invitedEvents = invited;
        this.waitlistedEvents = waitlisted;
        this.cancelledEvents = "";
        this.coOrganizerInvites = "";

        try {
            firebase = new FirebaseManager();
            db = firebase.getDB();
        } catch (Exception e) {
            // Running unit tests, don't want/need firebase
        }
    }

    // ── Existing getters / setters (unchanged) ────────────────────────────

    public String getDeviceId() { return deviceId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public boolean isOrganizer() { return organizer; }
    public void setOrganizer(boolean organizer) { this.organizer = organizer; }
    public boolean isAdmin() { return admin; }
    public void setAdmin(boolean admin) { this.admin = admin; }
    public void setOrganizedEvents(String organizedEvents) { this.organizedEvents = organizedEvents; }
    public String getWaitlistedEvents() { return waitlistedEvents; }
    public void setWaitlistedEvents(String waitlistedEvents) { this.waitlistedEvents = waitlistedEvents; }
    public String getInvitedEvents() { return invitedEvents; }
    public void setInvitedEvents(String invitedEvents) { this.invitedEvents = invitedEvents; }
    public ArrayList<Event> getOrganizedEventsList() { return organizedEventsList; }
    public ArrayList<Event> getWaitlistedEventsList() { return waitlistedEventsList; }
    public ArrayList<Event> getInvitedEventsList() { return invitedEventsList; }

    // ─────────────────────────────────────────────────────────────────────
    // US 01.04.03
    // Getter for notification preference — used by UserHomeActivity to set
    // the switch state, and by NotificationHelper as a guard check.
    // ─────────────────────────────────────────────────────────────────────
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    // ─────────────────────────────────────────────────────────────────────
    // US 01.04.03
    // Setter for notification preference — updates the local field and
    // persists to Firestore using FirebaseManager.updateUser() so the
    // preference survives app restarts.
    // ─────────────────────────────────────────────────────────────────────
    /**
     * Sets that the user wants notifications and updates the users Firestore data
     * @param enabled notifications enabled
     * */
    public void setNotificationsEnabled(boolean enabled) {
        this.notificationsEnabled = enabled;
        if (db != null) {
            firebase.updateUser(db.collection("users"), this,
                    "notificationsEnabled", enabled);
        }
    }

    // ── Existing methods (unchanged) ──────────────────────────────────────

    /**
     * Invites user to an event
     * Updates Firestore to add the event to the users invitedEvents
     * @param event the event to invite the user to
     * */
    public void inviteUser(Event event) {
        if (!invitedEvents.contains(event.getEventID())) {
            invitedEvents = invitedEvents + (event.getEventID()) + ",";
            firebase.updateUser(db.collection("users"), this, "invitedEvents", invitedEvents);
            // Guard against null list — invitedEventsList is only initialized
            // by initializeEvents() which may not have been called on this User object
            if (invitedEventsList != null) {
                invitedEventsList.add(event);
            }
            event.Invite(deviceId);
        }
    }

    public boolean isInvited(String eventID) {
        return invitedEvents.contains(eventID);
    }

    /**
     * Removes user from an event
     * Updates Firestore to remove the event from the users invitedEvents
     * @param event the event to remove the user from
     * */
    public void cancelUser(Event event) {
        if (invitedEvents.contains(event.getEventID())) {
            invitedEvents = invitedEvents.replace(event.getEventID() + ",", "");
            firebase.updateUser(db.collection("users"), this, "invitedEvents", invitedEvents);
        }
        invitedEventsList.remove(event);
        cancelledEvents = cancelledEvents + event.getEventID() + ",";
    }

    public boolean isCancelled(String eventID) {
        if(eventID == null || cancelledEvents==null){
            return false;
        } else {
            return cancelledEvents.contains(eventID);
        }
    }

    /**
     * States whether the user organizer the event
     * Updates Firestore to add the event to the users organizedEvents, and local list
     * @param event the event to add to the users organizedEvents
     * */
    public void addOrganizedEvent(Event event) {
        organizedEvents = organizedEvents + (event.getEventID()) + ",";
        firebase.updateUser(db.collection("users"), this, "organizedEvents", organizedEvents);
        organizedEventsList.add(event);
    }

    /**
     * Removes the event from the users organizedEvents
     * Updates Firestore and local list to remove the event
     * @param event the event to remove
     * */
    public void removeOrganizedEvent(Event event) {
        if (organizedEvents.contains(event.getEventID())) {
            organizedEvents = organizedEvents.replace(event.getEventID() + ",", "");
            firebase.updateUser(db.collection("users"), this, "organizedEvents", organizedEvents);
        }
        organizedEventsList.remove(event);
    }

    /**
     * Adds user to the events waitlist
     * Updates Firestore to add the event to the users waitlistedEvents, and local list
     * @param event the event to add to the users waitlistedEvents
     * */
    public void joinEventWaitList(Event event) {
        boolean check = event.enrollInWaitList(deviceId);
        if (check) {
            waitlistedEvents = waitlistedEvents + (event.getEventID()) + ",";
            firebase.updateUser(db.collection("users"), this, "waitlistedEvents", waitlistedEvents);
            waitlistedEventsList.add(event);
        }
    }

    /**
     * Removes user from the events waitlist
     * Updates Firestore to remove the event from the users waitlistedEvents, and local list
     * @param event the event to user is leaving the waitList for
     * */
    public void leaveEventWaitList(Event event) {
        boolean check = event.removeFromWaitList(deviceId);
        if (check) {
            waitlistedEvents = waitlistedEvents.replace(event.getEventID() + ",", "");
            firebase.updateUser(db.collection("users"), this, "waitlistedEvents", waitlistedEvents);
            waitlistedEventsList.remove(event);
        }
    }

    /**
     * Removes organizer access from a user
     * Updates Firestore to remove events by this user from the users organizedEvents
     * */
    public void demoteOrganizer() {
        organizer = false;
        CollectionReference eventsRef = db.collection("events");
        for (Event event : organizedEventsList) {
            firebase.deleteEvent(event, eventsRef);
        }
        firebase.updateUser(db.collection("users"), this, "organizedEvents", "");
        organizedEventsList.clear();
    }

    /**
     * Initialize events and notifications preference of a user
     * */
    public void initializeEvents() {
        this.organizedEventsList = new ArrayList<Event>();
        this.invitedEventsList = new ArrayList<Event>();
        this.waitlistedEventsList = new ArrayList<Event>();
        CollectionReference usersRef = db.collection("users");
        usersRef.document(deviceId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d("Firestore", "DocumentSnapshot data: " + document.getData());
                                cancelledEvents   = document.getString("cancelledEvents");
                                organizedEvents   = document.getString("organizedEvents");
                                invitedEvents     = document.getString("invitedEvents");
                                waitlistedEvents  = document.getString("waitlistedEvents");

                                // ─────────────────────────────────────────────────
                                // US 01.04.03
                                // Load saved notification preference from Firestore.
                                // Default to true if the field has never been set.
                                // ─────────────────────────────────────────────────
                                Boolean savedPref = document.getBoolean("notificationsEnabled");
                                notificationsEnabled = (savedPref == null || savedPref);

                                if (organizedEvents != null && !organizedEvents.equals("")) {
                                    readStringList(organizedEvents.split(","), organizedEventsList);
                                } else {
                                    firebase.updateUser(usersRef, User.this, "organizedEvents", "");
                                }
                                if (invitedEvents != null && !invitedEvents.equals("")) {
                                    readStringList(invitedEvents.split(","), invitedEventsList);
                                } else {
                                    firebase.updateUser(usersRef, User.this, "invitedEvents", "");
                                }
                                if (waitlistedEvents != null && !waitlistedEvents.equals("")) {
                                    readStringList(waitlistedEvents.split(","), waitlistedEventsList);
                                } else {
                                    firebase.updateUser(usersRef, User.this, "waitlistedEvents", "");
                                }
                            } else {
                                Log.d("Firestore", "No such document");
                            }
                        } else {
                            Log.d("Firestore", "get failed with ", task.getException());
                        }
                    }
                });
    }

    /**
     * Populates a list of events from an array of eventIDs
     * @param stringList array of eventIDs
     * @param eventList list of events
     * */
    public void readStringList(String[] stringList, ArrayList<Event> eventList) {
        if (stringList.length >= 1) {
            CollectionReference eventsRef = db.collection("events");
            for (String eventID : stringList) {
                if (eventID != null && !eventID.equals("")) {
                    Log.d("populating user event list", eventID);
                    eventsRef.document(eventID).get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot doc = task.getResult();
                                        if (doc.exists()) {
                                            String title         = doc.getString("title");
                                            String description   = doc.getString("description");
                                            String startDate     = doc.getString("startDate");
                                            String endDate       = doc.getString("endDate");
                                            String dateEvent     = doc.getString("dateEvent");
                                            int maxCapacity      = (doc.getLong("maxCapacity")).intValue();
                                            int waitingListLimit = (doc.getLong("waitingListLimit")).intValue();
                                            double price         = doc.getDouble("price");
                                            boolean geoLocation = doc.getBoolean("geoLocation");
                                            String poster        = doc.getString("poster");
                                            String eventID2      = doc.getString("eventID");
                                            String eventLocation = doc.getString("eventLocation");
                                            String organizerID   = doc.getString("organizerID");
                                            String tag           = doc.getString("tag");

                                            Event event = new Event(title, description, startDate, endDate,
                                                    dateEvent, maxCapacity, waitingListLimit, price,
                                                    geoLocation, poster, eventID2, eventLocation, organizerID,
                                                    tag);

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

    public String getCoOrganizerInvites() {
        return coOrganizerInvites;
    }

    public void setCoOrganizerInvites(String coOrganizerInvites) {
        this.coOrganizerInvites = coOrganizerInvites;
    }

    /**
     * Adds a co-organizer invite to the user
     * Updates Firestore to add the event to the users coOrganizerInvites
     * @param eventId the id of the event to add
     * */
    public void addCoOrganizerInvite(String eventId) {
        if (coOrganizerInvites == null) coOrganizerInvites = "";

        if (!coOrganizerInvites.contains(eventId)) {
            coOrganizerInvites = coOrganizerInvites + eventId + ",";

            if (db != null) {
                db.collection("users").document(deviceId)
                        .update("coOrganizerInvites", coOrganizerInvites);
            }
        }
    }

    /**
     * Removes a co-organizer invite from the user
     * Updates Firestore to remove the event from the users coOrganizerInvites
     * @param eventId the id of the event to remove
     * */
    public void removeCoOrganizerInvite(String eventId) {
        if (coOrganizerInvites != null && coOrganizerInvites.contains(eventId)) {
            coOrganizerInvites = coOrganizerInvites.replace(eventId + ",", "");

            if (db != null) {
                db.collection("users").document(deviceId)
                        .update("coOrganizerInvites", coOrganizerInvites);
            }
        }
    }
}