package com.example.junimoapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.models.Event;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

public class UserHomeActivity extends AppCompatActivity {

    ListView eventsList;
    Button invitationsButton;
    Button profileButton;
    Button guidelinesButton;
    private ArrayAdapter<Event> adapter;
    private ArrayList<Event> eventList;
    private FirebaseFirestore db;
    private ListView eventListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        invitationsButton = findViewById(R.id.invitationsButton);
        profileButton = findViewById(R.id.profileButton);
        guidelinesButton = findViewById(R.id.guidelinesButton);

        eventListView = findViewById(R.id.eventListView);


        //open invitations page
        invitationsButton.setOnClickListener(v ->
                startActivity(new Intent(this, InvitationsActivity.class)));

        //open profile page
        profileButton.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        //open guidelines page
        guidelinesButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserHomeActivity.this, GuidelinesActivity.class);
            startActivity(intent);
        });

        // fetch events from Firestore and populate the list
        db = FirebaseManager.getDB();
        eventList= new ArrayList<>();

        loadEvents();

        //events list
        adapter= new ArrayAdapter<>(this,R.layout.item_user_event,eventList);
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
        Log.d("user browse activity","on load events of browse activity");
        db.collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // clear any old data before loading fresh results
                    eventList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        //Fields in events
                        String title = doc.getString("Title");
                        String description = doc.getString("Description");
                        String startDate = doc.getString("startDate");
                        String endDate = doc.getString("endDate");
                        String dateEvent = doc.getString("dateEvent");
                        int maxCapacity = (doc.getLong("maxCapacity")).intValue();
                        int waitingListLimit = (doc.getLong("waitingListLimit")).intValue();
                        double price = doc.getDouble("price");
                        GeoPoint geoLocation = doc.getGeoPoint("geoLocation"); //geoPoint is a type apparently? seems helpful??
                        String poster = doc.getString("poster");
                        String eventID = doc.getString("eventID");
                        String eventLocation = doc.getString("eventLocation");

                        String organizerID = doc.getString("organizerID");

                        eventList.add(new Event(title, description, startDate, endDate, dateEvent, maxCapacity, waitingListLimit, price, geoLocation, poster, eventID, eventLocation, organizerID));
                    }
                    Log.d("user browse activity",eventList.toString());
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // log the error for debugging in Logcat
                    Log.e("Firebase", "Failed to load events", e);
                    // show a brief message to the admin so they know something went wrong
                    Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                });

    }
}