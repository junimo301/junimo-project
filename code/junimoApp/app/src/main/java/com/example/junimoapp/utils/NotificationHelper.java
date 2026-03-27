package com.example.junimoapp.utils;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for sending in-app notifications to entrants via Firestore.
 *
 * User stories implemented here:
 *  - US 01.04.01: Entrant receives a notification when invited from a waiting list
 *  - US 01.04.02: Entrant receives a notification when not chosen in the lottery
 *  - US 01.04.03: Entrant can opt out of receiving notifications
 *
 * How it works:
 *  - Notifications are stored in Firestore at: users/{userId}/notifications/{autoId}
 *  - Each notification document has: message, eventId, type, read, timestamp
 *  - Before writing, we check the user's notificationsEnabled flag (US 01.04.03)
 *    so opted-out users never receive any notifications
 *  - The NotificationsActivity reads these documents and displays them to the user
 */
public class NotificationHelper {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ─────────────────────────────────────────────────────────────────────
    // US 01.04.01
    // Call this after the lottery runs and a user has been selected.
    // Sends a notification telling the entrant they received an invite.
    // ─────────────────────────────────────────────────────────────────────
    public static void notifyInvited(String userId, String eventId, String eventTitle) {
        sendIfEnabled(userId, eventId, "invited",
                "You've been invited to join: " + eventTitle);
    }

    // ─────────────────────────────────────────────────────────────────────
    // US 01.04.02
    // Call this after the lottery runs and a user was NOT selected.
    // Sends a notification telling the entrant they were not chosen.
    // ─────────────────────────────────────────────────────────────────────
    public static void notifyNotChosen(String userId, String eventId, String eventTitle) {
        sendIfEnabled(userId, eventId, "not_chosen",
                "Unfortunately you were not selected for: " + eventTitle);
    }

    // ─────────────────────────────────────────────────────────────────────
    // US 01.05.06
    // Call this when an organizer invites a user to a private event waitlist.
    // Sends a notification telling the entrant about the private invite.
    // ─────────────────────────────────────────────────────────────────────
    public static void notifyPrivateInvite(String userId, String eventId, String eventTitle) {
        sendIfEnabled(userId, eventId, "private_invite",
                "You've been invited to the waiting list for private event: " + eventTitle);
    }

    // ─────────────────────────────────────────────────────────────────────
    // US 01.04.03
    // Checks the user's notificationsEnabled field in Firestore before
    // writing any notification. If the user has opted out (notificationsEnabled
    // is false), we return immediately and nothing is written.
    // If the field is missing we default to true (notifications on).
    // ─────────────────────────────────────────────────────────────────────
    private static void sendIfEnabled(String userId, String eventId,
                                      String type, String message) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) return;

                    // US 01.04.03 — respect the opt-out preference
                    Boolean enabled = snapshot.getBoolean("notificationsEnabled");
                    if (enabled != null && !enabled) return; // user opted out, do nothing

                    // Build the notification document
                    Map<String, Object> notif = new HashMap<>();
                    notif.put("message", message);
                    notif.put("eventId", eventId);
                    notif.put("type", type);       // "invited", "not_chosen", "private_invite"
                    notif.put("read", false);       // marked true when user views it
                    notif.put("timestamp", System.currentTimeMillis());

                    // Write to the user's notifications subcollection
                    db.collection("users").document(userId)
                            .collection("notifications")
                            .add(notif);
                });
    }
}
