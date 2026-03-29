package com.example.junimoapp.Organizer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.junimoapp.OrganizerStartScreen;
import com.example.junimoapp.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.example.junimoapp.models.Event;
import com.example.junimoapp.models.User;
import com.example.junimoapp.models.UserSession;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.GeoPoint;
import com.example.junimoapp.firebase.FirebaseManager;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Organizer creates and edits events
 * 
 */
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
    /**Can edit the these fields to add info
     * the buttons to proccess */
    EditText editTitle, editDescription, editStartDate, editEndDate, editDateEvent, editEventLocation, editMaxCapacity, editWaitingList, editPrice, editGeoLocation;
    Button uploadNewEvent, previewButton, QRCodeButton, cancelButton;
    TextView backButton;

    private Event createdEvent = null;
    private String QRCodeString = null;
    private String eventID;
    private String organizerID;

    //Event poster
    private ImageView eventPoster;
    private Uri imageUri;
    private Button pickImageButton;

    //pick an image
    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
        if (uri != null) {
            imageUri = uri;
            eventPoster.setImageURI(uri);
        }
    });

    /**
     * when activity is first created
     * listeners for QR code button and upload event button
     * @param savedInstanceState
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

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

        //button id
        uploadNewEvent = findViewById(R.id.upload_event_button);
        QRCodeButton = findViewById(R.id.QR_code_button);
        backButton=findViewById(R.id.backButton);
        cancelButton = findViewById(R.id.cancel_button);
        previewButton = findViewById(R.id.preview_event_button);
        pickImageButton = findViewById(R.id.pick_image_button);

        //event poster
        eventPoster = findViewById(R.id.event_poster);

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

                if (createdEvent.getPoster() != null && !createdEvent.getPoster().isEmpty()) {
                    Glide.with(this).load(createdEvent.getPoster())
                            .into(eventPoster);
                }

            }
        }


        /**
         * Upload an event poster
         * */
        pickImageButton.setOnClickListener(view -> {
            pickImage.launch("image/*");
        });

        /**
         * Generates qr code when creating an event
         * lets you know if qr code is created or exists already
         * */
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

        /**
         * allows you to view a preview of the event before upload in another activity
         * */
        previewButton.setOnClickListener( view -> {
            String title = editTitle.getText().toString();
            String description = editDescription.getText().toString();
            String startDate = editStartDate.getText().toString();
            String endDate = editEndDate.getText().toString();
            String waitingListLimit = editWaitingList.getText().toString();
            String dateEvent = editDateEvent.getText().toString();
            String geoLocation_string = editGeoLocation.getText().toString();
            String eventLocation = editEventLocation.getText().toString();
            int maxCapacity = Integer.parseInt(editMaxCapacity.getText().toString());
            double price = Double.parseDouble(editPrice.getText().toString());

            Intent previewEvent = new Intent(CreateEvent.this, EventPreview.class);

            previewEvent.putExtra("title", title);
            previewEvent.putExtra("description", description);
            previewEvent.putExtra("startDate", startDate);
            previewEvent.putExtra("endDate", endDate);
            previewEvent.putExtra("waitingListLimit", waitingListLimit);
            previewEvent.putExtra("dateEvent", dateEvent);
            previewEvent.putExtra("geoLocation_string", geoLocation_string);
            previewEvent.putExtra("eventLocation", eventLocation);
            previewEvent.putExtra("maxCapacity", maxCapacity);
            previewEvent.putExtra("price", price);
            if (createdEvent != null) {
                previewEvent.putExtra("poster", createdEvent.getPoster());
            }

            startActivity(previewEvent);
        });


        /**
         * Uploads event to firebase
         * checks that all required fields for the event are filled out
         * checks that format and input is valid
         * */
        uploadNewEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //must upload event poster
                if (imageUri == null) {
                    Toast.makeText(CreateEvent.this, "No image selected", Toast.LENGTH_SHORT).show();
                    return;
                }

                //optional fields
                String description = editDescription.getText().toString();
                String startDate = editStartDate.getText().toString();
                String endDate = editEndDate.getText().toString();

                //required date format
                if (!startDate.isEmpty() && !endDate.isEmpty()) {
                    try {
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        Date start = format.parse(startDate);
                        Date end = format.parse(endDate);

                        if (start.after(end)) {
                            editStartDate.setError("Start date must be before end date");
                            editStartDate.requestFocus();
                            return;
                        }
                    } catch (Exception e) {
                        Toast.makeText(CreateEvent.this, "Invalid date format", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                Integer waitingListLimit;
                if (!editWaitingList.getText().toString().isEmpty()) {
                    waitingListLimit = Integer.parseInt(editWaitingList.getText().toString());
                } else {
                    waitingListLimit = null;
                }
                if (waitingListLimit != null && waitingListLimit < 0) {
                    editWaitingList.setError("Waiting list limit must be a positive integer");
                    editWaitingList.requestFocus();
                    return;
                }
                //required fields
                String title = editTitle.getText().toString();
                if (title.isEmpty() ) {
                    editTitle.setError("*Field Required*");
                    editTitle.requestFocus();
                    return;
                }
                String dateEvent = editDateEvent.getText().toString();
                if (dateEvent.isEmpty()) {
                    editDateEvent.setError("*Field Required*");
                    editDateEvent.requestFocus();
                    return;
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

                //Upload image before creating event
                StorageReference storageRef = FirebaseStorage.getInstance()
                        .getReference("event_poster/" + eventID + ".jpg");

                storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String poster = uri.toString();

                        // creates event
                        Event saveEvent = new Event(title, description, startDate, endDate, dateEvent, maxCapacity, waitingListLimit, price, geoLocation, poster, eventID, eventLocation, organizerID);

                        if (QRCodeString != null) {
                            saveEvent.setQRCode(QRCodeString);
                        }

                        //add to firebase
                        FirebaseManager firebase = new FirebaseManager();
                        CollectionReference eventsRef = firebase.getDB().collection("events");
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


                    }).addOnFailureListener(error -> {
                        Toast.makeText(CreateEvent.this, "Error uploading image", Toast.LENGTH_SHORT).show();
                        Log.e("createEvent", "Error uploading image", error);
                    });

                });
            }

        });

        /* returns to organizer start screen
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(CreateEvent.this, OrganizerStartScreen.class);
                startActivity(intent);            }
        }); */
        backButton.setOnClickListener(view -> finish());
        cancelButton.setOnClickListener(view -> finish());

    }
}

