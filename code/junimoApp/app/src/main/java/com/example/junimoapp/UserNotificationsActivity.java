package com.example.junimoapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.junimoapp.utils.BaseActivity;
import com.example.junimoapp.utils.DeviceUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays all in-app notifications for the current user.
 *
 * User stories implemented here:
 *  - US 01.04.01: Shows notification when entrant was invited from waiting list
 *  - US 01.04.02: Shows notification when entrant was not chosen in lottery
 *  - US 01.05.06: Shows notification when entrant invited to private event waitlist
 *
 * Notifications are loaded from: users/{userId}/notifications
 * Ordered newest first. Each notification is marked as read after loading.
 *
 * Layout: activity_notifications.xml
 *
 * Treya
 */
public class UserNotificationsActivity extends BaseActivity {

    private NotifAdapter adapter;
    private final List<String> messages = new ArrayList<>();
    private FirebaseFirestore db;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        deviceId = DeviceUtils.getDeviceId(this);
        db = FirebaseFirestore.getInstance();

        RecyclerView rv = findViewById(R.id.notificationsRecycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotifAdapter(messages);
        rv.setAdapter(adapter);

        // Back button returns to user home
        findViewById(R.id.backToHomeNotif).setOnClickListener(v -> finish());

        loadNotifications();
    }

    // ─────────────────────────────────────────────────────────────────────
    // US 01.04.01 / US 01.04.02 / US 01.05.06
    // Loads all notifications from Firestore, newest first.
    // Marks each notification as read after loading so the user
    // knows they have been seen.
    // ─────────────────────────────────────────────────────────────────────
    private void loadNotifications() {
        db.collection("users").document(deviceId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snaps -> {
                    messages.clear();
                    for (QueryDocumentSnapshot doc : snaps) {
                        String msg = doc.getString("message");
                        if (msg != null) messages.add(msg);
                        // Mark as read now that the user has seen it
                        doc.getReference().update("read", true);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    // ── Simple RecyclerView adapter for notification messages ─────────────
    static class NotifAdapter extends RecyclerView.Adapter<NotifAdapter.VH> {

        private final List<String> items;

        NotifAdapter(List<String> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Uses a simple built-in Android list item layout — one TextView per row
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            h.text.setText(items.get(pos));
            h.text.setTextColor(android.graphics.Color.WHITE);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView text;
            VH(View v) {
                super(v);
                text = v.findViewById(android.R.id.text1);
            }
        }
    }
}
