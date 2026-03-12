package com.example.junimoapp.models;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.junimoapp.models.Event;
import com.example.junimoapp.firebase.FirebaseManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;

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
            usersRef.document(deviceID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d("Firestore", "DocumentSnapshot data: " + document.getData());
                            User user = new User(deviceID, document.getString("name"),document.getString("email"),document.getString("phone"));
                            users.add(user);
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
