package com.example.junimoapp.Organizer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.junimoapp.R;
import com.example.junimoapp.models.Event;

import java.util.List;

public class OrganizerStartScreen extends AppCompatActivity {
    //Create and edit event
    Button createEventButton;
    Button editEventButton;
    //HARDCODED ID FOR TESTING
    String currentEventID = "test-event-id-1234";

    //view my events
    private RecyclerView scrollable;
    private ListOfMyEvents myEvents;
    private List<Event> eventList;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_start_screen);

        //----------CREATE AND EDIT EVENTS-------------------
        createEventButton = findViewById(R.id.create_event_button);
        editEventButton = findViewById(R.id.edit_event_button);

        editEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editCurrentEvent = new Intent(OrganizerStartScreen.this, CreateEvent.class);
                editCurrentEvent.putExtra("event_Id", currentEventID);
                startActivity(editCurrentEvent);
            }
        });

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
        eventList.clear();
        eventList.addAll(EventData.listOfEvents());
        myEvents.notifyDataSetChanged();
    }
}
