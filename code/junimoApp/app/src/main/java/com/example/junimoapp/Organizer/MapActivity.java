package com.example.junimoapp.Organizer;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
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
 * GEO LOCATION MAP
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

        MaterialCardView map = findViewById(R.id.map);
        map.setOnClickListener(view -> {
            Intent biggerMap = new Intent(MapActivity.this, ZoomedInMap.class);
            biggerMap.putExtra("eventID", eventID);
            startActivity(biggerMap);
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        backButton.setOnClickListener(view -> finish());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null && mapFragment.getView() != null) {
            mapFragment.getMapAsync(this);
            mapFragment.getView().setClickable(true);
        }
        loadUsersLocation();
    }

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
