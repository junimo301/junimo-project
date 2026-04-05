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
import com.example.junimoapp.utils.NotificationHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
 *  - US 02.05.02: Set the system to sample a specified number of attendees to register for the event
 */
public class Entrants extends AppCompatActivity {

    private LinearLayout invitedEntrants, cancelledEntrants, enrolledEntrants;

    private String eventID;
    FirebaseFirestore db;
    private TextView eventName, backButton;
    private Button lotteryButton, inviteEntrantsButton;

    private List<User> users = Collections.synchronizedList(new ArrayList<>());

    Event selectEvent;

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

        lotteryButton.setEnabled(false);

        selectEvent = EventData.searchEventID(eventID);
        if (selectEvent != null) {
            eventName.setText(selectEvent.getTitle());
        }

        db = FirebaseManager.getDB();

        WaitlistUsers(selectEvent);
        lotteryButton.setOnClickListener(v -> startLottery());

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

        backButton.setOnClickListener(view -> finish());
    }


    private void WaitlistUsers(Event selectEvent) {
        String[] deviceIDs = selectEvent.getWaitList().split(",");

        if (deviceIDs.length == 0 || (deviceIDs.length == 1 && deviceIDs[0].equals(""))) {
            Log.d("waitlist", "no users in waitlist");
            lotteryButton.setEnabled(false);
            return;
        }
        CollectionReference usersRef = db.collection("users");

        final int total = deviceIDs.length;
        final int[] count = {0};

        for (String deviceID : deviceIDs) {
            if (deviceID == null && !deviceID.equals("")) {
                count[0]++;
                continue;
            }
            Log.d("waitlist populating", deviceID);
            usersRef.document(deviceID).get()
                    .addOnCompleteListener(task -> {
                        count[0]++;
                        if (task.isSuccessful() && task.getResult().exists()) {
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
                            } else {
                                Log.d("Firestore", "No such document");
                            }
                        } else {
                            Log.d("Firestore", "get failed with ", task.getException());
                        }
                        if (count[0] == total) {
                            lotteryButton.setEnabled(true);
                            refreshUI();
                        }
                    });
        }
    }

    private void refreshUI() {
        runOnUiThread(() -> {
            invitedEntrants.removeAllViews();
            cancelledEntrants.removeAllViews();
            enrolledEntrants.removeAllViews();

            loadInvitedEntrants(users, eventID);
            loadCancelledEntrants(users, eventID);
            loadEnrolledEntrants(users);
        });
    }
    private void startLottery() {
        lotteryButton.setEnabled(false);

        Log.d("lottery button", "button was pressed");

        // Shuffle users
        Collections.shuffle(users);

        // Get max capacity
        int max = selectEvent.getMaxCapacity();
        // More entrants than capacity — randomly select up to max
        ArrayList<User> selectedUsers = new ArrayList<>();
        int index = Math.min(max, users.size());

        for (int i = 0; i < index; i++) {
            User selected = users.get(i);

            // ─────────────────────────────────────────────
            // US 01.04.01
            // Notify this user they were selected by the lottery.
            // NotificationHelper checks their opt-out preference
            // (US 01.04.03) before writing anything.
            // ─────────────────────────────────────────────
            if(!selected.isInvited(eventID)) {
                selected.inviteUser(selectEvent);
                NotificationHelper.notifyInvited(
                        selected.getDeviceId(),
                        eventID,
                        selectEvent.getTitle()
                );
            }
            selectedUsers.add(selected);

            Log.d("lottery", "selected users: " + selected.getName());
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
        refreshUI();
    }


    /**
     * Loads invited entrants — users whose invitedEvents contains this eventID
     */
    private void loadInvitedEntrants(List<User> usersArray, String eventID) {
        boolean noneInvited = true;
        if (!usersArray.isEmpty()) {
            for (User user : usersArray) {
                Log.d("invited entrants", user.getName() + user.isInvited(eventID));
                if (user.isInvited(eventID)) {
                    TextView textView = new TextView(Entrants.this);
                    textView.setText(user.getName());
                    invitedEntrants.addView(textView);
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

    /**
     * Loads cancelled entrants
     */
    private void loadCancelledEntrants(List<User> usersArray, String eventID) {
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
    private void loadEnrolledEntrants(List<User> usersArray) {
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