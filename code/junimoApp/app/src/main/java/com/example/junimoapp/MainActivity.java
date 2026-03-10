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

import com.example.junimoapp.models.Event;
import com.example.junimoapp.Organizer.OrganizerStartScreen;
import com.example.junimoapp.TestData.EventTestData;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    String deviceId;
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

        //test event document
        EventTestData testEvents= new EventTestData();
        Event testEvent = testEvents.getEvents().get(0);
        Event testEvent1 = testEvents.getEvents().get(1);
        Event testEvent2 = testEvents.getEvents().get(2);
        //write to firestore
        firebase.addEvent(testEvent,eventsRef);
        firebase.addEvent(testEvent1,eventsRef);
        firebase.addEvent(testEvent2,eventsRef);

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
        userArrayList= new ArrayList<>();
        userArrayAdapter = new ArrayAdapter<>(this,0);

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

        //gets event list from firebase, needed here as the app opens to browsing events
        eventArrayList = new ArrayList<>();
        eventArrayAdapter = new ArrayAdapter<>(this, 0);

        eventsRef.addSnapshotListener((value,error)-> {
            if(error != null){
                Log.e("Firestore", error.toString());
            }
            if(value != null && !value.isEmpty()){
                eventArrayList.clear();
                for(QueryDocumentSnapshot snapshot : value){
                    //Fields in events
                    String title = snapshot.getString("Title");
                    String description = snapshot.getString("Description");
                    String startDate = snapshot.getString("startDate");
                    String endDate = snapshot.getString("endDate");
                    Integer maxCapacity = (snapshot.getLong("maxCapacity")).intValue(); //int is not allowed in firebase, long converted to int instead
                    Integer waitingListLimit = (snapshot.getLong("waitingListLimit")).intValue();
                    double price = snapshot.getDouble("price");
                    GeoPoint geoLocation = snapshot.getGeoPoint("geoLocation"); //geoPoint is a type apparently? seems helpful??
                    String poster = snapshot.getString("poster");
                    String eventID = snapshot.getString("eventID");
                    String eventLocation = snapshot.getString("eventLocation");


                    eventArrayList.add(new Event(title,description,startDate,endDate,maxCapacity,waitingListLimit,price,geoLocation,poster,eventID,eventLocation));
                }
                eventArrayAdapter.notifyDataSetChanged();
            }
        });
    }

}