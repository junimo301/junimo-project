package com.example.junimoapp.Organizer;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.junimoapp.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * US 02.01.03
 * Allows an organizer to invite specific entrants to a private event's
 * waiting list by searching via name, phone number, and/or email.
 *
 * How it works:
 *  1. Organizer types in the search box (name, email, or phone).
 *  2. Three Firestore prefix queries run (one per field) and results are merged.
 *  3. Organizer taps "Invite" next to a user.
 *  4. The user is added to the event's privateInvites subcollection
 *     and the eventId is added to the user's invitations list in Firestore.
 *
 * Layout: activity_private_invite.xml
 */
public class PrivateInviteActivity extends AppCompatActivity {

    private EditText searchField;
    private RecyclerView recyclerView;
    private UserResultAdapter adapter;
    private final List<UserResult> results = new ArrayList<>();

    private FirebaseFirestore db;

    // ─────────────────────────────────────────────────────────────────────
    // US 02.01.03
    // The eventId and eventTitle are passed in from CreateEvent via Intent
    // so we know which private event we are inviting people to.
    // ─────────────────────────────────────────────────────────────────────
    private String eventId;
    private String eventTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_invite);

        // Receive the event details from CreateEvent
        eventId    = getIntent().getStringExtra("eventId");
        eventTitle = getIntent().getStringExtra("eventTitle");

        db = FirebaseFirestore.getInstance();

        searchField  = findViewById(R.id.search_field);
        recyclerView = findViewById(R.id.results_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // ─────────────────────────────────────────────────────────────────
        // US 02.01.03
        // Pass the invite action into the adapter so each row's button
        // can trigger inviteUser() for that specific user.
        // ─────────────────────────────────────────────────────────────────
        adapter = new UserResultAdapter(results, userId -> inviteUser(userId));
        recyclerView.setAdapter(adapter);

        // ─────────────────────────────────────────────────────────────────
        // US 02.01.03
        // Search on every keystroke. No need to press a search button.
        // Blank query clears the results list.
        // ─────────────────────────────────────────────────────────────────
        searchField.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                searchUsers(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.back_button_private).setOnClickListener(v -> finish());
    }

    /**
     * US 02.01.03
     * Queries Firestore users collection by name, email, and phone.
     * Firestore does not support OR queries across different fields,
     * so we run three separate prefix queries and merge the results,
     * skipping any user already present in the list (no duplicates).
     *
     * Prefix search trick: Firestore range query between the query string
     * and the same string with \uf8ff appended matches all strings that
     * start with the query.
     */
    private void searchUsers(String query) {
        // Clear results when the search box is empty
        if (query.equals("")) {
            results.clear();
            adapter.notifyDataSetChanged();
            return;
        }

        results.clear();
        adapter.notifyDataSetChanged();

        // \uf8ff acts as an upper bound
        // so the range query matches all strings starting with 'query'
        String queryEnd = query + "\uf8ff";

        // Search by name
        db.collection("users")
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", queryEnd)
                .get()
                .addOnSuccessListener(snaps -> mergeResults(snaps.getDocuments()));

        // Search by email
        db.collection("users")
                .whereGreaterThanOrEqualTo("email", query)
                .whereLessThanOrEqualTo("email", queryEnd)
                .get()
                .addOnSuccessListener(snaps -> mergeResults(snaps.getDocuments()));

        // Search by phone
        db.collection("users")
                .whereGreaterThanOrEqualTo("phone", query)
                .whereLessThanOrEqualTo("phone", queryEnd)
                .get()
                .addOnSuccessListener(snaps -> mergeResults(snaps.getDocuments()));
    }

    /**
     * US 02.01.03
     * Adds incoming Firestore documents to the results list,
     * skipping any user whose deviceId is already present
     * to avoid showing the same person multiple times.
     */
    private void mergeResults(List<DocumentSnapshot> docs) {
        for (DocumentSnapshot doc : docs) {
            String uid = doc.getString("deviceId");
            if (uid == null) continue;

            // Check for duplicates before adding
            boolean alreadyPresent = false;
            for (UserResult r : results) {
                if (r.deviceId.equals(uid)) {
                    alreadyPresent = true;
                    break;
                }
            }

            if (!alreadyPresent) {
                results.add(new UserResult(
                        uid,
                        doc.getString("name"),
                        doc.getString("email"),
                        doc.getString("phone")
                ));
                adapter.notifyItemInserted(results.size() - 1);
            }
        }
    }

    /**
     * US 02.01.03
     * Invites a specific user to this private event by:
     *  1. Writing a pending record to the event's privateInvites subcollection.
     *  2. Adding the eventId to the user's invitations array in Firestore
     *     (only if not already present, to avoid duplicates).
     *
     * The user will then see this invite in InvitationsActivity
     * and can accept or decline it (US 01.05.07).
     */
    private void inviteUser(String userId) {
        // 1. Add to event's privateInvites subcollection with pending status
        Map<String, Object> invite = new HashMap<>();
        invite.put("userId", userId);
        invite.put("status", "pending"); // updated to "accepted"/"declined" by the entrant
        db.collection("events").document(eventId)
                .collection("privateInvites").document(userId)
                .set(invite);

        // 2. Add eventId to the user's invitations list (no duplicates)
        db.collection("users").document(userId).get()
                .addOnSuccessListener(snap -> {
                    List<String> invitations = new ArrayList<>();

                    // Carry over any existing invitations
                    Object existing = snap.get("invitations");
                    if (existing instanceof List) {
                        for (Object o : (List<?>) existing) {
                            if (o instanceof String) invitations.add((String) o);
                        }
                    }

                    // Only add if not already invited to this event
                    if (!invitations.contains(eventId)) {
                        invitations.add(eventId);
                        db.collection("users").document(userId)
                                .update("invitations", invitations);
                    }
                });

        Toast.makeText(this, "Invitation sent!", Toast.LENGTH_SHORT).show();
    }

    // Data holder for a single search result row

    /**
     * US 02.01.03
     * Holds the display data for one user shown in the search results list.
     */
    static class UserResult {
        String deviceId;
        String name;
        String email;
        String phone;

        UserResult(String deviceId, String name, String email, String phone) {
            this.deviceId = deviceId;
            this.name     = name;
            this.email    = email;
            this.phone    = phone;
        }
    }

    // RecyclerView adapter

    /**
     * US 02.01.03
     * Displays search results in a list. Each row shows the user's name
     * and contact info, with an Invite button the organizer taps to send
     * the invitation.
     */
    static class UserResultAdapter extends RecyclerView.Adapter<UserResultAdapter.VH> {

        /**
         * Callback interface — called when the organizer taps Invite
         * on a specific user row.
         */
        interface InviteListener {
            void onInvite(String userId);
        }

        private final List<UserResult> items;
        private final InviteListener listener;

        UserResultAdapter(List<UserResult> items, InviteListener listener) {
            this.items    = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_result, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            UserResult r = items.get(position);
            h.name.setText(r.name != null ? r.name : "—");

            // Show email and phone on the same subtitle line
            String sub = "";
            if (r.email != null) sub += r.email;
            if (r.phone != null) sub += (sub.equals("") ? "" : "  •  ") + r.phone;
            h.sub.setText(sub);

            h.inviteBtn.setOnClickListener(v -> listener.onInvite(r.deviceId));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView name;
            TextView sub;
            Button inviteBtn;

            VH(View v) {
                super(v);
                name      = v.findViewById(R.id.user_name);
                sub       = v.findViewById(R.id.user_sub);
                inviteBtn = v.findViewById(R.id.invite_button);
            }
        }
    }
}