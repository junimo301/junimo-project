package com.example.junimoapp.Organizer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.OrganizerStartScreen;
import com.example.junimoapp.R;
import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.models.Event;
import com.example.junimoapp.models.User;
import com.example.junimoapp.utils.NotificationHelper;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entrants names who have joined, cancelled, enrolled, or joined the waiting list of an event.
 * Organizer can view this information and run the lottery.
 *
 * User stories:
 *  - US 02.06.01: View list of all chosen entrants who are invited to apply
 *  - US 02.06.02: See a list of all cancelled entrants
 *  - US 02.06.03: See a final list of entrants who enrolled for the event
 *  - US 02.02.01: View the list of entrants who joined the waiting list
 *  - US 01.04.01: Notify entrant when they are selected by the lottery
 *  - US 01.04.02: Notify entrant when they are NOT selected by the lottery
 *  - US 02.05.02: Sample a specified number of attendees to register for the event
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

        // ─────────────────────────────────────────────────────────────────
        // Load waitlist fresh from Firestore before populating the users list.
        // The cached Event object's waitList may be empty because
        // initializeWaitlist() is async and may not have finished yet.
        //
        // We check both "waitList" (capital L — used by EventDetailsActivity)
        // and "waitlist" (lowercase — used by Event.enrollInWaitList) because
        // both field names exist in Firestore due to a naming inconsistency.
        // ─────────────────────────────────────────────────────────────────
        db.collection("events").document(eventID).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String waitlist = doc.getString("waitList");
                        if (waitlist == null || waitlist.equals("")) {
                            waitlist = doc.getString("waitlist");
                        }
                        if (waitlist != null && !waitlist.equals("")) {
                            selectEvent.setWaitList(waitlist);
                            Log.d("lottery button", "loaded waitlist: " + waitlist);
                        } else {
                            Log.d("lottery button", "waitlist is empty in Firestore");
                        }
                    }
                    WaitlistUsers(selectEvent);
                })
                .addOnFailureListener(e -> {
                    Log.e("lottery button", "Failed to load waitlist from Firestore", e);
                    WaitlistUsers(selectEvent);
                });

        lotteryButton.setOnClickListener(v -> startLottery());

        // ─────────────────────────────────────────────────────────────────
        // US 02.01.03
        // Opens PrivateInviteActivity so the organizer can invite entrants
        // to this private event's waiting list at any time after creation.
        // ─────────────────────────────────────────────────────────────────
        inviteEntrantsButton = findViewById(R.id.inviteEntrantsButton);
        inviteEntrantsButton.setOnClickListener(v -> {
            Intent intent = new Intent(Entrants.this, PrivateInviteActivity.class);
            intent.putExtra("eventId", eventID);
            intent.putExtra("eventTitle", selectEvent.getTitle());
            startActivity(intent);
        });

        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(Entrants.this, OrganizerStartScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
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
            if (deviceID == null || deviceID.equals("")) {
                count[0]++;
                continue;
            }
            Log.d("waitlist populating", deviceID);
            usersRef.document(deviceID).get()
                    .addOnCompleteListener(task -> {
                        count[0]++;
                        if (task.isSuccessful() && task.getResult().exists()) {
                            DocumentSnapshot document = task.getResult();
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
                            Log.d("Firestore", "get failed or no document");
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
            loadInvitedEntrants(users, eventID, selectEvent);
            loadCancelledEntrants(users, eventID);
            loadEnrolledEntrants(users);
        });
    }

    private void startLottery() {
        lotteryButton.setEnabled(false);
        Log.d("lottery button", "button was pressed");
        Log.d("lottery button", "users size: " + users.size());

        db.collection("events").document(eventID).collection("acceptedUsers").get()
                .addOnSuccessListener(acceptedSnapshot -> {
                    Set<String> acceptedIds = new HashSet<>();
                    for (QueryDocumentSnapshot document : acceptedSnapshot) {
                        acceptedIds.add(document.getId());
                    }

                    db.collection("events").document(eventID).collection("declinedUsers").get()
                            .addOnSuccessListener(declinedSnapshot -> {
                                Set<String> declinedIds = new HashSet<>();
                                for (QueryDocumentSnapshot document : declinedSnapshot) {
                                    declinedIds.add(document.getId());
                                }
                                drawReplacementApplicants(acceptedIds, declinedIds);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("lottery button", "Failed to load declined users", e);
                                drawReplacementApplicants(acceptedIds, new HashSet<>());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("lottery button", "Failed to load accepted users", e);
                    drawReplacementApplicants(new HashSet<>(), new HashSet<>());
                });
    }

    private void drawReplacementApplicants(Set<String> acceptedIds, Set<String> declinedIds) {
        ArrayList<User> eligibleUsers = new ArrayList<>();
        ArrayList<User> currentlyInvitedUsers = new ArrayList<>();

        for (User user : users) {
            String userId = user.getDeviceId();
            if (acceptedIds.contains(userId) || declinedIds.contains(userId) || user.isCancelled(eventID)) {
                continue;
            }
            if (user.isInvited(eventID)) {
                currentlyInvitedUsers.add(user);
                continue;
            }
            eligibleUsers.add(user);
        }

        int openSpots = selectEvent.getMaxCapacity() - acceptedIds.size() - currentlyInvitedUsers.size();
        if (openSpots <= 0 || eligibleUsers.isEmpty()) {
            refreshUI();
            lotteryButton.setEnabled(true);
            return;
        }

        boolean initialLottery = acceptedIds.isEmpty() && declinedIds.isEmpty() && currentlyInvitedUsers.isEmpty();

        Collections.shuffle(eligibleUsers);
        ArrayList<User> selectedUsers = new ArrayList<>();
        int index = Math.min(openSpots, eligibleUsers.size());
        Log.d("lottery button", "selecting " + index + " replacement users out of " + eligibleUsers.size());

        for (int i = 0; i < index; i++) {
            User selected = eligibleUsers.get(i);
            selected.inviteUser(selectEvent);
            NotificationHelper.notifyInvited(
                    selected.getDeviceId(),
                    eventID,
                    selectEvent.getTitle()
            );
            selectedUsers.add(selected);
            Log.d("lottery", "selected: " + selected.getName());
        }

        if (initialLottery) {
            for (User user : eligibleUsers) {
                if (!selectedUsers.contains(user)) {
                    NotificationHelper.notifyNotChosen(
                            user.getDeviceId(),
                            eventID,
                            selectEvent.getTitle()
                    );
                    Log.d("lottery", "not chosen: " + user.getName());
                }
            }
        }

        refreshUI();
        lotteryButton.setEnabled(true);
    }

    /**
     * Loads invited entrants — users whose invitedEvents contains this eventID.
     * Tapping an invited entrant opens a dialog to cancel their invite.
     */
    private void loadInvitedEntrants(List<User> usersArray, String eventID, Event selectEvent) {
        boolean noneInvited = true;
        if (!usersArray.isEmpty()) {
            for (User user : usersArray) {
                Log.d("invited entrants", user.getName() + " invited: " + user.isInvited(eventID));
                if (user.isInvited(eventID)) {
                    TextView textView = new TextView(Entrants.this);
                    textView.setText(user.getName());
                    invitedEntrants.addView(textView);
                    textView.setOnClickListener(v -> cancelEntrantDialogue(user, selectEvent, textView));
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
     * Shows a dialog to cancel an entrant's invite.
     * Added by teammate — allows organizer to remove an invited entrant.
     */
    private void cancelEntrantDialogue(User user, Event event, TextView textView) {
        new AlertDialog.Builder(this)
                .setTitle(this.getString(R.string.remove))
                .setMessage(this.getString(R.string.cancel) + " \"" + user.getName() + "\"" + this.getString(R.string.invite))
                .setPositiveButton(this.getString(R.string.remove), (dialog, which) -> {
                    user.cancelUser(event);
                    invitedEntrants.removeView(textView);
                })
                .setNegativeButton(this.getString(R.string.cancel), null)
                .show();
    }

    private void loadCancelledEntrants(List<User> usersArray, String eventID) {
        boolean noneCancelled = true;
        for (User user : usersArray) {
            if (user.isCancelled(eventID)) {
                TextView textView = new TextView(Entrants.this);
                textView.setText(user.getName());
                cancelledEntrants.addView(textView);
                noneCancelled = false;
            }
        }
        if (noneCancelled) {
            TextView textView = new TextView(Entrants.this);
            textView.setText("No users have been cancelled");
            cancelledEntrants.addView(textView);
        }
    }

    private void loadEnrolledEntrants(List<User> usersArray) {
        if (!usersArray.isEmpty()) {
            for (User user : usersArray) {
                TextView textView = new TextView(Entrants.this);
                textView.setText(user.getName());
                enrolledEntrants.addView(textView);
            }
        } else {
            TextView textView = new TextView(Entrants.this);
            textView.setText("No users have enrolled in the waitlist");
            enrolledEntrants.addView(textView);
        }
    }
}
