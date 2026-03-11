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

import com.example.junimoapp.TestData.UserTestData;
import com.example.junimoapp.admin.AdminHomeActivity;
import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.models.User;
import com.example.junimoapp.utils.DeviceUtils;
import com.google.firebase.FirebaseApp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import com.example.junimoapp.models.Event;
import com.example.junimoapp.Organizer.OrganizerStartScreen;
import com.example.junimoapp.TestData.EventTestData;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    String deviceId;
    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    private CollectionReference usersRef;
    private ArrayList<Event> eventArrayList;
    private ArrayAdapter<Event> eventArrayAdapter;
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

//        //test event document
//        EventTestData testEvents= new EventTestData();
//        Event testEvent = testEvents.getEvents().get(0);
//        Event testEvent1 = testEvents.getEvents().get(1);
//        Event testEvent2 = testEvents.getEvents().get(2);
//        //write to firestore
//        firebase.addEvent(testEvent,eventsRef);
//        firebase.addEvent(testEvent1,eventsRef);
//        firebase.addEvent(testEvent2,eventsRef);

        //test user document
        UserTestData testUsers= new UserTestData();
        User testUser = testUsers.getUsers().get(0);
        User testUser1 = testUsers.getUsers().get(1);
        User testUser2 = testUsers.getUsers().get(2);
        //write to firestore
        boolean check= firebase.addUser(testUser,usersRef);
        firebase.addUser(testUser1,usersRef);
        firebase.addUser(testUser2,usersRef);

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

        //Admin button
        Button adminButton = findViewById(R.id.admin_button);
        adminButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdminHomeActivity.class);
            startActivity(intent);
        });

        //code that gets the list of users from firebase, maybe should be moved to where it is needed (like admin?)
        //is it in admin already??
        userArrayAdapter = new ArrayAdapter<>(this,0);
        userArrayList = firebase.getUsers(usersRef);
        userArrayAdapter.notifyDataSetChanged();


        //gets event list from firebase, needed here as the app opens to browsing events
        eventArrayAdapter = new ArrayAdapter<>(this, 0);
        eventArrayList = firebase.getEvents(eventsRef);
        eventArrayAdapter.notifyDataSetChanged();

    }

}