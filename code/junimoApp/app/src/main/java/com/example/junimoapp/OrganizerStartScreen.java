package com.example.junimoapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.junimoapp.Organizer.MapActivity;
import com.example.junimoapp.R;

import com.example.junimoapp.Organizer.CreateEvent;
import com.example.junimoapp.Organizer.EventData;
import com.example.junimoapp.Organizer.ListOfMyEvents;
import com.example.junimoapp.Organizer.SelectAnEvent;
import com.example.junimoapp.Organizer.organizerNotifications;
import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.models.Event;
import com.example.junimoapp.models.User;
import com.example.junimoapp.models.UserSession;
import com.example.junimoapp.utils.BaseActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Home screen for organizer
 *  - create events
 *  - view entrants
 *  - view my events
 *  - edit events
 * */
public class OrganizerStartScreen extends BaseActivity {
    //Create and edit event
    ImageButton createEventButton;
    Button viewEntrantsButton, mapButton;
    TextView backButton;
    ImageButton settingsButton;
    Button notificationsButton;
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
        /** settings button */
        settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v->{
            Intent intent = new Intent(OrganizerStartScreen.this, ProfileActivity.class);
            intent.putExtra("new", false);
            intent.putExtra("organizer",true);
            startActivity(intent);
        });
        /** back button*/
        backButton = findViewById(R.id.backToHomeText);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createNewEvent = new Intent(OrganizerStartScreen.this, MainActivity.class);
                startActivity(createNewEvent);
            }
        });

        //----------CREATE EVENTS-------------------
        createEventButton = findViewById(R.id.create_event_button);
        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createNewEvent = new Intent(OrganizerStartScreen.this, CreateEvent.class);
                startActivity(createNewEvent);
            }
        });

        //----------MAP OF ENTRANTS----------------------------
        mapButton = findViewById(R.id.map_button);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewMap = new Intent(OrganizerStartScreen.this, SelectAnEvent.class);
                viewMap.putExtra("go to", "map");
                startActivity(viewMap);
            }
        });

        notificationsButton = findViewById(R.id.notifications_button);
        notificationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewNotifications = new Intent(OrganizerStartScreen.this, organizerNotifications.class);
                startActivity(viewNotifications);
            }
        });

        /** View entrants */
        //----------VIEW ENTRANTS-------------------
        viewEntrantsButton = findViewById(R.id.view_entrants_button);
        viewEntrantsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewEntrants = new Intent(OrganizerStartScreen.this, SelectAnEvent.class);
                viewEntrants.putExtra("go to", "entrants");
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
    }
    private void loadEvents() {
        Log.d("organizer browse activity", "on load events of browse activity");
        db.collection("events").get().addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear();
                    User currentUser = UserSession.getCurrentUser();
                    if (currentUser == null) {
                        Log.d("organizer browse activity", "user was null in event");
                        return;
                    }

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String title = doc.getString("title");
                        String description = doc.getString("description");
                        String startDate = doc.getString("startDate");
                        String endDate = doc.getString("endDate");
                        String dateEvent = doc.getString("dateEvent");
                        Long capacity = doc.getLong("maxCapacity");
                        int maxCapacity = capacity != null ? capacity.intValue() : 0;
                        Long limit = doc.getLong("waitingListLimit");
                        int waitingListLimit = limit != null ? limit.intValue() : 0;
                        Double priceObj = doc.getDouble("price");
                        double price = priceObj != null ? priceObj : 0.0;
                        Boolean geoLocationObj = doc.getBoolean("geoLocation");
                        boolean geoLocation = Boolean.TRUE.equals(geoLocationObj);
                        String poster = doc.getString("poster");
                        String eventID = doc.getString("eventID");
                        String eventLocation = doc.getString("eventLocation");
                        String organizerID = doc.getString("organizerID");
                        if (title == null || eventID == null) continue;
                        String tag = doc.getString("tag");

                        Event event = new Event(title, description, startDate, endDate, dateEvent,
                                maxCapacity, waitingListLimit, price, geoLocation,
                                poster, eventID, eventLocation, organizerID, tag);

                        if (organizerID != null &&
                                event.getOrganizerID().equals(currentUser.getDeviceId())) {
                            eventList.add(event);
                            EventData.addOrEditEvent(event);
                        }
                    }

                    // ─────────────────────────────────────────────────────
                    // Rebuild adapter INSIDE the success listener so it only
                    // runs after Firestore data has actually returned —
                    // not before like it was in onResume()
                    // ─────────────────────────────────────────────────────
                    myEvents = new ListOfMyEvents(eventList);
                    scrollable.setAdapter(myEvents);
                    myEvents.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Failed to load events", e);
                    Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }
}
