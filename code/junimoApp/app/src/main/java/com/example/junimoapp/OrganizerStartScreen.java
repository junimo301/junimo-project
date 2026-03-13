package com.example.junimoapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.junimoapp.R;
//import com.example.junimoapp.TestData.EventTestData;

import com.example.junimoapp.Organizer.CreateEvent;
import com.example.junimoapp.Organizer.EventData;
import com.example.junimoapp.Organizer.ListOfMyEvents;
import com.example.junimoapp.Organizer.SelectAnEvent;
import com.example.junimoapp.models.Event;

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
    Button createEventButton;
    Button viewEntrantsButton;
    //view my events
    private RecyclerView scrollable;
    private ListOfMyEvents myEvents;
    private List<Event> eventList;


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
        eventList = EventData.listOfEvents();
        myEvents = new ListOfMyEvents(eventList);
        scrollable.setAdapter(myEvents);
    }

    /**
     * runs when activity is resumed
     *  - coming back from another activity
     * updates event list
     * */
    @Override
    protected void onResume() {
        super.onResume();
        eventList= EventData.listOfEvents();
        myEvents = new ListOfMyEvents((eventList));
        scrollable.setAdapter(myEvents);
    }
}
