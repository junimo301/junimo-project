package com.example.junimoapp.models;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.junimoapp.firebase.FirebaseManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.PropertyName;

/**
 * Event specific information
 *  - details that organizer inputs
 * */
public class Event {

    /** the unique id for the event */
    private String eventID;
    private String title;
    private String description;

    /** the start and end date of the registration period */
    private String startDate;
    private String endDate;

    private String dateEvent;

    private int maxCapacity;

    private int waitingListLimit;
    private double price;

    /** unique QR code for the event */
    @PropertyName("qrcode")
    private String qrcode;

    /** the location of the entrants on the waitList for the event */
    private boolean geoLocation = false;

    private String eventLocation;

    /** the poster/image for the event */
    private String poster;  //event images

    /** list of the user id who are on the waiting list */
    private String waitList;

    private String organizerID;

    private String coOrganizers;

    // ─────────────────────────────────────────────────────────────────────
    // US 02.01.02
    // Flag that marks this event as private (invite-only).
    // Private events:
    //   - do NOT appear in the public event listing
    //   - do NOT generate a promotional QR code
    //   - entrants are added manually by the organizer (see US 02.01.03)
    // Defaults to false so all existing events stay public.
    // ─────────────────────────────────────────────────────────────────────
    private boolean isPrivate = false;

    /**tag for filtering events: US 01.01.05 and 01.01.06*/
    private String tag;

    FirebaseManager firebase;
    FirebaseFirestore db;

    private String cancelledUsers;
    private String invitedUsers;
    private String enrolledUsers;

    public Event() {}

    /**
     * Constructs event with all the details
     * @param title event title
     * @param description event description
     * @param startDate event start date
     * @param endDate event end date
     * @param dateEvent event date
     * @param maxCapacity event max capacity
     * @param waitingListLimit event waiting list limit
     * @param price event price
     * @param geoLocation event geo location
     * @param eventLocation event location
     * @param poster event poster
     * @param eventID event id
     * @param organizerID organizer id
     * @param tag event tag
     */
    public Event(String title, String description, String startDate, String endDate,
                 String dateEvent, int maxCapacity, int waitingListLimit, double price,
                 boolean geoLocation, String poster, String eventID,
                 String eventLocation, String organizerID, String tag) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dateEvent = dateEvent;
        this.maxCapacity = maxCapacity;
        this.waitingListLimit = waitingListLimit;
        this.price = price;
        this.geoLocation = geoLocation;
        this.eventLocation = eventLocation;
        this.poster = poster;
        this.eventID = eventID;
        this.waitList = "";
        this.organizerID = organizerID;
        this.tag = tag;
        this.cancelledUsers = "";
        this.invitedUsers = "";
        this.enrolledUsers = "";
        this.coOrganizers = "";

        try {
            firebase = new FirebaseManager();
            db = firebase.getDB();
            initializeWaitlist();
            initializeCoOrganizers();
        } catch (Exception e) {
            //unit testing environment, we don't need or want firebase
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // US 02.01.02
    // Getter and setter for the isPrivate flag.
    // setPrivate() also persists the value to Firestore immediately so that
    // UserHomeActivity can filter out private events from the public listing.
    // ─────────────────────────────────────────────────────────────────────
    public boolean isPrivate() {
        return isPrivate;
    }
    /**
     * Sets if the event is private or not
     * updates the private flag in the Firestore database immeditly so private events can be filtered properly
     * @param aPrivate if the event is private or not
     * */
    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
        // Persist to Firestore so all screens that load events see the flag
        if (db != null) {
            db.collection("events").document(eventID).update("private", aPrivate);
        }
    }
    public void restorePrivate(boolean aPrivate) {
        this.isPrivate = aPrivate;
    }

    // ── Existing getters / setters (unchanged) ────────────────────────────
    public String getTag() { return tag; }

    public void setTag(String tag) { this.tag = tag; }

    public String getWaitList() {
        return waitList;
    }

    public void setWaitList(String waitList) {
        this.waitList = waitList;
    }

    public String getOrganizerID() {
        return organizerID;
    }

