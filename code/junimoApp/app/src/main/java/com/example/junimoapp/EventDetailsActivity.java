package com.example.junimoapp;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.utils.DeviceUtils;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
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
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        return;
                    }
                    String startDate = documentSnapshot.getString("startDate");
                    String endDate = documentSnapshot.getString("endDate");
                    Long waitingListLimit = documentSnapshot.getLong("waitingListLimit");

                    //null check
                    if (startDate == null || endDate == null || waitingListLimit == null) {
                        return;
                    }
                    //close registration period if time is over
                    if (!registrationPeriod(startDate, endDate)) {
                        joinWaitlistButton.setEnabled(false);
                        joinWaitlistButton.setText("Registration period not open");
                        return;
                    }

                    db.collection("events")
                            .document(eventId)
                            .collection("waitlist")
                            .get()
                            .addOnSuccessListener(waitlistSnapshot -> {
                                int size = waitlistSnapshot.size();
                                //check if waitlist is full
                                if (size >= waitingListLimit) {
                                    joinWaitlistButton.setEnabled(false);
                                    joinWaitlistButton.setText("Waiting list full");
                                    return;
                                }

                                //registration period and waitlist is open
                                db.collection("events")
                                        .document(eventId)
                                        .collection("waitlist")
                                        .document(deviceId)
                                        .set(new HashMap<>());
                            });
                });
    }

    //check if registration period is open
    private boolean registrationPeriod(String startDate, String endDate) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date start = format.parse(startDate);
            Date end = format.parse(endDate);
            Date now = new Date();
            return !(now.before(start) || now.after(end));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

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