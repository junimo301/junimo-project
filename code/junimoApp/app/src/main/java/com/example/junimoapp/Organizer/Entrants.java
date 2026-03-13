package com.example.junimoapp.Organizer;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.R;
import com.example.junimoapp.models.Event;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Entrants extends AppCompatActivity {
    /*
    * User stories:
    * US 02.06.01 As an organizer I want to view a list of all chosen entrants who are invited to apply.
    * US 02.06.02 As an organizer I want to see a list of all the cancelled entrants.
    * US 02.06.03 As an organizer I want to see a final list of entrants who enrolled for the event.
    * US 02.02.01 As an organizer I want to view the list of entrants who joined my event waiting list
    *
    */

    LinearLayout invitedEntrants;
    LinearLayout cancelledEntrants;
    LinearLayout enrolledEntrants;

    String eventID;
    FirebaseFirestore db;
    TextView eventName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrants);

        eventName = findViewById(R.id.event_name);

        invitedEntrants = findViewById(R.id.invited_entrants);
        cancelledEntrants = findViewById(R.id.cancelled_entrants);
        enrolledEntrants = findViewById(R.id.enrolled_entrants);

        eventID = getIntent().getStringExtra("event_ID");

        Event selectEvent = EventData.searchEventID(eventID);
        if (selectEvent != null) {
            eventName.setText(selectEvent.getTitle());
        }


        db = FirebaseFirestore.getInstance();
        loadInvitedEntrants();
        loadCancelledEntrants();
        loadEnrolledEntrants();

    }

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
