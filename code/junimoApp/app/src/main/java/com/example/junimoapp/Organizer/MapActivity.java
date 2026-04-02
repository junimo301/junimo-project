package com.example.junimoapp.Organizer;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.junimoapp.R;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * GEO LOCATION MAP
 * */
public class MapActivity {
    FirebaseFirestore db;
    MapView map;
    ListView listOfEntrants;
    ArrayAdapter<String> adapter;
    ArrayList<String> entrantsDetails = new ArrayList<>();
    String eventID;
    String userID;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_map);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.getController().setZoom(15);
        map.getController().setCenter(new org.osmdroid.util.GeoPoint(49.2827, -123.1207));

        listOfEntrants = findViewById(R.id.list_of_entrants);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, entrantsDetails);
        listOfEntrants.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        eventID = getIntent().getStringExtra("eventID");

        loadUsersLocation();
    }

    private void loadUsersLocation() {
        db.collection("events")
                .document(eventID)
                .collection("userLocations")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            userID = document.getString("userID") != null
                                ? document.getId() : document.getId();
                            com.google.firebase.firestore.GeoPoint geoPoint = document.getGeoPoint("geoLocation");

                            if (geoPoint != null) continue;

                            double latitude = geoPoint.getLatitude();
                            double longitude = geoPoint.getLongitude();

                            db.collection("users").document(userID).get().addOnCompleteListener(userTask -> {
                                if (userTask.isSuccessful()) {
                                    DocumentSnapshot userDocument = userTask.getResult();
                                    String usersName = userDocument.getString("name");
                                    if (usersName == null) usersName = userID;

                                    Marker mapMarker = new Marker(map);
                                    mapMarker.setPosition(new org.osmdroid.util.GeoPoint(latitude, longitude));
                                    mapMarker.setTitle(usersName);
                                    mapMarker.setSnippit("Latitude: " + latitude + "\nLongitude: " + longitude);
                                    map.getOverLays().add(marker);
                                    map.invalidate();

                                    entrantsDetails.add(usersName + "\n" + latitude + "," + longitude);
                                    adapter.notifyDataSetChanged();

                                }

                            });

                        }
                    } else {
                        Log.e("EntrantMap", "Failed to load users location", task.getException());
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }

}
