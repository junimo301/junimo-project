package com.example.junimoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.junimoapp.adapters.InvitationAdapter;
import com.example.junimoapp.models.InvitationItem;
import com.example.junimoapp.models.UserSession;
import com.example.junimoapp.utils.BaseActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides logic for invitations to an event, including accepting/declining an invite.
 *
 * User stories implemented here:
 *  - US 01.05.02: Entrant wants to accept an invite when chosen
 *  - US 01.05.03: Entrant wants to decline an invitation if they are chosen
 *  - US 01.05.07: Entrant wants to accept or decline an invitation to join
 *                 the waiting list for a private event
 *
 * How invitations are loaded (US 01.05.07 note):
 *  - Anica's loadInvitations() reads from the "invitations" List field.
 *  - Private invites written by PrivateInviteActivity use the "invitedEvents"
 *    comma-String field (consistent with User.java).
 *  - We load from BOTH fields so both regular and private invitations appear.
 *
 * On decline (US 01.05.07):
 *  - We remove the eventID from the user's invitedEvents string so the
 *    private invite no longer shows — consistent with how User.cancelUser() works.
 */
public class InvitationsActivity extends BaseActivity {

    private InvitationAdapter adapter;
    private TextView backButton;
    private final List<InvitationItem> invitations = new ArrayList<>();
    private String deviceId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invite_page);

        RecyclerView recyclerView = findViewById(R.id.invitationsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // ─────────────────────────────────────────────────────────────────
        // US 01.05.07
        // Use UserSession to get the deviceId — consistent with the rest
        // of the app (per code review feedback on UserHomeActivity).
        // ─────────────────────────────────────────────────────────────────
        deviceId = UserSession.getCurrentUser().getDeviceId();
        db = FirebaseFirestore.getInstance();

        backButton = findViewById(R.id.backToHomeText);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InvitationsActivity.this, UserHomeActivity.class);
                startActivity(intent);
            }
        });

        adapter = new InvitationAdapter(invitations, new InvitationAdapter.InvitationListener() {
            @Override
            public void onAccept(String eventID) {
                acceptInvite(eventID);
            }

            @Override
            public void onDecline(String eventID) {
                declineInvite(eventID);
            }
        });

        recyclerView.setAdapter(adapter);

        // Load both regular and private invitations
        loadInvitations();
        loadPrivateInvitations();
    }

    /**
     * US 01.05.02 / US 01.05.03
     * Loads regular lottery invitations from the "invitations" List field.
     * This is Anica's original implementation — unchanged.
     */
    private void loadInvitations() {
        db.collection("users").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Object invitationsObj = documentSnapshot.get("invitations");
                        if (invitationsObj instanceof List) {
                            List<?> eventIdsRaw = (List<?>) invitationsObj;
                            for (Object idObj : eventIdsRaw) {
                                if (idObj instanceof String) {
                                    String eventID = (String) idObj;
                                    loadEventIntoList(eventID);
                                }
                            }
                        }
                    }
                });
    }

    /**
     * US 01.05.07
     * Loads private event invitations from the "invitedEvents" comma-String field.
     * PrivateInviteActivity writes to this field when an organizer invites someone.
     * We skip any eventID already loaded from the regular "invitations" list
     * to avoid showing the same event twice.
     */
    private void loadPrivateInvitations() {
        db.collection("users").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) return;

                    String invitedEvents = documentSnapshot.getString("invitedEvents");
                    if (invitedEvents == null || invitedEvents.equals("")) return;

                    String[] eventIds = invitedEvents.split(",");
                    for (String eventID : eventIds) {
                        if (eventID == null || eventID.equals("")) continue;

                        // Skip if already loaded from the regular invitations list
                        boolean alreadyLoaded = false;
                        for (InvitationItem item : invitations) {
                            if (item.getEventId().equals(eventID)) {
                                alreadyLoaded = true;
                                break;
                            }
                        }
                        if (!alreadyLoaded) {
                            loadEventIntoList(eventID);
                        }
                    }
                });
    }

    /**
     * Fetches event details from Firestore and adds it to the invitations list.
     * Shared by both loadInvitations() and loadPrivateInvitations().
     */
    private void loadEventIntoList(String eventID) {
        db.collection("events").document(eventID).get()
                .addOnSuccessListener(eventSnap -> {
                    if (eventSnap.exists()) {
                        String title = eventSnap.getString("title");

                        // ─────────────────────────────────────────────────
                        // US 01.05.07
                        // Label private event invites so the entrant knows
                        // they are being invited to a private event waitlist.
                        // ─────────────────────────────────────────────────
                        Boolean isPrivate = eventSnap.getBoolean("isPrivate");
                        String displayTitle = Boolean.TRUE.equals(isPrivate)
                                ? title + " [Private]"
                                : title;

                        int position = invitations.size();
                        invitations.add(new InvitationItem(eventID, displayTitle));
                        adapter.notifyItemInserted(position);
                    }
                });
    }

    /**
     * US 01.05.02
     * Accept a regular lottery invitation — adds user to acceptedUsers subcollection
     * and removes them from the waitlist subcollection.
     * Unchanged from Anica's original implementation.
     */
    private void acceptInvite(String eventID) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", deviceId);

        db.collection("events").document(eventID)
                .collection("acceptedUsers").document(deviceId).set(data);

        db.collection("events").document(eventID)
                .collection("waitlist").document(deviceId).delete();
    }

    /**
     * US 01.05.03 / US 01.05.07
     * Decline an invitation — adds user to declinedUsers subcollection,
     * removes from waitlist subcollection.
     *
     * US 01.05.07 addition:
     * Also removes the eventID from the user's invitedEvents comma-String
     * so private event invites are properly cleaned up on decline.
     * This mirrors how User.cancelUser() removes events from invitedEvents.
     */
    private void declineInvite(String eventID) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", deviceId);

        // Existing decline logic — unchanged
        db.collection("events").document(eventID)
                .collection("declinedUsers").document(deviceId).set(data);

        db.collection("events").document(eventID)
                .collection("waitlist").document(deviceId).delete();

        // ─────────────────────────────────────────────────────────────────
        // US 01.05.07
        // Remove the eventID from invitedEvents so the private invite
        // disappears from this screen and the user is no longer considered
        // invited. Mirrors User.cancelUser() logic in User.java.
        // ─────────────────────────────────────────────────────────────────
        db.collection("users").document(deviceId).get()
                .addOnSuccessListener(snap -> {
                    if (!snap.exists()) return;
                    String invitedEvents = snap.getString("invitedEvents");
                    if (invitedEvents != null && invitedEvents.contains(eventID)) {
                        String updated = invitedEvents.replace(eventID + ",", "");
                        db.collection("users").document(deviceId)
                                .update("invitedEvents", updated);
                    }
                });

        // Remove from the local list so UI updates immediately
        for (int i = 0; i < invitations.size(); i++) {
            if (invitations.get(i).getEventId().equals(eventID)) {
                invitations.remove(i);
                adapter.notifyItemRemoved(i);
                break;
            }
        }
    }
}