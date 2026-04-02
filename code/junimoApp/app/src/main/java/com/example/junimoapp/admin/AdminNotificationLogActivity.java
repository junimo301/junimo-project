package com.example.junimoapp.admin;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.junimoapp.R;
import com.example.junimoapp.firebase.FirebaseManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * allows admin to review all notifications that've been sent
 *
 * No notifications get deleted.
 *
 * US 03.08.01 - As an admin, I want to review logs of notifications
 */
public class AdminNotificationLogActivity extends AppCompatActivity {
    private static final String TAG = "AdminNotifLog";
    private FirebaseFirestore db;
    private TextView backButton;
    private RecyclerView recyclerView;
    private AdminNotificationAdapter notifAdapter;
    private List<AdminNotificationAdapter.NotifItem> notifList;

    /**
     * Called when activity created
     * @param savedInstanceState previously saved state (unused)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notification_log);

        db = FirebaseManager.getDB();

        backButton = findViewById(R.id.backToHomeText);
        recyclerView = findViewById(R.id.adminNotifRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        notifList = new ArrayList<>();
        notifAdapter = new AdminNotificationAdapter(notifList);
        recyclerView.setAdapter(notifAdapter);

        backButton.setOnClickListener(v -> finish());

        loadAllNotifications();
    }

    /**
     * Loads all notifications from Firestore by scanning user docs for their notifications
     */
    private void loadAllNotifications() {
        notifList.clear();

        db.collection("users").get()
                .addOnSuccessListener(userSnaps -> {
                    final int[] pending = {userSnaps.size()};

                    if (pending[0] == 0) {
                        //no users at all
                        notifAdapter.notifyDataSetChanged();
                        return;
                    }

                    for (DocumentSnapshot userDoc : userSnaps) {
                        String userId = userDoc.getId();

                        db.collection("users")
                                .document(userId)
                                .collection("notifications")
                                .get()
                                .addOnSuccessListener(notifSnaps -> {
                                    for (QueryDocumentSnapshot notifDoc : notifSnaps) {
                                        String message = notifDoc.getString("message");
                                        String organizerName = notifDoc.getString("organizerName");

                                        Object ts = notifDoc.get("timestamp");
                                        String timestamp = ts != null ? ts.toString() : "";

                                        if (message == null) {
                                            pending[0]--;
                                            checkAllDone(pending[0]);
                                            continue;
                                        }

                                        if (organizerName == null || organizerName.isEmpty()) {
                                            organizerName = getString(R.string.sent_by) + "(unknown organizer)";
                                        } else {
                                            organizerName = getString(R.string.sent_by) + organizerName;
                                        }

                                        notifList.add(new AdminNotificationAdapter.NotifItem(
                                                message, organizerName, timestamp
                                        ));
                                    }

                                    pending[0]--;
                                    checkAllDone(pending[0]);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to load notifications for user " + userId, e);
                                    pending[0]--;
                                    checkAllDone(pending[0]);
                                });
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load users for notification log", e));
    }

    /**
     * Checks if all pending sub-collection fetches are complete. If so, refresshes RecyclerView
     * @param remaining number of sub-collection fetches still in progress
     */
    private void checkAllDone(int remaining) {
        if (remaining <= 0) {
            notifAdapter.notifyDataSetChanged();
        }
    }
}















