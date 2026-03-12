package com.example.junimoapp.Organizer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.junimoapp.MainActivity;
import com.example.junimoapp.OrganizerStartScreen;
import com.example.junimoapp.R;
import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.models.Event;
import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.models.User;
import com.example.junimoapp.models.UserSession;
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
     * Create Event: title, description, date, location, max capacity, registration period, waiting list, price, geo location, poster
     *
     * */
    EditText editTitle, editDescription, editStartDate, editEndDate, editDateEvent, editEventLocation, editMaxCapacity, editWaitingList, editPrice, editGeoLocation, editPoster;
    Button uploadNewEvent;
    private Event createdEvent = null;
    Button QRCodeButton;
    TextView backButton;
    private String QRCodeString = null;
    private String eventID;
    private String organizerID;


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
        editDateEvent = findViewById(R.id.edit_date);
        editEventLocation = findViewById(R.id.edit_event_location);
        editMaxCapacity = findViewById(R.id.edit_max_capacity);
        editWaitingList = findViewById(R.id.edit_waiting_list);
        editPrice = findViewById(R.id.edit_price);
        editGeoLocation = findViewById(R.id.edit_geo_location);
        editPoster = findViewById(R.id.edit_poster);
        //button id
        uploadNewEvent = findViewById(R.id.upload_event_button);
        QRCodeButton = findViewById(R.id.QR_code_button);
        backButton=findViewById(R.id.backButton);

        eventID = getIntent().getStringExtra("event_ID");
        if (eventID != null) {
            createdEvent = EventData.searchEventID(eventID);

            if (createdEvent != null) {
                editTitle.setText(createdEvent.getTitle());
                editDescription.setText(createdEvent.getDescription());
                editStartDate.setText(createdEvent.getStartDate());
                editEndDate.setText(createdEvent.getEndDate());
                editDateEvent.setText(createdEvent.getDateEvent());
                editEventLocation.setText(createdEvent.getEventLocation());
                editMaxCapacity.setText(String.valueOf(createdEvent.getMaxCapacity()));
                editWaitingList.setText(String.valueOf(createdEvent.getWaitingListLimit()));
                editPrice.setText(String.valueOf(createdEvent.getPrice()));
                editGeoLocation.setText(createdEvent.getGeoLocation().toString());
                editEventLocation.setText(createdEvent.getEventLocation());
                editPoster.setText(createdEvent.getPoster());
            }
        }

        //QR code
        QRCodeButton.setOnClickListener(view -> {
            if (QRCodeString == null) {
                String QREventID;
                if (createdEvent != null) {
                    QREventID = createdEvent.getEventID();
                } else {
                    QREventID = UUID.randomUUID().toString();
                }
                QRCodeString = "junimo://event?id=" + QREventID;
                if (createdEvent != null) {
                    createdEvent.setQRCode(QRCodeString);
                }
                Toast.makeText(CreateEvent.this, "QR code generated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CreateEvent.this, "QR already exists", Toast.LENGTH_SHORT).show();
            }
        });

        //inputs
        uploadNewEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //optional fields
                String description = editDescription.getText().toString();
                String startDate = editStartDate.getText().toString();
                String endDate = editEndDate.getText().toString();
                String poster = editPoster.getText().toString();
                Integer waitingListLimit = null;
                if (!editWaitingList.getText().toString().isEmpty()) {
                    waitingListLimit = Integer.parseInt(editWaitingList.getText().toString());
                }
                //required fields
                String title = editTitle.getText().toString();
                if (title.isEmpty()) {
                    editTitle.setError("*Field Required*");
                    editTitle.requestFocus();
                    return;
                }
                String dateEvent = editDateEvent.getText().toString();
                if (dateEvent.isEmpty()) {
                    editDateEvent.setError("*Field Required*");
                    editDateEvent.requestFocus();
                }

                String geoLocation_string = editGeoLocation.getText().toString();
                GeoPoint geoLocation = new GeoPoint(00000, 00000);//needs to be something they choose?
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
                }
                int maxCapacity = Integer.parseInt(editMaxCapacity.getText().toString());

                if (editPrice.getText().toString().isEmpty()) {
                    editPrice.setError("*Field Required*");
                    editPrice.requestFocus();
                    return;
                }
                double price = Double.parseDouble(editPrice.getText().toString());

                //event ID
                if (createdEvent != null) {
                    eventID = createdEvent.getEventID();
                } else if (eventID == null) {
                    eventID = UUID.randomUUID().toString();
                }

                //organizer role
                User currentUser = UserSession.getCurrentUser();
                if (currentUser != null ) {
                    organizerID = currentUser.getDeviceId();
                } else {
                    Toast.makeText(CreateEvent.this, "User not logged in", Toast.LENGTH_SHORT).show();
                    return;
                }

                //creates event
                Event saveEvent = new Event(title, description, startDate, endDate, dateEvent, maxCapacity, waitingListLimit, price, geoLocation, poster, eventID, eventLocation, organizerID);

                //add to firebase
                FirebaseManager firebase = new FirebaseManager();
                CollectionReference eventsRef = firebase.getDB().collection("events");
                firebase.addEvent(saveEvent, eventsRef);


                if (QRCodeString != null) {
                    saveEvent.setQRCode(QRCodeString);
                }

                firebase.addEvent(saveEvent,eventsRef);

                //EventData.addOrEditEvent(saveEvent);
                Toast.makeText(CreateEvent.this, "Event Created", Toast.LENGTH_SHORT).show();
                finish();


                //TEST
                String logMessage;
                if (createdEvent != null) {
                    logMessage = "Event updated: " + saveEvent.getTitle();
                } else {
                    logMessage = "event created: " + saveEvent.getTitle();
                }

                Log.d("createEvent", logMessage);
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(CreateEvent.this, OrganizerStartScreen.class);
                startActivity(intent);            }
        });

    }
}

