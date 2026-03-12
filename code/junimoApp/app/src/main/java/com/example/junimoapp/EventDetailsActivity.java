package com.example.junimoapp;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.utils.DeviceUtils;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

/**
 * user stories implemented:
 *  - US 01.05.02: Entrant wants to be able to accept an invite when chosen.
 *  - US 01.05.03: Entrant wants to be able to decline an invitation if they are chosen.
 *  - US 01.06.02: Entrant wants to be able to sign up for a waiting list from event details.
 */

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
        setContentView(R.layout.indivdual_invite_page);

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