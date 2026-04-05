package com.example.junimoapp;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

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
import com.example.junimoapp.utils.BaseActivity;
import com.example.junimoapp.utils.DeviceUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class MainActivity extends BaseActivity {
    String deviceId;
    private FirebaseManager firebase = new FirebaseManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this);
        CollectionReference eventsRef = firebase.getDB().collection("events");
        CollectionReference usersRef = firebase.getDB().collection("users");

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //prevent button from flashing in and out
        Button adminButton = findViewById(R.id.admin_button);
        adminButton.setVisibility(INVISIBLE);
        Button organizerButton = findViewById(R.id.organizer_button);
        organizerButton.setVisibility(INVISIBLE);
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
                        Boolean admin = document.getBoolean("admin");
                        Boolean organizer = document.getBoolean("organizer");

                        if (docDeviceId.equals(deviceId)) {
                            User currentUser = new User(docDeviceId, name, email, phone, organizedEvents, invitedEvents, enrolledEvents);
                            UserSession.setCurrentUser(currentUser);
                            currentUser.initializeEvents();
                            currentUser.setOrganizer(organizer);
                            currentUser.setAdmin(admin);

                            //Organizer button
                            Button organizerButton = findViewById(R.id.organizer_button);
                            if(currentUser.isOrganizer()){
                                organizerButton.setVisibility(VISIBLE);

                                organizerButton.setOnClickListener(v -> {
                                    Intent intent = new Intent(MainActivity.this, OrganizerStartScreen.class);
                                    startActivity(intent);
                                });
                            }
                            //Admin button
                            Button adminButton = findViewById(R.id.admin_button);

                            if(currentUser.isAdmin()) {
                                adminButton.setVisibility(VISIBLE);

                                adminButton.setOnClickListener(v -> {
                                    Intent intent = new Intent(MainActivity.this, AdminHomeActivity.class);
                                    startActivity(intent);
                                });
                            }

                            check = true;
                            break;
                        }
                    }
                    if (!check) {
                        //send to login page if device id is not in users
                        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                        intent.putExtra("new", true);
                        intent.putExtra("organizer", false);
                        startActivity(intent);
                    }
                } else {
                    Log.d("Firestore", "Error getting documents: ", task.getException());
                }
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        //User button
        Button userButton = findViewById(R.id.user_button);
        userButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserHomeActivity.class);
            startActivity(intent);
        });

    }

}