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

import com.example.junimoapp.R;
import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.models.Event;
import com.example.junimoapp.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

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
    Button lotteryButton;

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
        lotteryButton = findViewById(R.id.startLotteryButton);

        eventID = getIntent().getStringExtra("event_ID");

        Event selectEvent = EventData.searchEventID(eventID);
        if (selectEvent != null) {
            eventName.setText(selectEvent.getTitle());
        }

        /** initialize firestore
         * loads entrants from firestore
         */
        db = FirebaseManager.getDB();

        ArrayList<User> users = new ArrayList<User>();
        String[] deviceIDs = selectEvent.getWaitList().split(",");
        if (deviceIDs.length>=1) {
            CollectionReference usersRef = db.collection("users");
            for (String deviceID : deviceIDs) {
                if (deviceID != null && deviceID != "") {
                    Log.d("waitlist populating", deviceID);
                    usersRef.document(deviceID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d("Firestore", "DocumentSnapshot data: " + document.getData());
                                    User user = new User(deviceID, document.getString("name"), document.getString("email"), document.getString("phone"),document.getString("organizedEvents"),document.getString("invitedEvents"),document.getString("waitlistedEvents"));
                                    users.add(user);
                                    loadInvitedEntrants(users, selectEvent);
                                    loadCancelledEntrants(users,selectEvent);
                                    loadEnrolledEntrants(users);

                                } else {
                                    Log.d("Firestore", "No such document");
                                }
                            } else {
                                Log.d("Firestore", "get failed with ", task.getException());
                            }
                        }
                    });
                }
            }
        }

        lotteryButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Log.d("lottery button","button was pressed");
                int max=selectEvent.getMaxCapacity();
                if(users.size()>max) {
                    int i =0;
                    while (i < max) {
                        int index = (int) (Math.random() * (max + 1));
                        User selected = users.get(index);
                        if (!selected.isInvited(selectEvent)) {
                            selected.inviteUser(selectEvent);
                            i += 1;
                        }
                    }
                }
                else {
                    for(User user : users){
                        user.inviteUser(selectEvent);
                        Log.d("lottery button","user was invited");
                    }
                }
            }
        });

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
    private void loadInvitedEntrants(ArrayList<User> usersArray, Event selectedEvent) {
        boolean noneInvited = true;
        if(usersArray.size()>=1) {
            for (User user : usersArray) {
                Log.d("invited entrants",user.getName() + user.isInvited(selectedEvent));
                if (user.isInvited(selectedEvent)) {
                    String name = user.getName();
                    TextView textView = new TextView(Entrants.this);
                    textView.setText(name);
                    invitedEntrants.addView(textView);
                    noneInvited=false;
                }
            }
        }
        if(noneInvited) {
            TextView textView = new TextView(Entrants.this);
            textView.setText("No users have been invited");
            invitedEntrants.addView(textView);
        }
    }

    /**
     * loads cancelled entrants from firestore
     * adds names to the cancelled entrants container
     *
     */
    private void loadCancelledEntrants(ArrayList<User> usersArray, Event selectedEvent) {
        boolean noneCancelled = true;
        if(usersArray.size()>=1) {
            for (User user : usersArray) {
                if (user.isInvited(selectedEvent)) {
                    String name = user.getName();
                    TextView textView = new TextView(Entrants.this);
                    textView.setText(name);
                    cancelledEntrants.addView(textView);
                    noneCancelled=false;
                }
            }
        }
        if(noneCancelled) {
            TextView textView = new TextView(Entrants.this);
            textView.setText("No users have been cancelled");
            cancelledEntrants.addView(textView);
        }
    }

    /**
     * loads enrolled entrants from firestore
     * adds names to the enrolled entrants container
     *
     */
    private void loadEnrolledEntrants(ArrayList<User> usersArray) {
        Log.d("entrants view added",usersArray.toString());
        if(usersArray.size()>=1) {
            for (User user : usersArray) {
                String name = user.getName();
                TextView textView = new TextView(Entrants.this);
                textView.setText(name);
                enrolledEntrants.addView(textView);
                Log.d("entrants view added", name);
            }
        }
        else {
            TextView textView = new TextView(Entrants.this);
            textView.setText("No users have enrolled in the waitlist");
            enrolledEntrants.addView(textView);
        }
    }


}
