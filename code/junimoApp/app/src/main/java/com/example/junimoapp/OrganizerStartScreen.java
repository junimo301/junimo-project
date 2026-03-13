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

public class OrganizerStartScreen extends AppCompatActivity {
    //Create and edit event
    Button createEventButton;
    //view my events
    private RecyclerView scrollable;
    private ListOfMyEvents myEvents;
    private List<OrganizerEvent> eventList;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_start_screen);

        //----------CREATE EVENTS-------------------
        createEventButton = findViewById(R.id.create_event_button);
        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createNewEvent = new Intent(OrganizerStartScreen.this, CreateEvent.class);
                startActivity(createNewEvent);
            }
        });

        //----------VIEW MY EVENTS-------------------
        scrollable = findViewById(R.id.scrollable);
        scrollable.setLayoutManager(new LinearLayoutManager(this));
        eventList = EventData.listOfEvents();
        myEvents = new ListOfMyEvents(eventList);
        scrollable.setAdapter(myEvents);
    }

    @Override
    protected void onResume() {
        super.onResume();
        eventList= EventData.listOfEvents();
        myEvents = new ListOfMyEvents((eventList));
        scrollable.setAdapter(myEvents);
    }
}
