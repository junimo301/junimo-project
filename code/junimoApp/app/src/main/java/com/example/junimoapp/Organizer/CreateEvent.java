package com.example.junimoapp.Organizer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.junimoapp.R;
import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.models.Event;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.GeoPoint;

import androidx.appcompat.app.AppCompatActivity;

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
    Button uploadNewEvent;
    private Event createdEvent = null;

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
        uploadNewEvent = findViewById(R.id.upload_event_button);

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
                editGeoLocation.setText(createdEvent.getGeoLocation().toString());
                editEventLocation.setText(createdEvent.getEventLocation().toString());
                editPoster.setText(createdEvent.getPoster());
            }
        }


        //buttons
        uploadNewEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //optional fields
                String description = editDescription.getText().toString();
                String startDate = editStartDate.getText().toString();
                String endDate = editEndDate.getText().toString();
                String title = editTitle.getText().toString();
                String poster = editPoster.getText().toString();
                Integer waitingListLimit = null;
                if (!editWaitingList.getText().toString().isEmpty()) {
                    waitingListLimit = Integer.parseInt(editWaitingList.getText().toString());
                }
                //required fields
                if (title.isEmpty()) {
                    editTitle.setError("*Field Required*");
                    editTitle.requestFocus();
                    return;
                }
                String geoLocation_string = editGeoLocation.getText().toString();
                GeoPoint geoLocation = new GeoPoint(00000,00000);//needs to be fixed
                if (geoLocation_string.isEmpty()) {
                    editGeoLocation.setError("*Field Required*");
                    editGeoLocation.requestFocus();
                    return;
                }
                String eventLocation = editEventLocation.getText().toString();
                if (eventLocation.isEmpty()) {
                    editEventLocation.setError("*Field Required*");
                    editEventLocation.requestFocus();
                    return;
                }
                if (editMaxCapacity.getText().toString().isEmpty()) {
                    editMaxCapacity.setError("*Field Required*");
                    editMaxCapacity.requestFocus();
                    return;
                } int maxCapacity = Integer.parseInt(editMaxCapacity.getText().toString());

                if (editPrice.getText().toString().isEmpty()) {
                    editPrice.setError("*Field Required*");
                    editPrice.requestFocus();
                    return;
                } double price = Double.parseDouble(editPrice.getText().toString());


                //event ID
                String eventID;
                if (createdEvent != null) {
                    eventID = createdEvent.getEventID();
                }
                else {
                    eventID = UUID.randomUUID().toString();
                }

                //creates event
                Event saveEvent = new Event(title, description, startDate, endDate, maxCapacity, waitingListLimit, price, geoLocation, poster, eventID, eventLocation);

                //add to firebase
                FirebaseManager firebase = new FirebaseManager();
                CollectionReference eventsRef=firebase.getDB().collection("events");
                firebase.addEvent(saveEvent,eventsRef);

                EventData.addOrEditEvent(saveEvent);
                Toast.makeText(CreateEvent.this, "Event Created", Toast.LENGTH_SHORT).show();
                finish();


                //TEST
                String logMessage;
                if (createdEvent != null) {
                    logMessage = "Event updated: " + saveEvent.getTitle();
                }
                else{
                    logMessage = "event created: " + saveEvent.getTitle();
                }

                Log.d("createEvent", logMessage);
            }
        });
    }
}

