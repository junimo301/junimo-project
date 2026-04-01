package com.example.junimoapp.Organizer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import com.example.junimoapp.R;
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

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Organizer creates and edits events.
 *
 * User stories implemented here:
 *  - US 02.01.01 Create a new event and generate a unique promotional QR code
 *  - US 02.01.02 Create a private event (no public listing, no QR code)
 *  - US 02.01.04 Set a registration period
 *  - US 02.03.01 Optionally limit the number of entrants on the waiting list
 */
public class CreateEvent extends AppCompatActivity {

    EditText editTitle, editDescription, editStartDate, editEndDate, editDateEvent,
            editEventLocation, editMaxCapacity, editWaitingList, editPrice;

    android.widget.Spinner editTagSpinner;
    Button uploadNewEvent, previewButton, QRCodeButton, cancelButton, enableGeoLocationButton;
    TextView backButton;

    // ─────────────────────────────────────────────────────────────────────
    // US 02.01.02
    // Checkbox that lets the organizer mark this event as private.
    // When checked:
    //   - the QR code button is disabled (private events have no promo QR)
    //   - isPrivate is saved as true in Firestore
    //   - the event will NOT appear in the public event listing
    // ─────────────────────────────────────────────────────────────────────
    CheckBox checkPrivate;

    private boolean geoLocation = false;
    private Event createdEvent = null;
    private String QRCodeString = null;
    private String eventID;
    private String organizerID;

    //Event poster
    private ImageView eventPoster;
    private Uri imageUri;
    private Button pickImageButton;

