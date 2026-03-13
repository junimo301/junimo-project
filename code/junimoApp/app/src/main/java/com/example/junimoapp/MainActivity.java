package com.example.junimoapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

<<<<<<< Updated upstream
=======
import com.example.junimoapp.admin.AdminHomeActivity;
import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.models.User;
>>>>>>> Stashed changes
import com.example.junimoapp.utils.DeviceUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

<<<<<<< Updated upstream
import java.util.HashMap;
import java.util.Map;
import com.example.junimoapp.Organizer.OrganizerEvent;
import com.example.junimoapp.Organizer.OrganizerStartScreen;
import com.example.junimoapp.TestData.EventTestData;
=======
import com.example.junimoapp.models.Event;
>>>>>>> Stashed changes
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    FirebaseFirestore db;
    String deviceId;
    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    private ArrayList<OrganizerEvent> eventArrayList;
    private ArrayAdapter<OrganizerEvent> eventArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

<<<<<<< Updated upstream
        //get device id
        deviceId = DeviceUtils.getDeviceId(this);

        //test user document
        Map<String, Object> testUser = new HashMap<>();
        testUser.put("deviceId", deviceId);
        testUser.put("test", "connected");
=======
        Button userButton=findViewById(R.id.user_button);
        userButton.setOnClickListener(v -> {
            //get device id
            deviceId = DeviceUtils.getDeviceId(this);
            ArrayList<User> allUsers= new ArrayList<>();
            usersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        boolean check = false;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d("Firestore", document.getId() + " => " + document.getData());
                            String docDeviceId = document.getString("deviceId");
                            String name = document.getString("name");
                            String email = document.getString("email");
                            String phone = document.getString("phone");

                            if(docDeviceId.equals(deviceId)){
                                check=true;
                                break;
                            }
                        }
                        if(!check) {
                            //send to login page if device id is not in users
                            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                            startActivity(intent);
                        }
                        else{
                            //send to user activity if user exists
                            Intent intent = new Intent(MainActivity.this,UserHomeActivity.class);
                            startActivity(intent);

                        }
                    } else {
                        Log.d("Firestore", "Error getting documents: ", task.getException());
                    }
                }
            });
        });
>>>>>>> Stashed changes

        //write to firestore
        db.collection("users")
                .document(deviceId)
                .set(testUser)
                .addOnSuccessListener(unused -> {

                    //open user homepage when firebase succeeds
                    Intent intent = new Intent(MainActivity.this, UserHomeActivity.class);
                    startActivity(intent);

                    //close mainactivity
                    finish();
                });

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
        eventArrayList = new ArrayList<>();
        eventArrayAdapter = new ArrayAdapter<>(this, 0);

        //testing

        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");
        EventTestData test = new EventTestData();
        List<OrganizerEvent> testList = test.getEvents();
        OrganizerEvent testEvent = testList.get(0);
        testAdd(testEvent);
        testEvent = testList.get(1);
        testAdd(testEvent);

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
    public void testAdd(OrganizerEvent testEvent) {
        //adds event to database... maybe?
        DocumentReference docRef = eventsRef.document(testEvent.getEventID());
        docRef.set(testEvent);
    }
}