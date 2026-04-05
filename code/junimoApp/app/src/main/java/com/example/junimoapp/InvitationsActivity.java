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
 * How invitations are loaded:
 *  - loadInvitations() reads from the "invitations" List field (Anica's implementation)
 *  - loadPrivateInvitations() reads from the "invitedEvents" comma-String field
 *    (written by PrivateInviteActivity when an organizer invites someone)
 *  - Both load into the same RecyclerView list
 */
public class InvitationsActivity extends AppCompatActivity {

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

        // Use UserSession — consistent with rest of app
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

        loadInvitations();
        loadPrivateInvitations();
    }

    /**
     * US 01.05.02 / US 01.05.03
     * Loads regular lottery invitations from the "invitations" List field.
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
                                    loadEventIntoList((String) idObj);
                                }
                            }
                        }
                    }
                });
    }

    /**
     * US 01.05.07
     * Loads private event invitations from the "invitedEvents" comma-String field.
     * Skips any eventID already loaded from the regular invitations list.
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
     * Fetches event title from Firestore and adds it to the invitations list.
     * Shared by both loadInvitations() and loadPrivateInvitations().
     */
    private void loadEventIntoList(String eventID) {
        db.collection("events").document(eventID).get()
                .addOnSuccessListener(eventSnap -> {
                    if (eventSnap.exists()) {
                        String title = eventSnap.getString("title");

                        // US 01.05.07 — label private event invites
                        Boolean isPrivate = eventSnap.getBoolean("private");
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
     * Accept an invitation — adds user to acceptedUsers, removes from waitlist,
     * cleans up invitedEvents string, and removes row from UI immediately.
     */
    private void acceptInvite(String eventId) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", deviceId);

        // Add to acceptedUsers subcollection
        db.collection("events").document(eventId)
                .collection("acceptedUsers").document(deviceId).set(data);

        // Remove from waitlist subcollection
        db.collection("events").document(eventId)
                .collection("waitlist").document(deviceId).delete();

        // ─────────────────────────────────────────────────────────────────
        // US 01.05.02
        // Remove eventId from invitedEvents string so it no longer shows
        // in the invitations list — same cleanup done in declineInvite().
        // ─────────────────────────────────────────────────────────────────
        db.collection("users").document(deviceId).get()
                .addOnSuccessListener(snap -> {
                    if (!snap.exists()) return;
                    String invitedEvents = snap.getString("invitedEvents");
                    if (invitedEvents != null && invitedEvents.contains(eventId)) {
                        String updated = invitedEvents.replace(eventId + ",", "");
                        db.collection("users").document(deviceId)
                                .update("invitedEvents", updated);
                    }
                });

        // Remove from local list so UI updates immediately
        removeFromList(eventId);
    }

    /**
     * US 01.05.03 / US 01.05.07
     * Decline an invitation — adds user to declinedUsers, removes from waitlist,
     * cleans up invitedEvents string, and removes row from UI immediately.
     */
    private void declineInvite(String eventID) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", deviceId);

        // Add to declinedUsers subcollection
        db.collection("events").document(eventID)
                .collection("declinedUsers").document(deviceId).set(data);

        // Remove from waitlist subcollection
        db.collection("events").document(eventID)
                .collection("waitlist").document(deviceId).delete();

        // ─────────────────────────────────────────────────────────────────
        // US 01.05.07
        // Remove the eventID from invitedEvents so the private invite
        // disappears from this screen. Mirrors User.cancelUser() in User.java.
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

        // Remove from local list so UI updates immediately
        removeFromList(eventID);
    }

    /**
     * Removes an invitation row from the local list and notifies the adapter.
     * Used by both acceptInvite() and declineInvite() to update the UI instantly.
     */
    private void removeFromList(String eventId) {
        for (int i = 0; i < invitations.size(); i++) {
            if (invitations.get(i).getEventId().equals(eventId)) {
                invitations.remove(i);
                adapter.notifyItemRemoved(i);
                break;
            }
        }
    }
}