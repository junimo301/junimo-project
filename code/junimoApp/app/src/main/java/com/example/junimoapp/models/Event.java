package com.example.junimoapp.models;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

public class Event {
    private String eventID;
    private String title;
    private String description;
    private String startDate;  //registration period, use Date later
    private String endDate;    //registration period, use Date later
    private String dateEvent;   //date of the event
    private int maxCapacity; //event limit
    private int waitingListLimit;
    private double price;
    private String QRCode = null; //generate QR code for events
    private GeoPoint geoLocation; //entrants location, use Location later
    private String eventLocation; //for event location
    private String poster;  //event images

    private ArrayList<String> waitList;
    private String organizerID;


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
    public ArrayList<String> getWaitList() {
        return waitList;
    }

    public void setWaitList(ArrayList<String> waitList) {
        this.waitList = waitList;
    }

    //Register for wait list of event returns true/false on success/failure
    public boolean Register(String account){
        if(waitList.contains(account)){
            return false;
        }
        else {
            waitList.add(account);
            return true;
        }
    }
}
