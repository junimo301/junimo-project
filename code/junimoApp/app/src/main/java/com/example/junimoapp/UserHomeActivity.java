package com.example.junimoapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.models.Event;
import com.example.junimoapp.utils.DeviceUtils;
import com.example.junimoapp.NotificationsActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

/**
 * Home page for the entrant/user.
 * Shows list of public events, navigation buttons, notification centre,
 * and the notification opt-out toggle.
 *
 * User stories implemented here:
 *  - US 01.04.01 / 01.04.02 / 01.05.06: Button to open NotificationsActivity
 *  - US 01.04.03: Switch to enable/disable receiving notifications
 *  - US 02.01.02: Private events are filtered out of the public browse list
 */
public class UserHomeActivity extends AppCompatActivity {

    Button invitationsButton;
    Button profileButton;
    Button guidelinesButton;

    // ─────────────────────────────────────────────────────────────────────
    // US 01.04.01 / 01.04.02 / 01.05.06
    // Button that opens the notifications screen so the user can see
    // all their invite and lottery result notifications.
    // ─────────────────────────────────────────────────────────────────────
    Button notificationsButton;

    // ─────────────────────────────────────────────────────────────────────
    // US 01.04.03
    // Toggle switch that lets the entrant opt out of receiving notifications.
    // State is persisted in Firestore under the user's notificationsEnabled field.
    // ─────────────────────────────────────────────────────────────────────
    Switch notifSwitch;

    private ArrayAdapter<String> adapter;
    private ArrayList<Event> eventList;
    private ArrayList<String> eventListString;
    private FirebaseFirestore db;
    private ListView eventListView;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        invitationsButton   = findViewById(R.id.invitationsButton);
        profileButton       = findViewById(R.id.profileButton);
        guidelinesButton    = findViewById(R.id.guidelinesButton);
        notificationsButton = findViewById(R.id.notificationsButton);
        notifSwitch         = findViewById(R.id.notifSwitch);
        eventListView       = findViewById(R.id.eventListView);

        // Get the current device ID so we can read/write this user's Firestore doc
        deviceId = DeviceUtils.getDeviceId(this);
        db = FirebaseManager.getDB();

        // Open invitations page
        invitationsButton.setOnClickListener(v ->
                startActivity(new Intent(this, InvitationsActivity.class)));

        // Open profile page
        profileButton.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        // Open guidelines page
        guidelinesButton.setOnClickListener(v ->
                startActivity(new Intent(this, GuidelinesActivity.class)));

        // ─────────────────────────────────────────────────────────────────
        // US 01.04.01 / 01.04.02 / 01.05.06
        // Opens NotificationsActivity where the user can read all their
        // invite and lottery result notifications.
        // ─────────────────────────────────────────────────────────────────
        notificationsButton.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsActivity.class)));

        // ─────────────────────────────────────────────────────────────────
        // US 01.04.03
        // Load the user's current notification preference from Firestore
        // and set the switch to match, so it always reflects the real state.
        // ─────────────────────────────────────────────────────────────────
        db.collection("users").document(deviceId).get()
                .addOnSuccessListener(snap -> {
                    if (!snap.exists()) return;
                    Boolean enabled = snap.getBoolean("notificationsEnabled");
                    // Default to true if the field has never been set
                    notifSwitch.setChecked(enabled == null || enabled);
                });

        // ─────────────────────────────────────────────────────────────────
        // US 01.04.03
        // When the user flips the switch, persist the new preference to
        // Firestore. NotificationHelper checks this flag before writing
        // any new notification, so opted-out users never receive them.
        // ─────────────────────────────────────────────────────────────────
        notifSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            db.collection("users").document(deviceId)
                    .update("notificationsEnabled", isChecked);
            Toast.makeText(this,
                    isChecked ? "Notifications enabled" : "Notifications disabled",
                    Toast.LENGTH_SHORT).show();
        });

        // Events list setup
        eventListString = new ArrayList<>();
        eventList = new ArrayList<>();
        loadEvents();

        adapter = new ArrayAdapter<>(this, R.layout.item_user_event, eventListString);
        eventListView.setAdapter(adapter);

        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(UserHomeActivity.this, EventDetailsActivity.class);
                intent.putExtra("eventId", eventList.get(i).getEventID());
                startActivity(intent);
            }
        });
    }

    private void loadEvents() {
        Log.d("UserHomeActivity", "loading events");
        db.collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear();
                    eventListString.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {

                        // ─────────────────────────────────────────────────
                        // US 02.01.02
                        // Skip private events — they must not appear in the
                        // public browse list, only invited entrants can see them
                        // ─────────────────────────────────────────────────
                        Boolean isPrivate = doc.getBoolean("isPrivate");
                        if (Boolean.TRUE.equals(isPrivate)) continue;

                        String title           = doc.getString("title");
                        String description     = doc.getString("description");
                        String startDate       = doc.getString("startDate");
                        String endDate         = doc.getString("endDate");
                        String dateEvent       = doc.getString("dateEvent");
                        int maxCapacity        = doc.getLong("maxCapacity").intValue();
                        int waitingListLimit   = doc.getLong("waitingListLimit").intValue();
                        double price           = doc.getDouble("price");
                        GeoPoint geoLocation   = doc.getGeoPoint("geoLocation");
                        String poster          = doc.getString("poster");
                        String eventID         = doc.getString("eventID");
                        String eventLocation   = doc.getString("eventLocation");
                        String organizerID     = doc.getString("organizerID");

                        Event event = new Event(title, description, startDate, endDate,
                                dateEvent, maxCapacity, waitingListLimit, price,
                                geoLocation, poster, eventID, eventLocation, organizerID);
                        eventList.add(event);
                        eventListString.add(title);
                        Log.d("UserHomeActivity", eventListString.toString());
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Failed to load events", e);
                    Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }
}