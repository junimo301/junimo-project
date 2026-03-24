package com.example.junimoapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.models.Event;
import com.example.junimoapp.models.User;
import com.example.junimoapp.models.UserSession;
import com.example.junimoapp.utils.DeviceUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * user stories implemented:
 *  - US 01.06.02: Entrant wants to be able to sign up for a waiting list from event details.
 */

/**
 * Provides details for events, such as the eventID, waitlists
 * Allows user to join wait list and leave waitlist
 */

public class EventDetailsActivity extends AppCompatActivity {

    FirebaseFirestore db;
    String deviceId;
    String eventId;
    Event selectedEvent;

    TextView eventTitle;
    TextView descriptionText;
    TextView organizerText;
    TextView eventDate;
    TextView eventLocationText;
    TextView priceText;
    TextView registrationDetails;
    TextView capacity;
    TextView countOnList;

    TextView backButton;
    Button joinWaitlistButton;
    Button acceptButton;
    Button declineButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.indivdual_invite_page);

        db = FirebaseManager.getDB();
        deviceId = DeviceUtils.getDeviceId(this);

        //eventId should be passed from previous activity
        eventId = getIntent().getStringExtra("eventId");

        db.collection("events").document(eventId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    //Fields in events
                    String title = doc.getString("title");
                    String description = doc.getString("description");
                    String startDate = doc.getString("startDate");
                    String endDate = doc.getString("endDate");
                    String dateEvent = doc.getString("dateEvent");
                    int maxCapacity = (doc.getLong("maxCapacity")).intValue();
                    int waitingListLimit = (doc.getLong("waitingListLimit")).intValue();
                    double price = doc.getDouble("price");
                    GeoPoint geoLocation = doc.getGeoPoint("geoLocation"); //geoPoint is a type apparently? seems helpful??
                    String poster = doc.getString("poster");
                    String eventID = doc.getString("eventID");
                    String eventLocation = doc.getString("eventLocation");
                    String organizerID = doc.getString("organizerID");

                    selectedEvent=(new Event(title, description, startDate, endDate, dateEvent, maxCapacity, waitingListLimit, price, geoLocation, poster, eventID, eventLocation, organizerID));
                    Log.d("Firebase",selectedEvent.toString());

                    eventTitle.setText(selectedEvent.getTitle());
                    descriptionText.setText(selectedEvent.getDescription());
                    organizerText.setText(selectedEvent.getOrganizerID());
                    eventDate.setText(selectedEvent.getDateEvent());
                    eventLocationText.setText(selectedEvent.getEventLocation());
                    priceText.setText(String.valueOf(selectedEvent.getPrice()));
                    String registration = selectedEvent.getStartDate()+"\n"+selectedEvent.getEndDate();
                    registrationDetails.setText(registration);
                    capacity.setText(String.valueOf(selectedEvent.getMaxCapacity()));
                    String waitlistText = String.valueOf(selectedEvent.getWaitList().split(",").length);
                    if(selectedEvent.getMaxCapacity()>0){
                        waitlistText = waitlistText+"/"+String.valueOf(selectedEvent.getMaxCapacity());
                    }
                    countOnList.setText(waitlistText);
                }
                else {
                    Log.d("Firestore", "Error getting documents: ", task.getException());
                }
            }
        });

        backButton = findViewById(R.id.backToInvitesText);
        joinWaitlistButton = findViewById(R.id.joinWaitlistButton);
        acceptButton = findViewById(R.id.acceptButton);
        declineButton = findViewById(R.id.declineButton);

        eventTitle = findViewById(R.id.eventTitle);
        descriptionText = findViewById(R.id.descriptionText);
        organizerText = findViewById(R.id.organizerText);
        eventDate = findViewById(R.id.eventDate);
        eventLocationText = findViewById(R.id.eventLocation);
        priceText = findViewById(R.id.price);
        registrationDetails = findViewById(R.id.registrationDetails);
        capacity = findViewById(R.id.capacity);
        countOnList = findViewById(R.id.countOnList);

        backButton.setOnClickListener(v -> back());
        joinWaitlistButton.setOnClickListener(v -> JoinWaitlist(selectedEvent));
        declineButton.setOnClickListener(v -> LeaveWaitlist(selectedEvent));
    }

    private void back() {
        Intent intent = new Intent(EventDetailsActivity.this, UserHomeActivity.class);
        startActivity(intent);
    }
    private void JoinWaitlist(Event event){
        Log.d("button click","waitlist button clicked");
        String startDate= event.getStartDate();
        String endDate = event.getEndDate();
        String[] oldList = event.getWaitList().split(",");
        if(oldList.length<=event.getMaxCapacity()) {
            if (registrationPeriod(startDate, endDate)) {
                event.enrollInWaitList(deviceId);
                String updatedList = event.getWaitList();
                FirebaseManager.updateEvent(db.collection("events"), event, "waitList", updatedList);
                joinWaitlistButton.setText("ADDED TO WAITLIST");
            }
            else {
                joinWaitlistButton.setText("REGISTRATION PERIOD NOT OPEN");
            }
        }
        else {
            joinWaitlistButton.setText("WAITLIST FULL");
        }
    }
    //check if registration period is open
    public boolean registrationPeriod(String startDate, String endDate) {
        if (startDate != "" && startDate != null) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date start = format.parse(startDate);
                Date end = format.parse(endDate);
                Date now = new Date();
                return !(now.before(start) || now.after(end));
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        else {
            return true;
        }

    }
    private void LeaveWaitlist(Event event){
        event.removeFromWaitList(deviceId);
        String updatedList = event.getWaitList();
        FirebaseManager.updateEvent(db.collection("events"), event, "waitList", updatedList);
        declineButton.setText("WAITLIST LEFT");

    }

}