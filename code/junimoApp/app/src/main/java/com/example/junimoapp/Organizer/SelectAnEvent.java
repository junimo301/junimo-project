package com.example.junimoapp.Organizer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.R;
import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.models.Event;
import com.example.junimoapp.models.UserSession;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Allows organizer to select an event to view entrants or map.
 * Loads events directly from Firestore on every resume so the
 * list is always fresh after returning from Entrants or Map.
 */
public class SelectAnEvent extends AppCompatActivity {

    FirebaseFirestore db;
    LinearLayout eventList;
    TextView backButton;
    String goTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_event);

        backButton = findViewById(R.id.back_button);
        eventList  = findViewById(R.id.event_list);
        db         = FirebaseManager.getDB();
        goTo       = getIntent().getStringExtra("go to");

        backButton.setOnClickListener(view -> finish());
    }

    /**
     * Reloads events from Firestore every time the screen is shown.
     * This ensures the list is always fresh after returning from
     * Entrants or Map, and also keeps EventData in sync so
     * Entrants.java can find events via EventData.searchEventID().
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadEventsFromFirestore();
    }

    private void loadEventsFromFirestore() {
        eventList.removeAllViews();

        // Guard against null user session
        if (UserSession.getCurrentUser() == null) {
            Log.e("SelectAnEvent", "currentUser is null");
            return;
        }
        String currentUserId = UserSession.getCurrentUser().getDeviceId();

        db.collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String organizerID = doc.getString("organizerID");

                        // Only show this organizer's own events
                        if (organizerID == null || !organizerID.equals(currentUserId)) continue;

                        String title   = doc.getString("title");
                        String eventID = doc.getString("eventID");
                        if (title == null || eventID == null) continue;

                        // Build Event object using the correct constructor
                        String description   = doc.getString("description");
                        String startDate     = doc.getString("startDate");
                        String endDate       = doc.getString("endDate");
                        String dateEvent     = doc.getString("dateEvent");
                        Long capacityObj     = doc.getLong("maxCapacity");
                        int maxCapacity      = capacityObj != null ? capacityObj.intValue() : 0;
                        Long limitObj        = doc.getLong("waitingListLimit");
                        int waitingListLimit = limitObj != null ? limitObj.intValue() : 0;
                        Double priceObj      = doc.getDouble("price");
                        double price         = priceObj != null ? priceObj : 0.0;
                        Boolean geoObj       = doc.getBoolean("geoLocation");
                        boolean geoLocation  = Boolean.TRUE.equals(geoObj);
                        String poster        = doc.getString("poster");
                        String eventLocation = doc.getString("eventLocation");
                        String tag           = doc.getString("tag");

                        Event event = new Event(title, description, startDate, endDate,
                                dateEvent, maxCapacity, waitingListLimit, price,
                                geoLocation, poster, eventID, eventLocation,
                                organizerID, tag);

                        // Restore QR code
                        String qrcode = doc.getString("qrcode");
                        event.setQRCode(qrcode);

                        // Restore private flag without writing back to Firestore
                        Boolean isPrivate = doc.getBoolean("private");
                        event.restorePrivate(Boolean.TRUE.equals(isPrivate));

                        // Keep EventData cache in sync so Entrants can find the event
                        EventData.addOrEditEvent(event);

                        // Add a button for this event
                        Button btn = new Button(this);
                        btn.setText(title);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        btn.setLayoutParams(params);

                        // Capture final eventID for the click listener
                        String finalEventID = eventID;
                        btn.setOnClickListener(v -> {
                            if ("entrants".equals(goTo)) {
                                Intent intent = new Intent(SelectAnEvent.this, Entrants.class);
                                intent.putExtra("eventID", finalEventID);
                                startActivity(intent);
                            } else if ("map".equals(goTo)) {
                                Intent intent = new Intent(SelectAnEvent.this, MapActivity.class);
                                intent.putExtra("eventID", finalEventID);
                                startActivity(intent);
                            }
                        });

                        eventList.addView(btn);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SelectAnEvent", "Failed to load events", e);
                    Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }
}
