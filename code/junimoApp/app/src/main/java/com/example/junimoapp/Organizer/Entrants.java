package com.example.junimoapp.Organizer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
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
 *  - US 01.04.01: Notify entrant when they are selected by the lottery
 *  - US 01.04.02: Notify entrant when they are NOT selected by the lottery
 *  - US 02.05.02: Sample a specified number of attendees to register for the event
 */
public class Entrants extends AppCompatActivity {

    private LinearLayout invitedEntrants, cancelledEntrants, enrolledEntrants, acceptedEntrants;

    private String eventID;
    FirebaseFirestore db;
    private TextView eventName, backButton;
    private Button lotteryButton, inviteEntrantsButton, exportButton;

    private List<User> users = Collections.synchronizedList(new ArrayList<>());

    private ArrayList<User> acceptedUsers;
    Event selectEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrants);

        eventName         = findViewById(R.id.event_name);
        acceptedEntrants  = findViewById(R.id.accepted_entrants);
        invitedEntrants   = findViewById(R.id.invited_entrants);
        cancelledEntrants = findViewById(R.id.cancelled_entrants);
        enrolledEntrants  = findViewById(R.id.enrolled_entrants);
        backButton        = findViewById(R.id.backButton);
        lotteryButton     = findViewById(R.id.startLotteryButton);
        exportButton      = findViewById(R.id.accepted_entrants_button);

        eventID = getIntent().getStringExtra("eventID");
        lotteryButton.setEnabled(false);
        exportButton.setEnabled(false);

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

        ArrayList<String> acceptedUsersString= new ArrayList<String>();
        db.collection("events").document(eventID).collection("acceptedUsers").get()
                .addOnSuccessListener(docs -> {
           for(DocumentSnapshot document : docs){
               if (document.exists()){
                  String deviceID=document.getString("name");
                  acceptedUsersString.add(deviceID);
               }
           }
           acceptedUsersInitialize(acceptedUsersString);
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

        exportButton.setOnClickListener(view ->{
            exportCSV();
        });
    }

    private void exportCSV(){
        EditText filepath;

        new AlertDialog.Builder(this)
                .setTitle("Export CSV")
                .setMessage("Export a list of all entrants that accepted their invitations into a CSV file.\nEnter filename: ")
                .setView(filepath = new EditText(this))
                .setPositiveButton("Export", (dialog, which) -> {
                    String chosenFilePath = filepath.getText().toString();
                    String csv_content="Name,Email,Phone,";
                    for(User user : acceptedUsers){
                        String user_line=user.getName()+","+user.getEmail()+","+user.getPhone()+",";
                        csv_content += user_line;
                    }
                    Log.d("CSV contents",csv_content);
                    writeToFile(csv_content,this,chosenFilePath);
                })
                .setNegativeButton(this.getString(R.string.cancel), null)
                .show();
    }
    private void writeToFile(String data,Context context, String filename) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            Log.d("write to file",outputStreamWriter.getEncoding()+"or null");
            outputStreamWriter.close();
            Log.d("write to file","write to file complete");
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    private void acceptedUsersInitialize(ArrayList<String> acceptedUsersString){
        acceptedUsers=new ArrayList<>();
        if (acceptedUsersString.isEmpty()) {
            Log.d("waitlist", "no users have accepted");
            return;
        }
        CollectionReference usersRef = db.collection("users");
        for(String deviceID : acceptedUsersString){
            if (deviceID == null || deviceID.equals("")) {
                continue;
            }
            usersRef.document(deviceID).get()
                    .addOnCompleteListener(task -> {
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
                            TextView textView = new TextView(Entrants.this);
                            textView.setText(user.getName());
                            acceptedEntrants.addView(textView);
                            user.initializeEvents();
                            acceptedUsers.add(user);
                            exportButton.setEnabled(true);
                        } else {
                            Log.d("Firestore", "get failed or no document");
                        }
                    });

        }
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
                            user.initializeEvents();
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

        Collections.shuffle(users);

        int max = selectEvent.getMaxCapacity();
        ArrayList<User> selectedUsers = new ArrayList<>();
        int index = Math.min(max, users.size());
        Log.d("lottery button", "selecting " + index + " out of " + users.size() + " users");

        for (int i = 0; i < index; i++) {
            User selected = users.get(i);

            // ─────────────────────────────────────────────────────────────
            // US 01.04.01
            // Invite user and notify them they were selected by the lottery.
            // NotificationHelper checks their opt-out preference (US 01.04.03)
            // before writing the notification.
            // ─────────────────────────────────────────────────────────────
            if (!selected.isInvited(eventID)) {
                selected.inviteUser(selectEvent);
            }
            NotificationHelper.notifyInvited(
                    selected.getDeviceId(),
                    eventID,
                    selectEvent.getTitle()
            );
            selectedUsers.add(selected);
            Log.d("lottery", "selected: " + selected.getName());
        }

        // ─────────────────────────────────────────────────────────────────
        // US 01.04.02
        // Notify users who were NOT selected by the lottery.
        // ─────────────────────────────────────────────────────────────────
        for (User user : users) {
            if (!selectedUsers.contains(user)) {
                NotificationHelper.notifyNotChosen(
                        user.getDeviceId(),
                        eventID,
                        selectEvent.getTitle()
                );
                Log.d("lottery", "not chosen: " + user.getName());
            }
        }

        refreshUI();
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
            textView.setText(R.string.no_users_have_been_invited);
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
            textView.setText(R.string.no_users_have_been_cancelled);
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
            textView.setText(R.string.no_users_have_enrolled_in_the_waitlist);
            enrolledEntrants.addView(textView);
        }
    }
}