package com.example.junimoapp.Organizer;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * Organizer can see location of entrants who joined a specific event waitList
 * Uses google maps
 * Displays a mini map of pinned entrants location
 *  - Clicking mini map navigates to a full screen map of the entrants location
 * Displays a list of the entrants with their name and coordinates
 * US 02.02.02 As an organizer I want to see on a map where entrants joined my event waiting list from.
 * */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    FirebaseFirestore db;
    GoogleMap googleMap;
    ListView listOfEntrants;
    ArrayAdapter<String> adapter;
    ArrayList<String> entrantsDetails = new ArrayList<>();
    String eventID;
    String userID;
    TextView backButton;

    /**
     * Starts activity.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        listOfEntrants = findViewById(R.id.list_of_entrants);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, entrantsDetails);
        listOfEntrants.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        eventID = getIntent().getStringExtra("eventID");
        backButton = findViewById(R.id.back_button);

        MaterialCardView mapCard = findViewById(R.id.mapCard);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        backButton.setOnClickListener(view -> finish());
    }

    /**
     * Initializes map
     * Navigates to enlarged map when clicked on the mini map
     * @param map the google map
     * */
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        //enlarge map when clicked to full screen version
        googleMap.setOnMapClickListener(latLng -> {
            Intent biggerMap = new Intent(MapActivity.this, ZoomedInMap.class);
            biggerMap.putExtra("eventID", eventID);
            startActivity(biggerMap);
        });

        loadUsersLocation();
    }

    /**
     * Loads the users locations, latitude and longitude
     * Displays users on the map as a pinned location
     * Displays a list of the users with their name and coordinates
     * */
    private void loadUsersLocation() {
        db.collection("events")
                .document(eventID)
                .collection("userLocations")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            userID = document.getId();
                            GeoPoint geoPoint = document.getGeoPoint("geoLocation");

                            if (geoPoint == null) continue;

                            double latitude = geoPoint.getLatitude();
                            double longitude = geoPoint.getLongitude();

                            db.collection("users").document(userID).get().addOnCompleteListener(userTask -> {
                                if (userTask.isSuccessful()) {
                                    DocumentSnapshot userDocument = userTask.getResult();
                                    String usersName = userDocument.getString("name");
                                    if (usersName == null) usersName = userID;

                                    LatLng location = new LatLng(latitude, longitude);
                                    googleMap.addMarker(new MarkerOptions()
                                            .position(location)
                                            .title(usersName)
                                            .snippet("Latitude: " + latitude + "\nLongitude: " + longitude));

                                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));

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
}