    //pick an image
    private final ActivityResultLauncher<String>
            pickImage = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
        if (uri != null) {
            imageUri = uri;

            //get the file name for button
            String imageFile = uri.getLastPathSegment();
            if (imageFile != null && imageFile.contains("/")) {
                imageFile = imageFile.substring(imageFile.lastIndexOf("/") +1);
            }
            pickImageButton.setText(imageFile);

            Glide.with(this).load(uri)
                    .placeholder(R.drawable.bg_event_tile)
                    .into(eventPoster);
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

        // Wire up all existing form fields
        editTitle               = findViewById(R.id.edit_title);
        editDescription         = findViewById(R.id.edit_description);
        editStartDate           = findViewById(R.id.edit_start_date);
        editEndDate             = findViewById(R.id.edit_end_date);
        editDateEvent           = findViewById(R.id.edit_date);
        editEventLocation       = findViewById(R.id.edit_event_location);
        editMaxCapacity         = findViewById(R.id.edit_max_capacity);
        editWaitingList         = findViewById(R.id.edit_waiting_list);
        editPrice               = findViewById(R.id.edit_price);
        uploadNewEvent          = findViewById(R.id.upload_event_button);
        QRCodeButton            = findViewById(R.id.QR_code_button);
        enableGeoLocationButton = findViewById(R.id.enable_geoLocation_button);
        backButton              = findViewById(R.id.backButton);
        cancelButton            = findViewById(R.id.cancel_button);
        previewButton           = findViewById(R.id.preview_event_button);
        editTagSpinner          = findViewById(R.id.edit_tag_spinner);
        pickImageButton         = findViewById(R.id.pick_image_button);
        eventPoster             = findViewById(R.id.event_poster);
        Glide.with(this).load((String)null)
                .placeholder(R.drawable.bg_event_tile)
                .into(eventPoster);

        // ─────────────────────────────────────────────────────────────────
        // US 02.01.02
        // Wire up the private event checkbox and disable QR when checked.
        // ─────────────────────────────────────────────────────────────────
        checkPrivate = findViewById(R.id.check_private_event);
        checkPrivate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Private events must not have a promotional QR code
            QRCodeButton.setEnabled(!isChecked);
            if (isChecked) {
                QRCodeString = null; // clear any already-generated QR
                Toast.makeText(this,
                        "Private event: no QR code will be generated.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Pre-fill fields if editing an existing event
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
                if (createdEvent.getPoster() != null && !createdEvent.getPoster().isEmpty()) {
                    Glide.with(this).load(createdEvent.getPoster())
                            .placeholder(R.drawable.bg_event_tile)
                            .error(R.drawable.bg_event_tile)
                            .into(eventPoster);
                }
                // US 01.01.05 and 01.01.06: set spinner selection based on existing tag
                if (createdEvent.getTag() != null && !createdEvent.getTag().isEmpty()) {
                    android.widget.ArrayAdapter<CharSequence> adapter = (android.widget.ArrayAdapter<CharSequence>) editTagSpinner.getAdapter();
                    if (adapter != null) {
                        int position = adapter.getPosition(createdEvent.getTag());
                        if (position >=0) {
                            editTagSpinner.setSelection(position);
                        }
                    }
                }
                // ─────────────────────────────────────────────────────────
                // US 02.01.02
                // Restore the private flag when editing an existing event
                // ─────────────────────────────────────────────────────────
                checkPrivate.setChecked(createdEvent.isPrivate());

                //geolocation
                geoLocation = createdEvent.isGeoLocation();
                enableGeoLocationButton.setText(geoLocation ? getString(R.string.enabled) : getString(R.string.disabled));
            }
        }

        /**
         * Enable geo location for the event
         * */
        enableGeoLocationButton.setOnClickListener(view -> {
            geoLocation = !geoLocation;
            if (geoLocation) {
                enableGeoLocationButton.setText(getString(R.string.enabled));
                //DELETE LATER
                Toast.makeText(this, "Geo Location Enabled", Toast.LENGTH_SHORT).show();
            } else {
                enableGeoLocationButton.setText(getString(R.string.disabled));
                //DELETE LATER
                Toast.makeText(this, "Geo Location Disabled", Toast.LENGTH_SHORT).show();
            } });

        /**
         * Upload an event poster
         * */
        pickImageButton.setOnClickListener(view -> {
            pickImage.launch("image/*");
        });

        /**
         * US 02.01.01 / US 02.01.02
         * Generates a QR code when creating a public event.
         * Blocked entirely for private events.
         */
        QRCodeButton.setOnClickListener(view -> {

            // ─────────────────────────────────────────────────────────────
            // US 02.01.02
            // Guard: do not generate a QR code for private events
            // ─────────────────────────────────────────────────────────────
            if (checkPrivate.isChecked()) {
                Toast.makeText(this,
                        "Private events do not use a QR code.",
                        Toast.LENGTH_SHORT).show();
                return; }
            if (QRCodeString == null) {
                String QREventID = (createdEvent != null)
                        ? createdEvent.getEventID()
                        : UUID.randomUUID().toString();
                QRCodeString = "junimo://event?id=" + QREventID;
                if (createdEvent != null) createdEvent.setQRCode(QRCodeString);
                Toast.makeText(this, "QR code generated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "QR already exists", Toast.LENGTH_SHORT).show(); }
        });

        /**
         * Allows a preview of the event before uploading
         */
        previewButton.setOnClickListener(view -> {
            String title              = editTitle.getText().toString();
            String description        = editDescription.getText().toString();
            String startDate          = editStartDate.getText().toString();
            String endDate            = editEndDate.getText().toString();
            String waitingListLimit   = editWaitingList.getText().toString();
            String dateEvent          = editDateEvent.getText().toString();
            String eventLocation      = editEventLocation.getText().toString();
            String maxCapacity        = editMaxCapacity.getText().toString();
            String price              = editPrice.getText().toString();

            Intent previewEvent = new Intent(CreateEvent.this, EventPreview.class);
            previewEvent.putExtra("title", title);
            previewEvent.putExtra("description", description);
            previewEvent.putExtra("startDate", startDate);
            previewEvent.putExtra("endDate", endDate);
            previewEvent.putExtra("waitingListLimit", waitingListLimit);
            previewEvent.putExtra("dateEvent", dateEvent);
            previewEvent.putExtra("eventLocation", eventLocation);
            previewEvent.putExtra("maxCapacity", maxCapacity);
            previewEvent.putExtra("price", price);
            if (createdEvent != null && createdEvent.getPoster() != null && !createdEvent.getPoster().isEmpty()) {
                previewEvent.putExtra("poster", createdEvent.getPoster());
            } else if (imageUri != null) {
                previewEvent.putExtra("posterURI", imageUri.toString());
            }
            startActivity(previewEvent);
        });

        uploadNewEvent.setOnClickListener(v -> uploadNewEvent());

        backButton.setOnClickListener(view -> finish());
        cancelButton.setOnClickListener(view -> finish());
    }

    /**
     * Validates all required fields then uploads the event to Firebase.
     * US 02.01.02: saves isPrivate flag and skips QR code for private events.
     */
    private void uploadNewEvent() {
        //optional fields
        String description = editDescription.getText().toString();
        String startDate = editStartDate.getText().toString();
        String endDate = editEndDate.getText().toString();

        // US 02.01.04 — validate registration period dates
        if (!startDate.equals("") && !endDate.equals("")) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date start = format.parse(startDate);
                Date end = format.parse(endDate);
                if (start.after(end)) {
                    editStartDate.setError("Start date must be before end date");
                    editStartDate.requestFocus();
                    return; }
            } catch (Exception e) {
                Toast.makeText(CreateEvent.this, "Invalid date format", Toast.LENGTH_SHORT).show();
                return;  }
        }

        // US 02.03.01 — optional waiting list limit
        final Integer finalWaitingListLimit;
        String waitingListLimit = editWaitingList.getText().toString();
        if (!waitingListLimit.equals("")) {
            int parsedLimit = Integer.parseInt(waitingListLimit);
            if (parsedLimit < 0) {
                editWaitingList.setError("Waiting list limit must be a positive integer");
                editWaitingList.requestFocus();
                return; }
            finalWaitingListLimit = parsedLimit;
        } else {
            finalWaitingListLimit = null;
        }

        // Required fields
        String title = editTitle.getText().toString();
        if (title.equals("")) {
            editTitle.setError("*Field Required");
            editTitle.requestFocus();
            return;
        }

        String dateEvent = editDateEvent.getText().toString();
        if (dateEvent.equals("")) {
            editDateEvent.setError("*Field Required");
            editDateEvent.requestFocus();
            return;
        }

        //GEO LOCATION

        String eventLocation = editEventLocation.getText().toString();
        if (eventLocation.equals("")) {
            editEventLocation.setError("*Field Required");
            editEventLocation.requestFocus();
            return;
        }

        if (editMaxCapacity.getText().toString().equals("")) {
            editMaxCapacity.setError("*Field Required");
            editMaxCapacity.requestFocus();
            return;
        }
        int maxCapacity = Integer.parseInt(editMaxCapacity.getText().toString());

        if (editPrice.getText().toString().equals("")) {
            editPrice.setError("*Field Required");
            editPrice.requestFocus();
            return;
        }
        double price = Double.parseDouble(editPrice.getText().toString());

        // Event ID
        if (createdEvent != null) {
            eventID = createdEvent.getEventID();
        } else if (eventID == null) {
            eventID = UUID.randomUUID().toString();
        }

        // Organizer role check
        User currentUser = UserSession.getCurrentUser();
        if (currentUser != null) {
            organizerID = currentUser.getDeviceId();
        } else {
            Toast.makeText(CreateEvent.this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        //Get selected tag from spinner
        String tag = editTagSpinner.getSelectedItem().toString();
        if (tag.equals("None")) {
            tag = ""; //empty string for no tag
        }

        //Upload image before creating event
        String finalTag;
        if (imageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance()
                    .getReference("event_poster/" + eventID + ".jpg");

            Log.d("createEvent", "Uploading image: " + imageUri.toString());
            Log.d("createEvent", "Storage path: " + storageRef.getPath());


            finalTag = tag;
            storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String poster = uri.toString();

                    // Build the event object
                    Event saveEvent = new Event(
                            title, description, startDate, endDate, dateEvent,
                            maxCapacity, finalWaitingListLimit, price, geoLocation,
                            poster, eventID, eventLocation, organizerID, finalTag);

                    // ─────────────────────────────────────────────────────────
                    // US 02.01.02
                    // Read the private checkbox and apply to the event.
                    // If private: clear any QR string so it is never saved.
                    // If public:  attach the QR code if one was generated.
                    // ─────────────────────────────────────────────────────────
                    boolean isPrivate = checkPrivate.isChecked();
                    saveEvent.setPrivate(isPrivate);

                    if (isPrivate) {
                        // Private events must not store a QR code
                        QRCodeString = null;
                    } else if (QRCodeString != null) {
                        saveEvent.setQRCode(QRCodeString);
                    }

                    // Upload to Firebase
                    FirebaseManager firebase = new FirebaseManager();
                    CollectionReference eventsRef = firebase.getDB().collection("events");
                    firebase.addEvent(saveEvent, eventsRef);

                    Toast.makeText(CreateEvent.this, "Event Created", Toast.LENGTH_SHORT).show();

                    String logMessage = (createdEvent != null)
                            ? "Event updated: " + saveEvent.getTitle()
                            : "Event created: " + saveEvent.getTitle();
                    Log.d("createEvent", logMessage);

                    finish();

                }).addOnFailureListener(e -> {
                    Toast.makeText(CreateEvent.this, "Failed to get URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("createEvent", "getDownloadUrl failed", e);
                });

            }).addOnFailureListener(e -> {
                Toast.makeText(CreateEvent.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("createEvent", "upload failed", e);
            });

        } else {
            finalTag = null;  //If an image isn't uploaded, use the default image
            String poster = (createdEvent != null) ? createdEvent.getPoster() : "";

            Event saveEvent = new Event(
                    title, description, startDate, endDate, dateEvent,
                    maxCapacity, finalWaitingListLimit, price, geoLocation,
                    poster, eventID, eventLocation, organizerID, finalTag);

            // ─────────────────────────────────────────────────────────
            // US 02.01.02
            // Read the private checkbox and apply to the event.
            // If private: clear any QR string so it is never saved.
            // If public:  attach the QR code if one was generated.
            // ─────────────────────────────────────────────────────────
            boolean isPrivate = checkPrivate.isChecked();
            saveEvent.setPrivate(isPrivate);

            if (isPrivate) {
                // Private events must not store a QR code
                QRCodeString = null;
            } else if (QRCodeString != null) {
                saveEvent.setQRCode(QRCodeString);
            }

            // Upload to Firebase
            FirebaseManager firebase = new FirebaseManager();
            CollectionReference eventsRef = firebase.getDB().collection("events");
            firebase.addEvent(saveEvent, eventsRef);

            Toast.makeText(CreateEvent.this, "Event Created", Toast.LENGTH_SHORT).show();

            String logMessage = (createdEvent != null)
                    ? "Event updated: " + saveEvent.getTitle()
                    : "Event created: " + saveEvent.getTitle();
            Log.d("createEvent", logMessage);

            finish();
        }
    }
}