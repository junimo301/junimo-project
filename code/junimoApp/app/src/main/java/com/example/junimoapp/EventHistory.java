package com.example.junimoapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.models.Event;
import com.example.junimoapp.models.User;
import com.example.junimoapp.models.UserSession;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

public class EventHistory extends AppCompatActivity {
    private TextView backButton;
    private ListView eventListView;
    private ArrayList<String> eventListString;
    private FirebaseFirestore db;
    private ArrayAdapter<String> adapter;
    private ArrayList<Event> eventList;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_history_activity);

        User currentUser = UserSession.getCurrentUser();
        String deviceID = currentUser.getDeviceId();
        eventListView = findViewById(R.id.eventListView);
        backButton = findViewById(R.id.backToHomeText);

        db = FirebaseManager.getDB();
        eventListString = new ArrayList<>();
        eventList = new ArrayList<>();
        loadEvents(deviceID);

        adapter = new ArrayAdapter<>(this, R.layout.item_user_event, eventListString);
        eventListView.setAdapter(adapter);

        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(EventHistory.this, EventDetailsActivity.class);
                intent.putExtra("eventId", eventList.get(i).getEventID());
                intent.putExtra("fromHistory",true);
                startActivity(intent);
            }

        });

        backButton.setOnClickListener(v->{
            Intent intent = new Intent(EventHistory.this,ProfileActivity.class);
            intent.putExtra("new",false);
            intent.putExtra("organizer",false);
            startActivity(intent);
        });



    }

    private void loadEvents(String deviceID) {
        db.collection("users").document(deviceID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        String waitlist = doc.getString("waitlistedEvents");
                        String[] eventIDs = waitlist.split(",");
                        for (String eventID : eventIDs) {
                            Log.d("event history", "on load events of event history"+eventID);
                            db.collection("events").document(eventID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()) {
                                        DocumentSnapshot doc = task.getResult();
                                        if (doc.exists()) {
                                            Log.d("event history", doc.getString("eventID"));
                                            String title = doc.getString("title");
                                            String description = doc.getString("description");
                                            String startDate = doc.getString("startDate");
                                            String endDate = doc.getString("endDate");
                                            String dateEvent = doc.getString("dateEvent");
                                            int maxCapacity = (doc.getLong("maxCapacity")).intValue();
                                            int waitingListLimit = (doc.getLong("waitingListLimit")).intValue();
                                            double price = doc.getDouble("price");
                                            GeoPoint geoLocation = doc.getGeoPoint("geoLocation");
                                            String poster = doc.getString("poster");
                                            String eventID = doc.getString("eventID");
                                            String eventLocation = doc.getString("eventLocation");
                                            String organizerID = doc.getString("organizerID");

                                            String tag = doc.getString("tag");
                                            if (tag == null)
                                                tag = ""; //default to no tag if it's an old event with no tags

                                            Event event = new Event(title, description, startDate, endDate,
                                                    dateEvent, maxCapacity, waitingListLimit, price,
                                                    geoLocation, poster, eventID, eventLocation, organizerID, tag);
                                            eventList.add(event);
                                            eventListString.add(title);
                                            Log.d("event history", eventListString.toString());
                                        }
                                        adapter.notifyDataSetChanged();
                                    }

                                }
                            });
                        }

                    } else {
                        Log.d("Firestore", "No such document");
                    }
                } else {
                    Log.d("Firestore", "get failed with ", task.getException());
                }
            }
        });


    }
}
