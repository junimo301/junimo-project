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
    private String startDate;  //registration period, use Date later
    private String endDate;    //registration period, use Date later

    /** the date of the event */
    private String dateEvent;   //date of the event

    /** the max capacity of entrants for the event */
    private int maxCapacity; //event limit

    /** the waiting list limit of entrants for the event */
    private int waitingListLimit;
    private double price;

    /** unique QR code for the event */
    private String QRCode = null; //generate QR code for events

    /** the location of the entrants for the event */
    private GeoPoint geoLocation; //entrants location, use Location later

    /** the location of the event */
    private String eventLocation; //for event location

    /** the poster/image for the event */
    private String poster;  //event images


    /** list of the user id who are on the waiting list */
    private ArrayList<String> waitList;

    /** the organizer id */
    private String organizerID;
    FirebaseManager firebase = new FirebaseManager();
    FirebaseFirestore db = firebase.getDB();


    /**
     * constructs event with all the details
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
     * */
    public Event(String title, String description, String startDate, String endDate, String dateEvent, int maxCapacity, int waitingListLimit, double price, GeoPoint geoLocation, String poster, String eventID, String eventLocation, String organizerID) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dateEvent = dateEvent;
        this.maxCapacity = maxCapacity;
        this.waitingListLimit = waitingListLimit;
        this.price = price;
        //this.QRCode = QRCode;
        this.geoLocation = geoLocation;
        this.eventLocation = eventLocation;
        this.poster = poster;
        this.eventID = eventID;

        this.waitList = new ArrayList<String>();
        initializeWaitlist();
        this.organizerID = organizerID;
    }

    //setters and getters
    public ArrayList<String> getWaitList() {
        return waitList;
    }

    public void setWaitList(ArrayList<String> waitList) {
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

    //Methods

    /**
     * Add user ID to waitList array
     * @param accountId
     * user device id
     * @return
     * true or false on success or failure
     */
    public boolean enrollInWaitList(String accountId){
        if(waitList.contains(accountId)){
            return false;
        }
        else {
            waitList.add(accountId);
            firebase.updateEvent(db.collection("events"),this,"waitlist",waitList);
            return true;
        }

    }

    /**
     * Remove user id from waitlist array
     * @param accountId
     * user device id
     * @return
     * true/false on success/failure
     */
    public boolean removeFromWaitList(String accountId) {
        if(waitList.contains(accountId)){
            waitList.remove(accountId);
            firebase.updateEvent(db.collection("events"),this,"waitlist",waitList);
            return true;
        }
        else {
            return false;
        }
    }

    public void initializeWaitlist() {
        db.collection("events").document(eventID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<String> data = document.get("waitlist", ArrayList.class);
                        if(data!=null) {
                            for (String item : data) {
                                if (item != null) {
                                    waitList.add(item);
                                    Log.d("Firestore", "added user to waitlist" + item);
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
