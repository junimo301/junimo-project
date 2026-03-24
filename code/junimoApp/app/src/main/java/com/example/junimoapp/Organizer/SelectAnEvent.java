package com.example.junimoapp.Organizer;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.OrganizerStartScreen;
import com.example.junimoapp.R;
import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.models.Event;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Allows organizer to select an event to view the entrant information for
 *  - organizer clicks and event
 *  - opens Entrants screen to view entrant names
 * Organizer can go back to the organizer start screen
 * */
public class SelectAnEvent extends AppCompatActivity {
    /*
    * Select and event that you want to view the entrant information for
    *
    */

    FirebaseFirestore db;
    LinearLayout eventList;
    List<Event> myEvents = EventData.getEvents();
    TextView backButton;


    /**
     * called when activty is first created
     * displays the events that the organizer has created
     * @param savedInstanceState
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_event);

        backButton = findViewById(R.id.back_button);

        db = FirebaseManager.getDB();
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

        // returns to select organizer home screen
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(SelectAnEvent.this, OrganizerStartScreen.class);
                startActivity(intent);            }
        });

    }

}
