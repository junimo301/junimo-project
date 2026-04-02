package com.example.junimoapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.models.Event;
import com.example.junimoapp.models.User;
import com.example.junimoapp.models.UserSession;
import com.example.junimoapp.utils.DeviceUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * user stories implemented:
 *  - US 01.06.02: Entrant wants to be able to sign up for a waiting list from event details.
 *  - US 01.08.01: As an entrant, I want to post a comment on an event.
 *  - US 01.08.02: As an entrant, I want to view comments on an event.
 *  - US 02.08.01: As an organizer, I want to view and delete entrant comments on my event.
 *  - US 02.08.02: As an organizer, I want to comment on my events.
 */

/**
 * Provides details for events, such as the eventID, waitlists
 * Allows user to join wait list and leave waitlist
 * Displays comments
 */

public class EventDetailsActivity extends AppCompatActivity {

    FirebaseFirestore db;
    String deviceId;
    String eventId;
    Event selectedEvent;

    TextView eventTitle;
    TextView descriptionText;
    TextView organizerText;
    TextView eventDate;
    TextView eventLocationText;
    TextView priceText;
    TextView registrationDetails;
    TextView capacity;
    TextView countOnList;

    TextView backButton;
    Button joinWaitlistButton;
    Button declineButton;

    EditText commentInput;
    Button postCommentButton;
    ListView commentsListView;

    ArrayAdapter<String> commentsAdapter;
    ArrayList<String> commentsList = new ArrayList<>();
    ArrayList<String> commentIds = new ArrayList<>();

    //for locations
    private FusedLocationProviderClient fusedLocationClient;

