package com.example.junimoapp;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.junimoapp.OrganizerEvent;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;
import java.util.UUID;

public class CreateEvent extends AppCompatActivity {
    /*
     * User stories:
     * US 02.01.01 As an organizer I want to create a new event and generate a unique promotional QR code that links to the event description and event poster in the app.
     * US 02.01.04 As an organizer, I want to set a registration period.
     * US 02.03.01 As an organizer I want to OPTIONALLY limit the number of entrants who can join my waiting list.
     *
     * Create Event: title, description, date, location, max capcity, registration period, waiting list, price, geo location, poster
     *
     * */
    EditText editTitle, editDescription, editStartDate, editEndDate, editEventLocation, editMaxCapacity, editWaitingList, editPrice, editGeoLocation, editPoster;
    Button createEventButton;
    private OrganizerEvent createdEvent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        //ids created in "dummy" xml file
        //variable ids
        editTitle = findViewById(R.id.edit_title);
        editDescription = findViewById(R.id.edit_description);
        editStartDate = findViewById(R.id.edit_start_date);
        editEndDate = findViewById(R.id.edit_end_date);
        editEventLocation = findViewById(R.id.edit_event_location);
        editMaxCapacity = findViewById(R.id.edit_max_capacity);
        editWaitingList = findViewById(R.id.edit_waiting_list);
        editPrice = findViewById(R.id.edit_price);
        editGeoLocation = findViewById(R.id.edit_geo_location);
        editPoster = findViewById(R.id.edit_poster);
        //button id
        createEventButton = findViewById(R.id.create_event_button);

        String eventID = getIntent().getStringExtra("event_Id");
        if (eventID != null) {
            createdEvent = EventData.searchEventID(eventID);

            if (createdEvent != null) {
                editTitle.setText(createdEvent.getTitle());
                editDescription.setText(createdEvent.getDescription());
                editStartDate.setText(createdEvent.getStartDate());
                editEndDate.setText(createdEvent.getEndDate());
                editMaxCapacity.setText(String.valueOf(createdEvent.getMaxCapacity()));
                editWaitingList.setText(String.valueOf(createdEvent.getWaitingListLimit()));
                editPrice.setText(String.valueOf(createdEvent.getPrice()));
                editGeoLocation.setText(createdEvent.getGeoLocation());
                editEventLocation.setText(createdEvent.getEventLocation());
                editPoster.setText(createdEvent.getPoster());
            }
        }


        //buttons
        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = editTitle.getText().toString();
                String description = editDescription.getText().toString();
                String startDate = editStartDate.getText().toString();
                String endDate = editEndDate.getText().toString();
                int maxCapacity = Integer.parseInt(editMaxCapacity.getText().toString());
                int waitingListLimit = Integer.parseInt(editWaitingList.getText().toString());
                double price = Double.parseDouble(editPrice.getText().toString());
                String geoLocation = editGeoLocation.getText().toString();
                String eventLocation = editEventLocation.getText().toString();
                String poster = editPoster.getText().toString();

                String eventID;
                if (createdEvent != null) {
                    eventID = createdEvent.getEventID();
                }
                else {
                    eventID = UUID.randomUUID().toString();
                }

                OrganizerEvent saveEvent = new OrganizerEvent(title, description, startDate, endDate, maxCapacity, waitingListLimit, price, geoLocation, poster, eventID, eventLocation);

                EventData.addOrEditEvent(saveEvent);


                //TEST
                String logMessage;
                if (createdEvent != null) {
                    logMessage = "Event updated: " + saveEvent.getTitle();
                }
                else{
                    logMessage = "event created: " + saveEvent.getTitle();
                }

                Log.d("createEvent", "Event created: " + saveEvent.getTitle());
            }
        });
    }
}

