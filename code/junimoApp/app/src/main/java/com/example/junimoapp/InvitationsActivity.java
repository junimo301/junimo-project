package com.example.junimoapp;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.junimoapp.adapters.InvitationAdapter;
import com.example.junimoapp.models.InvitationItem;
import com.example.junimoapp.utils.DeviceUtils;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * user stories implemented:
 *  - US 01.05.02: Entrant wants to be able to accept an invite when chosen.
 *  - US 01.05.03: Entrant wants to be able to decline an invitation if they are chosen.
 */

/**
 * provides logic for invitations to an event, including accepting/declining an invite
 */

public class InvitationsActivity extends AppCompatActivity {

    private InvitationAdapter adapter;
    private final List<InvitationItem> invitations = new ArrayList<>();
    private String deviceId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitations);

        RecyclerView recyclerView = findViewById(R.id.invitationsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        deviceId = DeviceUtils.getDeviceId(this);
        db = FirebaseFirestore.getInstance();

        //adapter with listener for accept/decline
        adapter = new InvitationAdapter(invitations, new InvitationAdapter.InvitationListener() {
            @Override
            public void onAccept(String eventId) {
                acceptInvite(eventId);
            }

            @Override
            public void onDecline(String eventId) {
                declineInvite(eventId);
            }
        });

        recyclerView.setAdapter(adapter);

        loadInvitations();
    }

    private void loadInvitations() {
        db.collection("users").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Object invitationsObj = documentSnapshot.get("invitations");
                        if (invitationsObj instanceof List) {
                            List<?> eventIdsRaw = (List<?>) invitationsObj;
                            for (Object idObj : eventIdsRaw) {
                                if (idObj instanceof String) {
                                    String eventId = (String) idObj;
                                    db.collection("events").document(eventId).get()
                                            .addOnSuccessListener(eventSnap -> {
                                                if (eventSnap.exists()) {
                                                    String title = eventSnap.getString("title");
                                                    int position = invitations.size();
                                                    invitations.add(new InvitationItem(eventId, title));
                                                    adapter.notifyItemInserted(position);
                                                }
                                            });
                                }
                            }
                        }
                    }
                });
    }

    //accept invitation
    private void acceptInvite(String eventId) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", deviceId);

        db.collection("events").document(eventId)
                .collection("acceptedUsers").document(deviceId).set(data);

        db.collection("events").document(eventId)
                .collection("waitlist").document(deviceId).delete();
    }

    //decline invitation
    private void declineInvite(String eventId) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", deviceId);

        db.collection("events").document(eventId)
                .collection("declinedUsers").document(deviceId).set(data);

        db.collection("events").document(eventId)
                .collection("waitlist").document(deviceId).delete();
    }
}