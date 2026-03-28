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
import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.models.User;
import com.example.junimoapp.utils.NotificationHelper;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * US 02.01.03
 * Allows an organizer to invite specific entrants to a private event's
 * waiting list by searching via name, phone number, and/or email.
 *
 * How it works:
 *  1. Organizer types in the search box (name, email, or phone).
 *  2. Three Firestore prefix queries run and results are merged.
 *  3. Organizer taps "Invite" next to a user.
 *  4. The user's invitedEvents string is updated via FirebaseManager.updateUser()
 *     — consistent with how User.inviteUser() works throughout the app.
 *  5. A private invite notification is sent via NotificationHelper (US 01.05.06).
 *
 * Layout: activity_private_invite.xml
 */
public class PrivateInviteActivity extends AppCompatActivity {

    private EditText searchField;
    private RecyclerView recyclerView;
    private UserResultAdapter adapter;
    private final List<UserResult> results = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseManager firebase;

    // ─────────────────────────────────────────────────────────────────────
    // US 02.01.03
    // eventId and eventTitle passed in from CreateEvent via Intent
    // ─────────────────────────────────────────────────────────────────────
    private String eventId;
    private String eventTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_invite);

        eventId    = getIntent().getStringExtra("eventId");
        eventTitle = getIntent().getStringExtra("eventTitle");

        db       = FirebaseFirestore.getInstance();
        firebase = new FirebaseManager();

        searchField  = findViewById(R.id.search_field);
        recyclerView = findViewById(R.id.results_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UserResultAdapter(results, userId -> inviteUser(userId));
        recyclerView.setAdapter(adapter);

        // ─────────────────────────────────────────────────────────────────
        // US 02.01.03
        // Search on every keystroke — blank query clears results list
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
     * Prefix-searches Firestore users by name, email, and phone.
     * Runs three separate queries (Firestore has no OR across fields)
     * and merges results, skipping duplicates.
     */
    private void searchUsers(String query) {
        if (query.equals("")) {
            results.clear();
            adapter.notifyDataSetChanged();
            return;
        }
        results.clear();
        adapter.notifyDataSetChanged();

        // \uf8ff upper-bounds the range so all strings starting with query match
        String queryEnd = query + "\uf8ff";

        db.collection("users")
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", queryEnd)
                .get().addOnSuccessListener(s -> mergeResults(s.getDocuments()));

        db.collection("users")
                .whereGreaterThanOrEqualTo("email", query)
                .whereLessThanOrEqualTo("email", queryEnd)
                .get().addOnSuccessListener(s -> mergeResults(s.getDocuments()));

        db.collection("users")
                .whereGreaterThanOrEqualTo("phone", query)
                .whereLessThanOrEqualTo("phone", queryEnd)
                .get().addOnSuccessListener(s -> mergeResults(s.getDocuments()));
    }

    /**
     * US 02.01.03
     * Adds incoming docs to the results list, skipping duplicates by deviceId.
     */
    private void mergeResults(List<DocumentSnapshot> docs) {
        for (DocumentSnapshot doc : docs) {
            String uid = doc.getString("deviceId");
            if (uid == null) continue;
            boolean alreadyPresent = false;
            for (UserResult r : results) {
                if (r.deviceId.equals(uid)) { alreadyPresent = true; break; }
            }
            if (!alreadyPresent) {
                results.add(new UserResult(uid,
                        doc.getString("name"),
                        doc.getString("email"),
                        doc.getString("phone")));
                adapter.notifyItemInserted(results.size() - 1);
            }
        }
    }

    /**
     * US 02.01.03 / US 01.05.06
     * Invites a user to this private event:
     *  1. Reads their current invitedEvents string from Firestore.
     *  2. Appends this eventId (same format as User.inviteUser()).
     *  3. Saves back using FirebaseManager.updateUser() as requested in review.
     *  4. Sends a private invite notification via NotificationHelper (US 01.05.06)
     *     which respects the user's opt-out preference (US 01.04.03).
     */
    private void inviteUser(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(snap -> {
                    if (!snap.exists()) return;

                    // ─────────────────────────────────────────────────────
                    // US 02.01.03
                    // invitedEvents is a comma-separated String on the User doc
                    // (per User.java — same format used by User.inviteUser()).
                    // We use firebase.updateUser() as requested in code review.
                    // ─────────────────────────────────────────────────────
                    String current = snap.getString("invitedEvents");
                    if (current == null) current = "";

                    if (!current.contains(eventId)) {
                        String updated = current + eventId + ",";

                        // User shell just provides the deviceId for the doc path
                        User shell = new User(userId, "", "", "", "", "", "");
                        firebase.updateUser(
                                db.collection("users"),
                                shell,
                                "invitedEvents",
                                updated
                        );

                        // ─────────────────────────────────────────────────
                        // US 01.05.06
                        // Send notification — respects opt-out (US 01.04.03)
                        // ─────────────────────────────────────────────────
                        NotificationHelper.notifyPrivateInvite(userId, eventId, eventTitle);
                        Toast.makeText(this, "Invitation sent!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "User already invited", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ── Data holder for one search result row ─────────────────────────────
    static class UserResult {
        String deviceId, name, email, phone;
        UserResult(String d, String n, String e, String p) {
            deviceId = d; name = n; email = e; phone = p;
        }
    }

    // ── RecyclerView adapter ──────────────────────────────────────────────
    static class UserResultAdapter extends RecyclerView.Adapter<UserResultAdapter.VH> {
        interface InviteListener { void onInvite(String userId); }
        private final List<UserResult> items;
        private final InviteListener listener;
        UserResultAdapter(List<UserResult> items, InviteListener l) {
            this.items = items; this.listener = l;
        }
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_result, parent, false);
            return new VH(v);
        }
        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            UserResult r = items.get(pos);
            h.name.setText(r.name != null ? r.name : "—");
            String sub = r.email != null ? r.email : "";
            if (r.phone != null) sub += (sub.equals("") ? "" : "  •  ") + r.phone;
            h.sub.setText(sub);
            h.inviteBtn.setOnClickListener(v -> listener.onInvite(r.deviceId));
        }
        @Override public int getItemCount() { return items.size(); }
        static class VH extends RecyclerView.ViewHolder {
            TextView name, sub; Button inviteBtn;
            VH(View v) {
                super(v);
                name = v.findViewById(R.id.user_name);
                sub = v.findViewById(R.id.user_sub);
                inviteBtn = v.findViewById(R.id.invite_button);
            }
        }
    }
}