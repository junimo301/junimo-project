package com.example.junimoapp.Organizer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.OrganizerStartScreen;
import com.example.junimoapp.R;
import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.models.Event;
import com.example.junimoapp.models.User;
import com.example.junimoapp.models.WaitList;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Entrants names who have joined cancelled, enrolled, or joined the waiting list of an event
 * Organizer can view this information
 */
public class Entrants extends AppCompatActivity {
    /*
     * User stories:
     * US 02.06.01 As an organizer I want to view a list of all chosen entrants who are invited to apply.
     * US 02.06.02 As an organizer I want to see a list of all the cancelled entrants.
     * US 02.06.03 As an organizer I want to see a final list of entrants who enrolled for the event.
     * US 02.02.01 As an organizer I want to view the list of entrants who joined my event waiting list
     *
     */

    /**
     * container for displaying entrants info
     */
    LinearLayout invitedEntrants;
    LinearLayout cancelledEntrants;
    LinearLayout enrolledEntrants;

    String eventID;
    FirebaseFirestore db;
    TextView eventName;
    TextView backButton;

    /**
     * loads data, when activity is first created
     * gets entrants from firestore
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrants);

        eventName = findViewById(R.id.event_name);

        invitedEntrants = findViewById(R.id.invited_entrants);
        cancelledEntrants = findViewById(R.id.cancelled_entrants);
        enrolledEntrants = findViewById(R.id.enrolled_entrants);

        backButton = findViewById(R.id.backButton);


        eventID = getIntent().getStringExtra("event_ID");

        Event selectEvent = EventData.searchEventID(eventID);
        if (selectEvent != null) {
            eventName.setText(selectEvent.getTitle());
        }
        selectEvent.initializeWaitlist();

        /** initialize firestore
         * loads entrants from firestore
         */
        db = FirebaseManager.getDB();

        loadInvitedEntrants(selectEvent);
        loadCancelledEntrants();
        loadEnrolledEntrants(selectEvent);

        /** returns to select an event screen */
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Entrants.this, SelectAnEvent.class);
                startActivity(intent);
            }
        });
    }

    /**
     * loads invited entrants from firestore (waitlist)
     * adds their name to the invited entrants container
     *
     */
    private void loadInvitedEntrants(Event selectedEvent) {
        WaitList waitlist = new WaitList(selectedEvent);
        waitlist.populateWaitList(selectedEvent);

        ArrayList<User> usersArray = waitlist.getUsers();
        for (User user : usersArray) {
            if (user.isInvited(selectedEvent)) {
                String name = user.getName();
                TextView textView = new TextView(Entrants.this);
                textView.setText(name);
                invitedEntrants.addView(textView);
            }
        }
//        ArrayList<String> userIDs = selectedEvent.getWaitList();
//        Log.d("entrants list",userIDs.toString());
//
//        for (String userID : userIDs) {
//            db.collection("users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                    if (task.isSuccessful()) {
//                        DocumentSnapshot doc = task.getResult();
//                        String entrantName = doc.getString("name");
//                        if (entrantName != null) {
//                            TextView textView = new TextView(Entrants.this);
//                            textView.setText(entrantName);
//                            invitedEntrants.addView(textView);
//                            Log.d("entrants view added",entrantName);
//                        }
//                    }
//                }
//            });
//        }
    }

    /**
     * loads cancelled entrants from firestore
     * adds names to the cancelled entrants container
     *
     */
    private void loadCancelledEntrants() {
//        db.collection("events")
//                .document(eventID)
//                .collection("declinedUsers")
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
//                        String entrantName = document.getString("name");
//                        if (entrantName != null) {
//                            TextView textView = new TextView(this);
//                            textView.setText(entrantName);
//                            cancelledEntrants.addView(textView);
//                        }
//                    }
//
//                });
    }

    /**
     * loads enrolled entrants from firestore
     * adds names to the enrolled entrants container
     *
     */
    private void loadEnrolledEntrants(Event selectedEvent) {
        WaitList waitlist = new WaitList(selectedEvent);
        waitlist.populateWaitList(selectedEvent);
        ArrayList<User> usersArray = waitlist.getUsers();
        Log.d("entrants view added",usersArray.toString());
        for (User user : usersArray) {
            String name = user.getName();
            TextView textView = new TextView(Entrants.this);
            textView.setText(name);
            enrolledEntrants.addView(textView);
            Log.d("entrants view added",name);
        }
//        db.collection("events")
//                .document(eventID)
//                .collection("acceptedUsers")
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
//                        String entrantName = document.getString("name");
//                        if (entrantName != null) {
//                            TextView textView = new TextView(this);
//                            textView.setText(entrantName);
//                            enrolledEntrants.addView(textView);
//                        }
//                    }
//                });
    }


}
