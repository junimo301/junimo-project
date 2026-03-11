package com.example.junimoapp.models;

import com.example.junimoapp.models.Event;

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
    }

    public void populateWaitList(Event event){
        ArrayList<String> deviceIDs=event.getWaitList();
        for(String deviceID : deviceIDs){

            //find users by id and add them as users to waitList
        }
    }
}
