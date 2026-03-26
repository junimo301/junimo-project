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


import com.example.junimoapp.admin.AdminHomeActivity;
import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.models.User;
import com.example.junimoapp.models.UserSession;
import com.example.junimoapp.utils.DeviceUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;

import java.util.HashMap;
import java.util.Map;
import com.example.junimoapp.models.Event;
import com.example.junimoapp.OrganizerStartScreen;
import com.example.junimoapp.TestData.EventTestData;

import com.example.junimoapp.models.Event;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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

        Button userButton = findViewById(R.id.user_button);
        userButton.setOnClickListener(v -> {
            //get device id
            deviceId = DeviceUtils.getDeviceId(this);
            ArrayList<User> allUsers = new ArrayList<>();
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
                            String organizedEvents = document.getString("organizedEvents");
                            String invitedEvents = document.getString("invitedEvents");
                            String enrolledEvents = document.getString("enrolledEvents");

                            if (docDeviceId.equals(deviceId)) {
                                User currentUser = new User(docDeviceId, name, email, phone,organizedEvents,invitedEvents,enrolledEvents);
                                UserSession.setCurrentUser(currentUser);
                                check = true;
                                break;
                            }
                        }
                        if (!check) {
                            //send to login page if device id is not in users
                            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                            intent.putExtra("new",true);
                            startActivity(intent);
                        } else {
                            //send to user activity if user exists
                            Intent intent = new Intent(MainActivity.this, UserHomeActivity.class);
                            startActivity(intent);

                        }
                    } else {
                        Log.d("Firestore", "Error getting documents: ", task.getException());
                    }
                }
            });
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button organizerButton = findViewById(R.id.organizer_button);

        organizerButton.setOnClickListener(v -> {
            //get device id
            deviceId = DeviceUtils.getDeviceId(this);
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
                            String organizedEvents = document.getString("organizedEvents");
                            String invitedEvents = document.getString("invitedEvents");
                            String enrolledEvents = document.getString("enrolledEvents");

                            if (docDeviceId.equals(deviceId)) {
                                User currentUser = new User(docDeviceId, name, email, phone,organizedEvents,invitedEvents,enrolledEvents);
                                UserSession.setCurrentUser(currentUser);
                                check = true;
                                break;
                            }
                        }
                        if (!check) {
                            //send to login page if device id is not in users
                            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                            intent.putExtra("new",true);
                            startActivity(intent);
                        } else {
                            //send to organizer activity if user exists
                            Intent intent = new Intent(MainActivity.this, OrganizerStartScreen.class);
                            startActivity(intent);

                        }
                    } else {
                        Log.d("Firestore", "Error getting documents: ", task.getException());
                    }
                }
            });
        });


        //Admin button
        Button adminButton = findViewById(R.id.admin_button);
        adminButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdminHomeActivity.class);
            startActivity(intent);
        });

    }

}