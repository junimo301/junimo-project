package com.example.junimoapp.utils;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for sending in-app notifications to entrants via Firestore.
 *
 * User stories implemented here:
 *  - US 01.04.01: Notify entrant when selected by lottery
 *  - US 01.04.02: Notify entrant when NOT selected by lottery
 *  - US 01.04.03: Respect the user's opt-out preference before sending
 *  - US 01.05.06: Notify entrant when invited to a private event waitlist
 *
 * How it works:
 *  - Notifications stored at: users/{userId}/notifications/{autoId}
 *  - Each document has: message, eventID, type, read, timestamp
 *  - Before writing, we read notificationsEnabled from the user's Firestore doc.
 *    This is a boolean field added to User and saved by User.setNotificationsEnabled()
 *    via FirebaseManager.updateUser(). If false, nothing is written.
 *  - We use a Map for the notification document because it is a lightweight
 *    subcollection record — it has no corresponding model class in the app.
 */
public class NotificationHelper {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ─────────────────────────────────────────────────────────────────────
    // US 01.04.01
    // Call this after the lottery runs and a user has been selected.
    // Called from Entrants.java inside the lottery button click listener.
    // ─────────────────────────────────────────────────────────────────────
    public static void notifyInvited(String userId, String eventID, String eventTitle) {
        sendIfEnabled(userId, eventID, "invited",
                "You've been invited to join: " + eventTitle);
    }

    // ─────────────────────────────────────────────────────────────────────
    // US 01.04.02
    // Call this after the lottery runs and a user was NOT selected.
    // Called from Entrants.java inside the lottery button click listener.
    // ─────────────────────────────────────────────────────────────────────
    public static void notifyNotChosen(String userId, String eventID, String eventTitle) {
        sendIfEnabled(userId, eventID, "not_chosen",
                "Unfortunately you were not selected for: " + eventTitle);
    }

    // ─────────────────────────────────────────────────────────────────────
    // US 01.05.06
    // Call this when an organizer invites a user to a private event waitlist.
    // Called from PrivateInviteActivity.inviteUser().
    // ─────────────────────────────────────────────────────────────────────
    public static void notifyPrivateInvite(String userId, String eventID, String eventTitle) {
        sendIfEnabled(userId, eventID, "private_invite",
                "You've been invited to the waiting list for private event: " + eventTitle);
    }

    // ─────────────────────────────────────────────────────────────────────
    // US 01.04.03
    // Reads notificationsEnabled from the target user's Firestore document.
    // This is called with the TARGET user's ID (not the current user),
    // so we cannot use UserSession here — a Firestore read is necessary.
    // If the field is missing we default to true (notifications on).
    // The notification document uses a Map because it is a subcollection
    // record with no corresponding model class.
    // ─────────────────────────────────────────────────────────────────────
    private static void sendIfEnabled(String userId, String eventID,
                                      String type, String message) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) return;

                    // US 01.04.03 — check opt-out preference
                    Boolean enabled = snapshot.getBoolean("notificationsEnabled");
                    if (enabled != null && !enabled) return; // user opted out

                    // Write notification document to subcollection
                    Map<String, Object> notif = new HashMap<>();
                    notif.put("message", message);
                    notif.put("eventID", eventID);
                    notif.put("type", type);
                    notif.put("read", false);
                    notif.put("timestamp", System.currentTimeMillis());

                    db.collection("users").document(userId)
                            .collection("notifications")
                            .add(notif);
                });
    }
}
