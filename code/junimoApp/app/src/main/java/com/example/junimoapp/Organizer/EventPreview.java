package com.example.junimoapp.Organizer;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;


import com.example.junimoapp.R;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Allows you to see the preview of an event before it is published
 *   - How it will look to users
 * Organizer can go back to editing or upload the event back on the create/edit screen
 * */
public class EventPreview extends AppCompatActivity {
    /*
     * Allows you to see the preview of an event before it is published
     *   - How it will look to users
     * Can test how an event will look and its details
     *
     * Goes back the create/edit event if needs changing or ready to upload
     *
     * User story: US 02.05.02 As an organizer I want to set the system to sample a specified number of attendees to register for the event.
     *
     */
    Button backButton;
    /** Views the event info */
    TextView title, description, startDate, endDate, dateEvent, eventLocation, maxCapacity, waitingList, price, geoLocation, poster;
    private FirebaseFirestore db;

    /**
     * called when activty is first created
     * displays the event data from CreateEvent as a "preview" before upload
     * @param savedInstanceState
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_preview);
        db = FirebaseFirestore.getInstance();


        //
        title = findViewById(R.id.preview_title);
        description = findViewById(R.id.preview_description);
        startDate = findViewById(R.id.preview_start_date);
        endDate = findViewById(R.id.preview_end_date);
        dateEvent = findViewById(R.id.preview_date);
        eventLocation = findViewById(R.id.preview_event_location);
        maxCapacity = findViewById(R.id.preview_max_capacity);
        waitingList = findViewById(R.id.preview_waiting_list);
        price = findViewById(R.id.preview_price);
        geoLocation = findViewById(R.id.preview_geo_location);
        //poster = findViewById(R.id.preview_poster);
        //button id
        backButton = findViewById(R.id.back_button);


        //get data from CreateEvent
        Intent createEventData = getIntent();
        //e = event
        String eTitle = createEventData.getStringExtra("title");
        String eDescription = createEventData.getStringExtra("description");
        String eStartDate = createEventData.getStringExtra("startDate");
        String eEndDate = createEventData.getStringExtra("endDate");
        String eDateEvent = createEventData.getStringExtra("dateEvent");
        String eEventLocation = createEventData.getStringExtra("eventLocation");
        int eMaxCapacity = createEventData.getIntExtra("maxCapacity", 0);
        String eWaitingList = createEventData.getStringExtra("waitingListLimit");
        double ePrice = createEventData.getDoubleExtra("price",0);
        String eGeoLocation = createEventData.getStringExtra("geoLocation_string");
        //String ePoster = createEventData.getStringExtra("poster");


        //display
        title.setText(eTitle);
        description.setText(eDescription);
        startDate.setText(eStartDate);
        endDate.setText(eEndDate);
        dateEvent.setText(eDateEvent);
        eventLocation.setText(eEventLocation);
        maxCapacity.setText(String.valueOf(eMaxCapacity));
        waitingList.setText(eWaitingList);
        price.setText(String.valueOf(ePrice));
        geoLocation.setText(eGeoLocation);
        //poster.setText(ePoster);


        /** returns to creating/editing the event */
        backButton.setOnClickListener(view -> {
            finish();
        });



    }






}
