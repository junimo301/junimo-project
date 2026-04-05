package com.example.junimoapp.Organizer;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;


import com.bumptech.glide.Glide;
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
    TextView title, description, startDate, endDate, dateEvent, eventLocation, maxCapacity, waitingList, price;
    private ImageView eventPoster;
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

        //event details
        title         = findViewById(R.id.preview_title);
        description   = findViewById(R.id.preview_description);
        startDate     = findViewById(R.id.preview_start_date);
        endDate       = findViewById(R.id.preview_end_date);
        dateEvent     = findViewById(R.id.preview_date);
        eventLocation = findViewById(R.id.preview_event_location);
        maxCapacity   = findViewById(R.id.preview_max_capacity);
        waitingList   = findViewById(R.id.preview_waiting_list);
        price         = findViewById(R.id.preview_price);
        eventPoster   = findViewById(R.id.event_poster);
        backButton    = findViewById(R.id.back_button);

        //get data from CreateEvent
        Intent createEventData = getIntent();
        //e = event
        String eTitle         = createEventData.getStringExtra("title");
        String eDescription   = createEventData.getStringExtra("description");
        String eStartDate     = createEventData.getStringExtra("startDate");
        String eEndDate       = createEventData.getStringExtra("endDate");
        String eDateEvent     = createEventData.getStringExtra("dateEvent");
        String eEventLocation = createEventData.getStringExtra("eventLocation");
        String eMaxCapacity      = createEventData.getStringExtra("maxCapacity");
        String eWaitingList   = createEventData.getStringExtra("waitingListLimit");
        String ePrice         = createEventData.getStringExtra("price");
        String ePoster        = createEventData.getStringExtra("poster");
        String ePosterURI     = createEventData.getStringExtra("posterURI");

        if (ePoster != null && !ePoster.isEmpty()) {
            Glide.with(this).load(ePoster)
                    .placeholder(R.drawable.bg_event_tile)
                    .error(R.drawable.bg_event_tile)
                    .into(eventPoster);
        } else if (ePosterURI != null) {
            Glide.with(this).load(Uri.parse(ePosterURI))
                    .placeholder(R.drawable.bg_event_tile)
                    .error(R.drawable.bg_event_tile)
                    .into(eventPoster);
        } else {
            Glide.with(this).load((String)null)
                    .placeholder(R.drawable.bg_event_tile)
                    .error(R.drawable.bg_event_tile)
                    .into(eventPoster);
        }

        //display
        title.setText(eTitle);
        description.setText(eDescription);
        startDate.setText(eStartDate);
        endDate.setText(eEndDate);
        dateEvent.setText(eDateEvent);
        eventLocation.setText(eEventLocation);
        maxCapacity.setText(eMaxCapacity);
        waitingList.setText(eWaitingList);
        price.setText(ePrice);
        //poster.setText(ePoster);


        // returns to creating/editing the event
        backButton.setOnClickListener(view -> {
            finish();
        });
    }
}
