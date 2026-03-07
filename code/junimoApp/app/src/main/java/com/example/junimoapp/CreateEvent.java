package com.example.junimoapp;

import android.location.Location;
import android.media.metrics.Event;

import java.util.Date;

public class CreateEvent extends OrganizerStart {
    /*
    * User stories:
    * US 02.01.01 As an organizer I want to create a new event and generate a unique promotional QR code that links to the event description and event poster in the app.
    * US 02.01.04 As an organizer, I want to set a registration period.
    * US 02.03.01 As an organizer I want to OPTIONALLY limit the number of entrants who can join my waiting list.
    *
    * Create Event: title, description, date, location, max capcity, registration period, waiting list, price, geo location, poster
    *
    * */

    private String title;
    private String description;
    private Date startDate;  //registration period
    private Date endDate;    //registration period
    private Location location;
    private int maxCapacity;
    private int waitingListLimit;
    private int price;
    private Location geoLocation;
    private String poster;

    Event myEvent = new Event(title, description, startDate, endDate, location, maxCapacity, waitingListLimit, price, geoLocation, poster);



}
