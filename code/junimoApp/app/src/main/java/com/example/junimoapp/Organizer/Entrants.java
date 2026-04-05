package com.example.junimoapp.Organizer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.R;
import com.example.junimoapp.admin.AdminUserAdapter;
import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.models.Event;
import com.example.junimoapp.models.User;
import com.example.junimoapp.utils.NotificationHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Entrants names who have joined, cancelled, enrolled, or joined the waiting list of an event.
 * Organizer can view this information and run the lottery.
 *
 * User stories:
 *  - US 02.06.01: View list of all chosen entrants who are invited to apply
 *  - US 02.06.02: See a list of all cancelled entrants
 *  - US 02.06.03: See a final list of entrants who enrolled for the event
 *  - US 02.02.01: View the list of entrants who joined the waiting list
 *  - US 01.04.01: Notify entrant when they are selected by the lottery (added here)
 *  - US 01.04.02: Notify entrant when they are NOT selected by the lottery (added here)
 */
public class Entrants extends AppCompatActivity {

    LinearLayout invitedEntrants;
    LinearLayout cancelledEntrants;
    LinearLayout enrolledEntrants;

    String eventID;
    FirebaseFirestore db;
    TextView eventName;
    TextView backButton;
    Button lotteryButton;
    Button inviteEntrantsButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrants);

        eventName         = findViewById(R.id.event_name);
        invitedEntrants   = findViewById(R.id.invited_entrants);
        cancelledEntrants = findViewById(R.id.cancelled_entrants);
        enrolledEntrants  = findViewById(R.id.enrolled_entrants);
        backButton        = findViewById(R.id.backButton);
        lotteryButton     = findViewById(R.id.startLotteryButton);

        eventID = getIntent().getStringExtra("eventID");

        Event selectEvent = EventData.searchEventID(eventID);
        if (selectEvent != null) {
            eventName.setText(selectEvent.getTitle());
        }

        db = FirebaseManager.getDB();

        ArrayList<User> users = new ArrayList<User>();
        String[] deviceIDs = selectEvent.getWaitList().split(",");

        if (deviceIDs.length >= 1) {
            CollectionReference usersRef = db.collection("users");
            for (String deviceID : deviceIDs) {
                if (deviceID != null && !deviceID.equals("")) {
                    Log.d("waitlist populating", deviceID);
                    usersRef.document(deviceID).get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            User user = new User(
                                                    deviceID,
                                                    document.getString("name"),
                                                    document.getString("email"),
                                                    document.getString("phone"),
                                                    document.getString("organizedEvents"),
                                                    document.getString("invitedEvents"),
                                                    document.getString("waitlistedEvents")
                                            );
                                            users.add(user);
                                            loadInvitedEntrants(users, eventID,selectEvent);
                                            loadCancelledEntrants(users, eventID);
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

        lotteryButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("lottery button", "button was pressed");
                int max = selectEvent.getMaxCapacity();

                if (users.size() > max) {
                    // More entrants than capacity — randomly select up to max
                    ArrayList<User> selectedUsers = new ArrayList<>();
                    int i = 0;
                    while (i < max) {
                        int index = (int) (Math.random() * (users.size()));
                        User selected = users.get(index);
                        selected.initializeEvents();
                        if (!selected.isInvited(eventID)) {
                            selected.inviteUser(selectEvent);

                            // ─────────────────────────────────────────────
                            // US 01.04.01
                            // Notify this user they were selected by the lottery.
                            // NotificationHelper checks their opt-out preference
                            // (US 01.04.03) before writing anything.
                            // ─────────────────────────────────────────────
                            NotificationHelper.notifyInvited(
                                    selected.getDeviceId(),
                                    eventID,
                                    selectEvent.getTitle()
                            );

                            selectedUsers.add(selected);
                            i += 1;
                        }
                    }

                    // ─────────────────────────────────────────────────────
                    // US 01.04.02
                    // Notify users who were NOT selected by the lottery.
                    // Anyone in the waitlist who was not added to selectedUsers
                    // receives a "not chosen" notification.
                    // ─────────────────────────────────────────────────────
                    for (User user : users) {
                        if (!selectedUsers.contains(user)) {
                            NotificationHelper.notifyNotChosen(
                                    user.getDeviceId(),
                                    eventID,
                                    selectEvent.getTitle()
                            );
                        }
                    }

                } else {
                    // Fewer entrants than capacity — invite everyone
                    for (User user : users) {
                        user.initializeEvents();
                        user.inviteUser(selectEvent);
                        Log.d("lottery button", "user was invited");

                        // ─────────────────────────────────────────────────
                        // US 01.04.01
                        // Everyone gets invited so everyone gets the invited
                        // notification. Opt-out is still respected.
                        // ─────────────────────────────────────────────────
                        NotificationHelper.notifyInvited(
                                user.getDeviceId(),
                                eventID,
                                selectEvent.getTitle()
                        );
                    }
                }
            }
        });
