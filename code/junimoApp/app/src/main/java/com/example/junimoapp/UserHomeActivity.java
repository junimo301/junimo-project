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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.models.Event;
import com.example.junimoapp.models.User;
import com.example.junimoapp.models.UserSession;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

/**
 * Home page for the entrant/user.
 *
 * User stories implemented here:
 *  - US 01.04.01 / 01.04.02 / 01.05.06: Button to open NotificationsActivity
 *  - US 01.04.03: Switch to enable/disable receiving notifications
 *  - US 02.01.02: Private events are filtered out of the public browse list
 */
public class UserHomeActivity extends AppCompatActivity {

    // Existing fields — unchanged
    ListView eventsList;
    Button invitationsButton;
    Button profileButton;
    Button guidelinesButton;
    TextView backButton;
    private ArrayAdapter<String> adapter;
    private ArrayList<Event> eventList;
    private ArrayList<String> eventListString;
    private FirebaseFirestore db;
    private ListView eventListView;

    // ─────────────────────────────────────────────────────────────────────
    // US 01.04.01 / 01.04.02 / 01.05.06
    // Button that opens NotificationsActivity so the user can read all
    // their invite and lottery result notifications.
    // ─────────────────────────────────────────────────────────────────────
    Button notificationsButton;

    // ─────────────────────────────────────────────────────────────────────
    // US 01.04.03
    // Toggle switch that lets the entrant opt out of receiving notifications.
    // Reads from and writes to UserSession.getCurrentUser() so no extra
    // Firestore call is needed — the User object already has the value
    // loaded from initializeEvents().
    // ─────────────────────────────────────────────────────────────────────
    Switch notifSwitch;

    // ---------------------------------------------------
    // US 01.01.04 / 01.01.05 / 01.01.06
    // Button for searching events
    // ---------------------------------------------------
    Button searchEventsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        // Existing view wiring — unchanged
        invitationsButton = findViewById(R.id.invitationsButton);
        profileButton     = findViewById(R.id.profileButton);
        guidelinesButton  = findViewById(R.id.guidelinesButton);
        eventListView     = findViewById(R.id.eventListView);

        // New views
        notificationsButton = findViewById(R.id.notificationsButton);
        notifSwitch         = findViewById(R.id.notifSwitch);
        searchEventsButton = findViewById(R.id.searchEventsButton);

        //back button
        backButton = findViewById(R.id.backToHomeText);

        // Existing navigation — unchanged
        invitationsButton.setOnClickListener(v ->
                startActivity(new Intent(this, InvitationsActivity.class)));
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("new", false);
            intent.putExtra("organizer",false);
            startActivity(intent);
        });

        guidelinesButton.setOnClickListener(v ->
                startActivity(new Intent(this, GuidelinesActivity.class)));

        //back button
        backButton.setOnClickListener(v->{
            Intent intent = new Intent(UserHomeActivity.this,MainActivity.class);
            startActivity(intent);
        });

        // ─────────────────────────────────────────────────────────────────
        // US 01.04.01 / 01.04.02 / 01.05.06
        // Opens NotificationsActivity to show the user their notifications.
        // ─────────────────────────────────────────────────────────────────
        notificationsButton.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsActivity.class)));

        // ---------------------------------
        // US 01.01.04 / 01.01.05 / 01.01.06
        // Opens Events SearchActivity to allow users to search/filter
        // ---------------------------------
        searchEventsButton.setOnClickListener(v ->
                startActivity(new Intent(this, EventSearchActivity.class)));


        // ─────────────────────────────────────────────────────────────────
        // US 01.04.03
        // Load notification preference from UserSession — no Firestore call
        // needed, the User object already has the value from initializeEvents().
        // ─────────────────────────────────────────────────────────────────
        User currentUser = UserSession.getCurrentUser();
        if (currentUser != null) {
            notifSwitch.setChecked(currentUser.isNotificationsEnabled());
        }

        // ─────────────────────────────────────────────────────────────────
        // US 01.04.03
        // When the user flips the switch, call User.setNotificationsEnabled()
        // which updates the User object AND persists to Firestore via
        // FirebaseManager.updateUser() — consistent with the rest of the app.
        // ─────────────────────────────────────────────────────────────────
        notifSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            User user = UserSession.getCurrentUser();
            if (user != null) {
                user.setNotificationsEnabled(isChecked);
            }
            Toast.makeText(this,
                    isChecked ? "Notifications enabled" : "Notifications disabled",
                    Toast.LENGTH_SHORT).show();
        });

        // Existing event list setup — unchanged
        db = FirebaseManager.getDB();
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
        Button scanQRButton = findViewById(R.id.scanQRButton);
        scanQRButton.setOnClickListener(v ->
                startActivity(new Intent(this, QRScanActivity.class)));
    }

    private void loadEvents() {
        Log.d("user browse activity", "on load events of browse activity");
        db.collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear();
                    eventListString.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {

                        // ─────────────────────────────────────────────────
                        // US 02.01.02
                        // Skip private events — they must not appear in the
                        // public browse list, only invited entrants can see them.
                        // ─────────────────────────────────────────────────
                        Boolean isPrivate = doc.getBoolean("isPrivate");
                        if (Boolean.TRUE.equals(isPrivate)) continue;

                        String title         = doc.getString("title");
                        String description   = doc.getString("description");
                        String startDate     = doc.getString("startDate");
                        String endDate       = doc.getString("endDate");
                        String dateEvent     = doc.getString("dateEvent");
                        int maxCapacity      = (doc.getLong("maxCapacity")).intValue();
                        int waitingListLimit = (doc.getLong("waitingListLimit")).intValue();
                        double price         = doc.getDouble("price");
                        GeoPoint geoLocation = doc.getGeoPoint("geoLocation");
                        String poster        = doc.getString("poster");
                        String eventID       = doc.getString("eventID");
                        String eventLocation = doc.getString("eventLocation");
                        String organizerID   = doc.getString("organizerID");

                        String tag           = doc.getString("tag");
                        if (tag == null) tag = ""; //default to no tag if it's an old event with no tags

                        Event event = new Event(title, description, startDate, endDate,
                                dateEvent, maxCapacity, waitingListLimit, price,
                                geoLocation, poster, eventID, eventLocation, organizerID, tag);
                        eventList.add(event);
                        eventListString.add(title);
                        Log.d("user browse activity", eventListString.toString());
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Failed to load events", e);
                    Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }
}