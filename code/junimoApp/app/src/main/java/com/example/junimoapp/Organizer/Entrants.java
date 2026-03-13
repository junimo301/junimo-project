package com.example.junimoapp.Organizer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.OrganizerStartScreen;
import com.example.junimoapp.R;
import com.example.junimoapp.models.Event;
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

    /** container for displaying entrants info */
    LinearLayout invitedEntrants;
    LinearLayout cancelledEntrants;
    LinearLayout enrolledEntrants;

    String eventID;
    FirebaseFirestore db;
    TextView eventName;
    Button backButton;

    /** loads data, when activity is first created
     * gets entrants from firestore
     * @param savedInstanceState*/
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

        /** initialize firestore
         * loads entrants from firestore
         */
        db = FirebaseFirestore.getInstance();
        loadInvitedEntrants();
        loadCancelledEntrants();
        loadEnrolledEntrants();

        /** returns to select an event screen */
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Entrants.this, SelectAnEvent.class);
                startActivity(intent);            }
        });
    }

    /**
     * loads invited entrants from firestore (waiting list)
     * adds their name to the invited entrants container
     * */
    private void loadInvitedEntrants() {
        db.collection("events")
                .document(eventID)
                .collection("waitlist")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> invitedEntrantNames = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String entrantName = document.getString("name");
                        if (entrantName != null) {
                            TextView textView = new TextView(this);
                            textView.setText(entrantName);
                            invitedEntrants.addView(textView);
                        }
                    }
                });
    }

    /**
     * loads cancelled entrants from firestore
     * adds names to the cancelled entrants container
     * */
    private void loadCancelledEntrants() {
        db.collection("events")
                .document(eventID)
                .collection("declinedUsers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> cancelledEntrantNames = new ArrayList<>();

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String entrantName = document.getString("name");
                        if (entrantName != null) {
                            TextView textView = new TextView(this);
                            textView.setText(entrantName);
                            cancelledEntrants.addView(textView);
                        }
                    }

                });
    }

    /**
     * loads enrolled entrants from firestore
     * adds names to the enrolled entrants container
     * */
    private void loadEnrolledEntrants() {
        db.collection("events")
                .document(eventID)
                .collection("acceptedUsers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> enrolledEntrantNames = new ArrayList<>();

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String entrantName = document.getString("name");
                        if (entrantName != null) {
                            TextView textView = new TextView(this);
                            textView.setText(entrantName);
                            enrolledEntrants.addView(textView);
                        }
                    }
                });
    }


}