    //geo location
    private final ActivityResultLauncher<String> requestLocationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    saveUserLocation(selectedEvent,UserSession.getCurrentUser());
                } else {
                    //DELETE LATER
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    boolean isOrganizer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.indivdual_invite_page);

        db = FirebaseManager.getDB();
        User user = UserSession.getCurrentUser();
        deviceId = user.getDeviceId();
        //eventId should be passed from previous activity
        eventId = getIntent().getStringExtra("eventId");
        boolean fromHistory = getIntent().getBooleanExtra("fromHistory",false);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        db.collection("events").document(eventId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    //Fields in events
                    String title = doc.getString("title");
                    String description = doc.getString("description");
                    String startDate = doc.getString("startDate");
                    String endDate = doc.getString("endDate");
                    String dateEvent = doc.getString("dateEvent");
                    int maxCapacity = (doc.getLong("maxCapacity")).intValue();
                    int waitingListLimit = (doc.getLong("waitingListLimit")).intValue();
                    double price = doc.getDouble("price");
                    boolean geoLocation = doc.getBoolean("geoLocation"); //geoPoint is a type apparently? seems helpful??
                    String poster = doc.getString("poster");
                    String eventID = doc.getString("eventID");
                    String eventLocation = doc.getString("eventLocation");
                    String organizerID = doc.getString("organizerID");
                    String tag = doc.getString("tag");

                    selectedEvent=(new Event(title, description, startDate, endDate, dateEvent,
                            maxCapacity, waitingListLimit, price, geoLocation, poster, eventID,
                            eventLocation, organizerID, tag));
                    Log.d("Firebase",selectedEvent.toString());

                    isOrganizer = deviceId.equals(organizerID);

                    eventTitle.setText(selectedEvent.getTitle());
                    descriptionText.setText(selectedEvent.getDescription());
                    organizerText.setText(selectedEvent.getOrganizerID());
                    eventDate.setText(selectedEvent.getDateEvent());
                    eventLocationText.setText(selectedEvent.getEventLocation());
                    priceText.setText(String.valueOf(selectedEvent.getPrice()));
                    String registration = selectedEvent.getStartDate()+"\n"+selectedEvent.getEndDate();
                    registrationDetails.setText(registration);
                    capacity.setText(String.valueOf(selectedEvent.getMaxCapacity()));
                    String waitlistText = String.valueOf(selectedEvent.getWaitList().split(",").length);
                    if(selectedEvent.getMaxCapacity()>0){
                        waitlistText = waitlistText+"/"+String.valueOf(selectedEvent.getMaxCapacity());
                    }
                    countOnList.setText(waitlistText);
                    loadComments();
                }
                else {
                    Log.d("Firestore", "Error getting documents: ", task.getException());
                }
            }
        });

        backButton = findViewById(R.id.backToInvitesText);
        joinWaitlistButton = findViewById(R.id.joinWaitlistButton);
        declineButton = findViewById(R.id.declineButton);

        eventTitle = findViewById(R.id.eventTitle);
        descriptionText = findViewById(R.id.descriptionText);
        organizerText = findViewById(R.id.organizerText);
        eventDate = findViewById(R.id.eventDate);
        eventLocationText = findViewById(R.id.eventLocation);
        priceText = findViewById(R.id.price);
        registrationDetails = findViewById(R.id.registrationDetails);
        capacity = findViewById(R.id.capacity);
        countOnList = findViewById(R.id.countOnList);

        backButton.setOnClickListener(v -> back(fromHistory));
        joinWaitlistButton.setOnClickListener(v -> JoinWaitlist(selectedEvent,user));
        declineButton.setOnClickListener(v -> LeaveWaitlist(selectedEvent,user));

        commentInput = findViewById(R.id.commentInput);
        postCommentButton = findViewById(R.id.postCommentButton);
        commentsListView = findViewById(R.id.commentsListView);

        commentsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, commentsList);
        commentsListView.setAdapter(commentsAdapter);

        commentsListView.setOnItemLongClickListener((parent, view, position, id) -> {

            //only organizers can del comms
            if (!isOrganizer) return true;

            String commentId = commentIds.get(position);

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Delete Comment")
                    .setMessage("Are you sure you want to delete this comment?")
                    .setPositiveButton("Delete", (dialog, which) -> {

                        db.collection("events")
                                .document(eventId)
                                .collection("comments")
                                .document(commentId)
                                .delete()
                                .addOnSuccessListener(aVoid -> loadComments());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

            return true;
        });

        postCommentButton.setOnClickListener(v -> {
            String text = commentInput.getText().toString().trim();

            if (!text.isEmpty()) {
                HashMap<String, Object> comment = new HashMap<>();
                comment.put("text", text);
                comment.put("userId", deviceId);
                comment.put("timestamp", new Date());

                db.collection("events")
                        .document(eventId)
                        .collection("comments")
                        .add(comment)
                        .addOnSuccessListener(docRef -> {
                            commentInput.setText("");
                            loadComments(); //refresh comments
                        });
            }
        });
    }

    private void back(boolean fromHistory) {
        if(fromHistory) {
            Intent intent = new Intent(EventDetailsActivity.this, EventHistory.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(EventDetailsActivity.this, UserHomeActivity.class);
            startActivity(intent);
        }

    }

    private void requestUserLocation(Event event, User user) {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }
        saveUserLocation(event,user);
    }

    @SuppressLint("MissingPermission")
    private void saveUserLocation(Event event,User user) {
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                db.collection("events")
                        .document(eventId)
                        .collection("userLocations")
                        .document(deviceId)
                        .set(new HashMap<String, Object>() {{
                            put("geoLocation", geoPoint);
                            put("timestamp", new Date());
                        }})
                        .addOnSuccessListener(aVoid -> {
                            Log.d("geoLocation", "User location saved: " + deviceId); })
                        .addOnFailureListener(e -> {
                            Log.e("geoLocation", "Error saving user location", e);
                        });

            } else { Log.w("geoLocation", "Location is null"); }
        });
    }

    private void JoinWaitlist(Event event, User user){
        Log.d("button click","waitlist button clicked");
        String startDate= event.getStartDate();
        String endDate = event.getEndDate();
        String[] oldList = event.getWaitList().split(",");
        if(oldList.length<=event.getMaxCapacity()) {
            if (registrationPeriod(startDate, endDate)) {
                user.joinEventWaitList(event);
                String updatedList = event.getWaitList();
                FirebaseManager.updateEvent(db.collection("events"), event, "waitList", updatedList);
                joinWaitlistButton.setText("ADDED TO WAITLIST");

                //GeoLocation
                if (event.isGeoLocation()) { requestUserLocation(event, user); }
            }
            else {
                joinWaitlistButton.setText("REGISTRATION PERIOD NOT OPEN");
            }
        }
        else {
            joinWaitlistButton.setText("WAITLIST FULL");
        }
    }
    //check if registration period is open
    public boolean registrationPeriod(String startDate, String endDate) {
        if (startDate != null && !startDate.equals("")) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date start = format.parse(startDate);
                Date end = format.parse(endDate);
                Date now = new Date();
                return !(now.before(start) || now.after(end));
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        else {
            return true;
        }

    }
    private void LeaveWaitlist(Event event,User user){
        user.leaveEventWaitList(event);
        String updatedList = event.getWaitList();
        FirebaseManager.updateEvent(db.collection("events"), event, "waitList", updatedList);
        declineButton.setText("WAITLIST LEFT");

    }

    //loads comments on an event
    private void loadComments() {
        commentIds.clear();
        db.collection("events")
                .document(eventId)
                .collection("comments")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        commentsList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String text = doc.getString("text");
                            String user = doc.getString("userId");
                            if (text == null || user == null) continue;

                            String display = user + ": " + text;

                            commentsList.add(display);
                            commentIds.add(doc.getId());
                        }
                        commentsAdapter.notifyDataSetChanged();
                    }
                });
    }

}