// US 02.01.03
// Opens PrivateInviteActivity so the organizer can invite entrants
// to this private event's waiting list at any time after creation.
// ─────────────────────────────────────────────────────────────────────
        inviteEntrantsButton = findViewById(R.id.inviteEntrantsButton);
        inviteEntrantsButton.setOnClickListener(v -> {
            Intent intent = new Intent(Entrants.this, PrivateInviteActivity.class);
            intent.putExtra("eventId", eventID);
            intent.putExtra("eventTitle", selectEvent.getTitle());
            startActivity(intent);
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Entrants.this, SelectAnEvent.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Loads invited entrants — users whose invitedEvents contains this eventID
     */
    private void loadInvitedEntrants(ArrayList<User> usersArray, String eventID, Event selectEvent) {
        boolean noneInvited = true;
        if (!usersArray.isEmpty()) {
            for (User user : usersArray) {
                Log.d("invited entrants", user.getName() + user.isInvited(eventID));
                if (user.isInvited(eventID)) {
                    TextView textView = new TextView(Entrants.this);
                    textView.setText(user.getName());
                    invitedEntrants.addView(textView);
                    textView.setOnClickListener(v->{cancelEntrantDialogue(user,selectEvent,textView);});
                    noneInvited = false;
                }
            }
        }
        if (noneInvited) {
            TextView textView = new TextView(Entrants.this);
            textView.setText("No users have been invited");
            invitedEntrants.addView(textView);
        }
    }

    private void cancelEntrantDialogue(User user, Event event,TextView textView) {
        new AlertDialog.Builder(this)
                .setTitle(this.getString(R.string.remove))
                .setMessage(this.getString(R.string.cancel) + " \"" + user.getName() + "\"" + this.getString(R.string.invite))
                //delete the user
                .setPositiveButton(this.getString(R.string.remove), (dialog, which) -> {user.cancelUser(event); invitedEntrants.removeView(textView);})
                //don't delete (dismiss dialog)
                .setNegativeButton(this.getString(R.string.cancel), null)
                .show();
    }


    /**
     * Loads cancelled entrants
     */
    private void loadCancelledEntrants(ArrayList<User> usersArray, String eventID) {
        boolean noneCancelled = true;
        if (!usersArray.isEmpty()) {
            for (User user : usersArray) {
                if (user.isCancelled(eventID)) {
                    TextView textView = new TextView(Entrants.this);
                    textView.setText(user.getName());
                    cancelledEntrants.addView(textView);
                    noneCancelled = false;
                }
            }
        }
        if (noneCancelled) {
            TextView textView = new TextView(Entrants.this);
            textView.setText("No users have been cancelled");
            cancelledEntrants.addView(textView);
        }
    }

    /**
     * Loads enrolled entrants
     */
    private void loadEnrolledEntrants(ArrayList<User> usersArray) {
        Log.d("entrants view added", usersArray.toString());
        if (!usersArray.isEmpty()) {
            for (User user : usersArray) {
                TextView textView = new TextView(Entrants.this);
                textView.setText(user.getName());
                enrolledEntrants.addView(textView);
                Log.d("entrants view added", user.getName());
            }
        } else {
            TextView textView = new TextView(Entrants.this);
            textView.setText("No users have enrolled in the waitlist");
            enrolledEntrants.addView(textView);
        }
    }
}