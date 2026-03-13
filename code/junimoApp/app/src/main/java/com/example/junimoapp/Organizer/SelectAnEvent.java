package com.example.junimoapp.Organizer;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.R;
import com.example.junimoapp.models.Event;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class SelectAnEvent extends AppCompatActivity {
    /*
    * Select and event that you want to view the entrant information for
    *
    */

    FirebaseFirestore db;
    LinearLayout eventList;
    List<Event> myEvents = EventData.getEvents();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_event);

        db = FirebaseFirestore.getInstance();
        eventList = findViewById(R.id.event_list);

        //buttons
        for (Event events: myEvents) {
            Button eventbutton = new Button(this);
            eventbutton.setText(events.getTitle());
            LinearLayout.LayoutParams parameters = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            eventbutton.setLayoutParams(parameters);

            //go to entrant info
            eventbutton.setOnClickListener(v -> {
                Intent viewEntrants = new Intent(SelectAnEvent.this, Entrants.class);
                viewEntrants.putExtra("event_ID", events.getEventID());
                startActivity(viewEntrants);
            });
            eventList.addView(eventbutton);

        }

    }

}
