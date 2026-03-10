package com.example.junimoapp;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.utils.DeviceUtils;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class EventDetailsActivity extends AppCompatActivity {

    FirebaseFirestore db;
    String deviceId;
    String eventId;

    Button joinWaitlistButton;
    Button acceptButton;
    Button declineButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        db = FirebaseManager.getDB();
        deviceId = DeviceUtils.getDeviceId(this);

        //eventId should be passed from previous activity
        eventId = getIntent().getStringExtra("eventId");

        joinWaitlistButton = findViewById(R.id.joinWaitlistButton);
        acceptButton = findViewById(R.id.acceptButton);
        declineButton = findViewById(R.id.declineButton);

        joinWaitlistButton.setOnClickListener(v -> joinWaitlist());
        acceptButton.setOnClickListener(v -> acceptInvite());
        declineButton.setOnClickListener(v -> declineInvite());
    }

    private void joinWaitlist() {

        db.collection("events")
                .document(eventId)
                .collection("waitlist")
                .document(deviceId)
                .set(new HashMap<>());
    }

    private void acceptInvite() {

        db.collection("events")
                .document(eventId)
                .collection("acceptedUsers")
                .document(deviceId)
                .set(new HashMap<>());

        db.collection("events")
                .document(eventId)
                .collection("waitlist")
                .document(deviceId)
                .delete();
    }

    private void declineInvite() {

        db.collection("events")
                .document(eventId)
                .collection("declinedUsers")
                .document(deviceId)
                .set(new HashMap<>());

        db.collection("events")
                .document(eventId)
                .collection("waitlist")
                .document(deviceId)
                .delete();
    }
}