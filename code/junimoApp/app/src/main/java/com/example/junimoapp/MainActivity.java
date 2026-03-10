package com.example.junimoapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.models.User;
import com.example.junimoapp.utils.DeviceUtils;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import com.example.junimoapp.Organizer.OrganizerEvent;
import com.example.junimoapp.Organizer.OrganizerStartScreen;
import com.example.junimoapp.TestData.EventTestData;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    String deviceId;
    private CollectionReference eventsRef;
    private CollectionReference usersRef;
    private ArrayList<OrganizerEvent> eventArrayList;
    private ArrayAdapter<OrganizerEvent> eventArrayAdapter;
    private ArrayList<User> userArrayList;
    private ArrayAdapter<User> userArrayAdapter;
    private FirebaseManager firebase = new FirebaseManager();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this);
        CollectionReference eventsRef = firebase.getDB().collection("events");
        CollectionReference usersRef = firebase.getDB().collection("users");

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //get device id
        deviceId = DeviceUtils.getDeviceId(this);

        //test user document
        User testUser = new User(deviceId,"name","email","phone");

        //write to firestore
        boolean check= firebase.addUser(testUser,usersRef);

        if(check) {
            //open user homepage when firebase succeeds
            Intent intent = new Intent(MainActivity.this, UserHomeActivity.class);
            startActivity(intent);

            //close mainactivity
            finish();
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button organizerButton = findViewById(R.id.organizer_button);

        organizerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, OrganizerStartScreen.class);
            startActivity(intent);
        });

        userArrayList= new ArrayList<>();
        userArrayAdapter = new ArrayAdapter<>(this,0);
        //Admin button
        Button adminButton = findViewById(R.id.admin_button);
        adminButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdminHomeActivity.class);
            startActivity(intent);
        });
        eventArrayList = new ArrayList<>();
        eventArrayAdapter = new ArrayAdapter<>(this, 0);

        usersRef.addSnapshotListener((value, error)->{
            if(error != null){
                Log.e("Firestore",error.toString());
            }
            if(value!=null && !value.isEmpty()){
                userArrayList.clear();
                for(QueryDocumentSnapshot snapshot : value){
                    String deviceId = snapshot.getString("deviceId");
                    String name = snapshot.getString("name");
                    String email = snapshot.getString("email");
                    String phone = snapshot.getString("phone");

                    userArrayList.add(new User(deviceId,name,email,phone));
                }
                userArrayAdapter.notifyDataSetChanged();
            }
        });
        eventArrayList = new ArrayList<>();
        eventArrayAdapter = new ArrayAdapter<>(this, 0);

        eventsRef.addSnapshotListener((value,error)-> {
            if(error != null){
                Log.e("Firestore", error.toString());
            }
            if(value != null && !value.isEmpty()){
                eventArrayList.clear();
                for(QueryDocumentSnapshot snapshot : value){
                    String title = snapshot.getString("Title");
                    String description = snapshot.getString("Description");
                    String startDate = snapshot.getString("startDate");
                    String endDate = snapshot.getString("endDate");
                    Integer maxCapacity = (snapshot.getLong("maxCapacity")).intValue();
                    Integer waitingListLimit = (snapshot.getLong("waitingListLimit")).intValue();
                    double price = snapshot.getDouble("price");
                    GeoPoint geoLocation = snapshot.getGeoPoint("geoLocation"); //geoPoint is a type apparently? seems helpful??
                    String poster = snapshot.getString("poster");
                    String eventID = snapshot.getString("eventID");
                    String eventLocation = snapshot.getString("eventLocation");


                    eventArrayList.add(new OrganizerEvent(title,description,startDate,endDate,maxCapacity,waitingListLimit,price,geoLocation,poster,eventID,eventLocation));
                }
                eventArrayAdapter.notifyDataSetChanged();
            }
        });
    }

}