package com.example.junimoapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.junimoapp.R;
//import com.example.junimoapp.TestData.EventTestData;

import com.example.junimoapp.Organizer.CreateEvent;
import com.example.junimoapp.Organizer.EventData;
import com.example.junimoapp.Organizer.ListOfMyEvents;
import com.example.junimoapp.Organizer.SelectAnEvent;
import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.models.Event;
import com.example.junimoapp.models.User;
import com.example.junimoapp.models.UserSession;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Home screen for organizer
 *  - create events
 *  - view entrants
 *  - view my events
 *  - edit events
 * */
public class OrganizerStartScreen extends AppCompatActivity {
    //Create and edit event
    ImageButton createEventButton;
    Button viewEntrantsButton;
    //view my events
    private RecyclerView scrollable;
    private ListOfMyEvents myEvents;
    private List<Event> eventList;
    private FirebaseFirestore db;


    /**
     * when activity is first created
     * listeners for create event and view entrants buttons
     * */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_start_screen);

        /** create events */
        //----------CREATE EVENTS-------------------
        createEventButton = findViewById(R.id.create_event_button);
        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createNewEvent = new Intent(OrganizerStartScreen.this, CreateEvent.class);
                startActivity(createNewEvent);
            }
        });

        /** View entrants */
        //----------VIEW ENTRANTS-------------------
        viewEntrantsButton = findViewById(R.id.view_entrants_button);
        viewEntrantsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewEntrants = new Intent(OrganizerStartScreen.this, SelectAnEvent.class);
                startActivity(viewEntrants);
            }
        });


        /** View My Events */
        //----------VIEW MY EVENTS-------------------
        scrollable = findViewById(R.id.scrollable);
        scrollable.setLayoutManager(new LinearLayoutManager(this));

        eventList= new ArrayList<>();

        myEvents = new ListOfMyEvents(eventList);
        scrollable.setAdapter(myEvents);

        db = FirebaseManager.getDB();
        loadEvents();
    }

    /**
     * runs when activity is resumed
     *  - coming back from another activity
     * updates event list
     * */
    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
        myEvents = new ListOfMyEvents((eventList));
        scrollable.setAdapter(myEvents);
    }
    private void loadEvents() {
        Log.d("organizer browse activity","on load events of browse activity");
        db.collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // clear any old data before loading fresh results
                    eventList.clear();
                    User currentUser = UserSession.getCurrentUser(); //get current user

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
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

                        Event event = new Event(title, description, startDate, endDate, dateEvent, maxCapacity, waitingListLimit, price, geoLocation, poster, eventID, eventLocation, organizerID);
                        if(organizerID != null){
                            if(event.getOrganizerID().equals(currentUser.getDeviceId())) {
                                eventList.add(event);
                                EventData.addOrEditEvent(event);
                            }
                        }
                        else {
                            Log.d("organizer browse activity","organizer ID was null in event");
                        }

                        Log.d("organizer browse activity",eventList.toString());
                    }
                    myEvents.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // log the error for debugging in Logcat
                    Log.e("Firebase", "Failed to load events", e);
                    // show a brief message to the admin so they know something went wrong
                    Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                });

    }
}
