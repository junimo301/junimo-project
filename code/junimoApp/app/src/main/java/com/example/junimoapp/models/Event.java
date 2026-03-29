package com.example.junimoapp.models;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.junimoapp.firebase.FirebaseManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    /** the date of the event */
    private String dateEvent;

    /** the max capacity of entrants for the event */
    private int maxCapacity;

    /** the waiting list limit of entrants for the event */
    private int waitingListLimit;
    private double price;

    /** unique QR code for the event */
    private String QRCode = null;

    /** the location of the entrants for the event */
    private GeoPoint geoLocation;

    /** the location of the event */
    private String eventLocation;

<<<<<<< HEAD
    /** the poster/image for the event saved as a url */
    private String poster;  //event images

=======
    /** the poster/image for the event */
    private String poster;
>>>>>>> origin/main

    /** list of the user id who are on the waiting list */
    private String waitList;

    /** the organizer id */
    private String organizerID;

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

    FirebaseManager firebase = new FirebaseManager();
    FirebaseFirestore db = firebase.getDB();


    /**
     * Constructs event with all the details
     * @param title
     * @param description
     * @param startDate
     * @param endDate
     * @param dateEvent
     * @param maxCapacity
     * @param waitingListLimit
     * @param price
     * @param geoLocation
     * @param eventLocation
     * @param poster
     * @param eventID
     * @param organizerID
     */
    public Event(String title, String description, String startDate, String endDate,
                 String dateEvent, int maxCapacity, int waitingListLimit, double price,
                 GeoPoint geoLocation, String poster, String eventID,
                 String eventLocation, String organizerID) {
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
        initializeWaitlist();
        this.organizerID = organizerID;
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

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
        // Persist to Firestore so all screens that load events see the flag
        db.collection("events").document(eventID).update("isPrivate", aPrivate);
    }

    // ── Existing getters / setters (unchanged) ────────────────────────────

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

    public String getQRCode() {
        return QRCode;
    }

    public void setQRCode(String QRCode) {
        this.QRCode = QRCode;
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

    public GeoPoint getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(GeoPoint geoLocation) {
        this.geoLocation = geoLocation;
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
     * Add user ID to waitList
     * @param accountId user device id
     * @return true on success, false if already enrolled
     */
    public boolean enrollInWaitList(String accountId) {
        if (waitList.contains(accountId)) {
            return false;
        } else {
            waitList = waitList + accountId + ",";
            firebase.updateEvent(db.collection("events"), this, "waitlist", waitList);
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
            firebase.updateEvent(db.collection("events"), this, "waitlist", waitList);
            return true;
        } else {
            return false;
        }
    }

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
}