    public void setOrganizerID(String organizerID) {
        this.organizerID = organizerID;
    }

    @PropertyName("qrcode")
    public String getQRCode() {
        return qrcode;
    }
    @PropertyName("qrcode")
    public void setQRCode(String QRCode) {
        this.qrcode = QRCode;
    }

    public String getDateEvent() {
        return dateEvent;
    }

    public void setDateEvent(String dateEvent) {
        this.dateEvent = dateEvent;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public int getWaitingListLimit() {
        return waitingListLimit;
    }

    public void setWaitingListLimit(int waitingListLimit) {
        this.waitingListLimit = waitingListLimit;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setGeoLocation(boolean geoLocation) {
        this.geoLocation = geoLocation;
    }
    public boolean isGeoLocation() {
        return geoLocation;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    // ── Existing methods (unchanged) ──────────────────────────────────────
    /**
     * Add user ID to invitedUsers
     * @param deviceID device id of user
     */
    public void Invite(String deviceID) {
        if (!invitedUsers.contains(deviceID)) {
            invitedUsers = invitedUsers + deviceID + ",";
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("events").document(eventID).update("invitedUsers", invitedUsers);
        }
    }

    /**
     * Add user ID to waitList
     * @param accountId user device id
     * @return true on success, false if already enrolled
     */
    public boolean enrollInWaitList(String accountId) {

        //block co-orgs
        if (isCoOrganizer(accountId)) {
            return false;
        }

        if (waitList.contains(accountId)) {
            return false;
        } else {
            waitList = waitList + accountId + ",";
            if (db != null) {
                firebase.updateEvent(db.collection("events"), this, "waitlist", waitList);
            }
            return true;
        }
    }

    /**
     * Remove user id from waitlist
     * @param accountId user device id
     * @return true on success, false if not found
     */
    public boolean removeFromWaitList(String accountId) {
        if (waitList.contains(accountId)) {
            waitList = waitList.replace(accountId + ",", "");
            if (db != null) {
                firebase.updateEvent(db.collection("events"), this, "waitlist", waitList);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Initialize waitlist from Firestore
     * */
    public void initializeWaitlist() {
        db.collection("events").document(eventID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String data = document.getString("waitlist");
                                if (data != null) {
                                    for (String item : data.split(",")) {
                                        if (item != null && !item.isEmpty()) {
                                            waitList = waitList + item + ",";
                                            Log.d("Firestore", "added user to waitlist " + item);
                                        }
                                    }
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
     * Adds a user as a co organizer to a event
     * If user is on waitlist, they are removed and added to co-organizers
     * Added to firestore as such
     * @param userId user device id to add as coorganizer
     * */
    public void addCoOrganizer(String userId) {
        if (coOrganizers == null) coOrganizers = "";

        if (!coOrganizers.contains(userId)) {

            removeFromWaitList(userId);

            coOrganizers = coOrganizers + userId + ",";

            if (db != null) {
                db.collection("events").document(eventID)
                        .update("coOrganizers", coOrganizers);
            }
        }
    }

    /**
     * Removes a user as a co organizer from an event
     * Updates firestore coOrganizer list
     * @param userId user device id to remove as coorganizer
     * */
    public void removeCoOrganizer(String userId) {
        if (coOrganizers != null && coOrganizers.contains(userId)) {
            coOrganizers = coOrganizers.replace(userId + ",", "");

            if (db != null) {
                db.collection("events").document(eventID)
                        .update("coOrganizers", coOrganizers);
            }
        }
    }

    /**
     * Checks whether a user is a co-organizer for an event
     * @param userId user device id to check if a user is a coorganizer
     * @return true if user is a coorganizer, false otherwise
     * */
    public boolean isCoOrganizer(String userId) {
        return coOrganizers != null && coOrganizers.contains(userId);
    }

    /**
     * Initializes co-organizers from Firestore
     * */
    public void initializeCoOrganizers() {
        db.collection("events").document(eventID).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String data = document.getString("coOrganizers");
                            if (data != null) {
                                coOrganizers = data;
                            }
                        }
                    }
                });
    }
}
