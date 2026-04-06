package com.example.junimoapp.Organizer;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.R;
import com.example.junimoapp.models.User;
import com.example.junimoapp.models.UserSession;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Organizer can send notifications to users
 * US 02.07.01: Sends notifications to all entrants on the waiting list
 * US 02.07.02: Sends notifications to all selected entrants
 * US 02.07.03: Sends a notification to all cancelled entrants
 * US 02.05.01: Sends a notification to chosen entrants to sign up for events
 * */
public class organizerNotifications extends AppCompatActivity {

    private static final String EVENTS_COLLECTION = "events";
    private static final String USERS_COLLECTION = "users";
    private static final String NOTIFICATIONS_COLLECTION = "notifications";
    private static final String WAITLIST_FIELD = "waitlist";
    private static final String LEGACY_WAITLIST_FIELD = "waitList";
    private static final String TITLE_FIELD = "title";

    private final FirebaseFirestore db;
    private final Map<Integer, EventOption> eventOptions = new HashMap<>();

    private RadioGroup eventGroup;
    private Spinner notificationTypeSpinner;
    private EditText messageInput;
    private Button sendButton;
    private Button cancelButton;
    private TextView backButton;

    public organizerNotifications() {
        this(null);
    }

    organizerNotifications(FirebaseFirestore db) {
        this.db = db;
    }

    /**
     * Callback for sending notifications
     * */
    public interface NotificationCallback {
        void onSuccess(int notifiedCount);

        void onFailure(@NonNull Exception exception);
    }

    /**
     * Internal callback for fetching recipient IDs
     * */
    private interface RecipientIdsCallback {
        void onSuccess(@NonNull List<String> recipientIds);

        void onFailure(@NonNull Exception exception);
    }

    /**
     * Represents an event option with its ID and title
     * */
    private static class EventOption {
        final String eventId;
        final String title;

        EventOption(String eventId, String title) {
            this.eventId = eventId;
            this.title = title;
        }
    }

    /**
     * Start activity
     * @param savedInstanceState saved instance state
     * */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_organizer);

        eventGroup = findViewById(R.id.notifications_event_group);
        notificationTypeSpinner = findViewById(R.id.notifications_type_spinner);
        messageInput = findViewById(R.id.notifications_message_input);
        sendButton = findViewById(R.id.notifications_send_button);
        cancelButton = findViewById(R.id.notifications_cancel_button);
        backButton = findViewById(R.id.notifications_back_button);

        setupNotificationTypeSpinner();
        loadOrganizerEvents();

        backButton.setOnClickListener(v -> finish());
        cancelButton.setOnClickListener(v -> finish());
        sendButton.setOnClickListener(v -> sendSelectedNotification());
    }

    /**
     * Types of notifications to send to users
     * Organizer selects which type of notification to send to users
     * */
    private void setupNotificationTypeSpinner() {
        List<String> types = new ArrayList<>();
        types.add("Entrants On Waiting List");
        types.add("Selected Entrants");
        types.add("Cancelled Entrants");
        types.add("Sign-Up To Lottery Winners");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_notification_spinner, types);
        adapter.setDropDownViewResource(R.layout.item_notification_spinner);
        notificationTypeSpinner.setAdapter(adapter);
    }

    /**
     * Loads all organizer's events
     * */
    private void loadOrganizerEvents() {
        User currentUser = UserSession.getCurrentUser();
        List<com.example.junimoapp.models.Event> cachedEvents = EventData.listOfEvents();
        if (!cachedEvents.isEmpty()) {
            populateEventOptions(cachedEvents, currentUser);
            if (eventGroup.getChildCount() > 0) {
                return;
            }
        }

        getFirestore().collection(EVENTS_COLLECTION).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventOptions.clear();
                    eventGroup.removeAllViews();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String organizerId = doc.getString("organizerID");
                        if (currentUser != null && (organizerId == null || !organizerId.equals(currentUser.getDeviceId()))) {
                            continue;
                        }

                        String eventId = safeValue(doc.getString("eventID"), doc.getId());
                        String title = safeValue(doc.getString(TITLE_FIELD), "Untitled Event");
                        addEventOption(eventId, title);
                    }

                    if (eventGroup.getChildCount() == 0) {
                        Toast.makeText(this, "No events found for this organizer", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show());
    }

    /**
     * Populates the event options list with the organizer's events
     * @param events list of events
     * @param currentUser current user
     * */
    private void populateEventOptions(@NonNull List<com.example.junimoapp.models.Event> events,
                                      @Nullable User currentUser) {
        eventOptions.clear();
        eventGroup.removeAllViews();

        for (com.example.junimoapp.models.Event event : events) {
            if (currentUser != null && !safeValue(event.getOrganizerID(), "").equals(currentUser.getDeviceId())) {
                continue;
            }
            addEventOption(
                    safeValue(event.getEventID(), "unknown-event"),
                    safeValue(event.getTitle(), "Untitled Event")
            );
        }
    }

    /**
     * Adds an event option to the event options list
     * @param eventId event ID
     * @param title event name
     * */
    private void addEventOption(@NonNull String eventId, @NonNull String title) {
        RadioButton radioButton = new RadioButton(this);
        radioButton.setId(View.generateViewId());
        radioButton.setText(title);
        radioButton.setTextColor(getColor(android.R.color.white));
        radioButton.setButtonTintList(ColorStateList.valueOf(getColor(android.R.color.white)));
        radioButton.setBackgroundResource(R.drawable.bg_rounded_dark);
        radioButton.setPadding(36, 28, 36, 28);

        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = 20;
        radioButton.setLayoutParams(params);

        eventOptions.put(radioButton.getId(), new EventOption(eventId, title));
        eventGroup.addView(radioButton);

        if (eventGroup.getCheckedRadioButtonId() == -1) {
            radioButton.setChecked(true);
        }
    }

    /**
     * Sends out the selected notification for a event to users
     * Checks for notification type and a selected event before sending
     * Prevents duplicate notifications by disabling button while sending
     * */
    private void sendSelectedNotification() {
        int selectedButtonId = eventGroup.getCheckedRadioButtonId();
        if (selectedButtonId == -1) {
            Toast.makeText(this, "Select an event first", Toast.LENGTH_SHORT).show();
            return;
        }

        EventOption selectedEvent = eventOptions.get(selectedButtonId);
        if (selectedEvent == null) {
            Toast.makeText(this, "Unable to read the selected event", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedType = String.valueOf(notificationTypeSpinner.getSelectedItem());
        String customMessage = messageInput.getText().toString().trim();

        sendButton.setEnabled(false);
        sendNotificationByType(selectedEvent.eventId, selectedEvent.title, selectedType, customMessage, new NotificationCallback() {
            @Override
            public void onSuccess(int notifiedCount) {
                sendButton.setEnabled(true);
                Toast.makeText(organizerNotifications.this, "Notification sent to " + notifiedCount + " users", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
                sendButton.setEnabled(true);
                Toast.makeText(organizerNotifications.this, "Failed to send notification", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sends notifications to users based on notification type
     * @param eventId event Id
     * @param eventTitle events title
     * @param notificationType the type of notification to send
     * @param customMessage optional custom message
     * @param callback result callback
     * */
    private void sendNotificationByType(@NonNull String eventId,
                                        @NonNull String eventTitle,
                                        @NonNull String notificationType,
                                        @Nullable String customMessage,
                                        @Nullable NotificationCallback callback) {
        String title = notificationType;
        String message = safeValue(customMessage, buildDefaultMessage(notificationType, eventTitle));

        if ("Entrants On Waiting List".equals(notificationType)) {
            sendNotificationToWaitingList(eventId, title, message, callback);
            return;
        }

        fetchRecipientIds(eventId, notificationType, new RecipientIdsCallback() {
            @Override
            public void onSuccess(@NonNull List<String> recipientIds) {
                if (recipientIds.isEmpty()) {
                    dispatchSuccess(0, callback);
                    return;
                }
                sendNotificationToUsers(recipientIds, eventId, eventTitle, title, message, notificationType, callback);
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
                dispatchFailure(exception, callback);
            }
        });
    }

    /**
     * Sends a default waitlist notification using the event title.
     *
     * @param eventId event document id
     * @param callback result callback
     */
    public void notifyWaitingList(@NonNull String eventId, @Nullable NotificationCallback callback) {
        sendNotificationToWaitingList(eventId, null, null, callback);
    }

    /**
     * Sends a notification to every user id stored in the event's waitlist field.
     *
     * @param eventId event document id
     * @param notificationTitle optional notification title, defaults to event title
     * @param notificationMessage optional custom message, defaults to a waitlist update message
     * @param callback result callback
     */
    public void sendNotificationToWaitingList(@NonNull String eventId,
                                              @Nullable String notificationTitle,
                                              @Nullable String notificationMessage,
                                              @Nullable NotificationCallback callback) {
        FirebaseFirestore firestore = getFirestore();
        firestore.collection(EVENTS_COLLECTION).document(eventId).get()
                .addOnCompleteListener(task -> handleEventLoaded(
                        task,
                        eventId,
                        notificationTitle,
                        notificationMessage,
                        callback
                ));
    }

    /**
     * Handles the loaded event data
     * Sends notifications to every user id stored in the event's waitlist
     * @param task Firebase task
     * @param eventId event ID
     * @param notificationTitle notification title
     * @param notificationMessage notification message
     * @param callback result callback
     */
    private void handleEventLoaded(@NonNull Task<DocumentSnapshot> task,
                                   @NonNull String eventId,
                                   @Nullable String notificationTitle,
                                   @Nullable String notificationMessage,
                                   @Nullable NotificationCallback callback) {
        if (!task.isSuccessful()) {
            dispatchFailure(task.getException(), callback);
            return;
        }

        DocumentSnapshot document = task.getResult();
        if (document == null || !document.exists()) {
            dispatchFailure(new IllegalArgumentException("Event not found for id: " + eventId), callback);
            return;
        }

        String eventTitle = safeValue(document.getString(TITLE_FIELD), "Event Update");
        List<String> waitlistedUserIds = parseWaitlist(resolveWaitlistValue(document));
        if (waitlistedUserIds.isEmpty()) {
            dispatchSuccess(0, callback);
            return;
        }

        String resolvedTitle = safeValue(notificationTitle, eventTitle);
        String resolvedMessage = safeValue(
                notificationMessage,
                "There is an update for " + eventTitle + ". Check the app for more details."
        );

        sendNotificationToUsers(
                waitlistedUserIds,
                eventId,
                eventTitle,
                resolvedTitle,
                resolvedMessage,
                "Entrants On Waiting List",
                callback
        );
    }

    /**
     * Get the users ID to send notifications
     * @param eventId event ID
     * @param notificationType notification type
     * @param callback result callback
     * */
    private void fetchRecipientIds(@NonNull String eventId,
                                   @NonNull String notificationType,
                                   @NonNull RecipientIdsCallback callback) {
        if ("Cancelled Entrants".equals(notificationType)) {
            getFirestore().collection(EVENTS_COLLECTION).document(eventId)
                    .collection("declinedUsers")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<String> ids = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            ids.add(doc.getId());
                        }
                        callback.onSuccess(ids);
                    })
                    .addOnFailureListener(callback::onFailure);
            return;
        }

        if ("Selected Entrants".equals(notificationType) || "Sign-Up To Lottery Winners".equals(notificationType)) {
            getFirestore().collection(USERS_COLLECTION)
                    .whereArrayContains("invitations", eventId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<String> ids = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            ids.add(doc.getId());
                        }
                        callback.onSuccess(ids);
                    })
                    .addOnFailureListener(callback::onFailure);
            return;
        }

        callback.onSuccess(new ArrayList<>());
    }

    /**
     * Sends notifications to users for the specific event
     * @param recipientIds list of user IDs
     * @param eventId event ID
     * @param eventTitle event title
     * @param notificationTitle notification title
     * @param notificationMessage notification message
     * @param notificationType notification type
     * @param callback result callback
     * */
    private void sendNotificationToUsers(@NonNull List<String> recipientIds,
                                         @NonNull String eventId,
                                         @NonNull String eventTitle,
                                         @NonNull String notificationTitle,
                                         @NonNull String notificationMessage,
                                         @NonNull String notificationType,
                                         @Nullable NotificationCallback callback) {
        WriteBatch batch = getFirestore().batch();
        Timestamp sentAt = Timestamp.now();

        for (String userId : recipientIds) {
            DocumentReference notificationRef = getFirestore().collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(NOTIFICATIONS_COLLECTION)
                    .document();

            batch.set(notificationRef, buildNotificationPayload(
                    eventId,
                    eventTitle,
                    notificationTitle,
                    notificationMessage,
                    notificationType,
                    sentAt
            ));
        }

        batch.commit()
                .addOnSuccessListener(unused -> dispatchSuccess(recipientIds.size(), callback))
                .addOnFailureListener(exception -> dispatchFailure(exception, callback));
    }

    /**
     * Creates notification object to be stored database
     * @param eventId event ID
     * @param eventTitle event title
     * @param notificationTitle notification title
     * @param notificationMessage notification message
     * @param notificationType notification type
     * */
    private Map<String, Object> buildNotificationPayload(@NonNull String eventId,
                                                         @NonNull String eventTitle,
                                                         @NonNull String notificationTitle,
                                                         @NonNull String notificationMessage,
                                                         @NonNull String notificationType,
                                                         @NonNull Timestamp sentAt) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("eventId", eventId);
        notification.put("eventTitle", eventTitle);
        notification.put("title", notificationTitle);
        notification.put("message", notificationMessage);
        notification.put("type", notificationType);
        notification.put("sentAt", sentAt);
        notification.put("read", false);
        return notification;
    }

    /**
     * Default notification messages to send out to users by notification type
     * @param eventTitle event title
     * @param notificationType notification type
     * */
    @NonNull
    private String buildDefaultMessage(@NonNull String notificationType, @NonNull String eventTitle) {
        if ("Cancelled Entrants".equals(notificationType)) {
            return "There is an update for cancelled entrants in " + eventTitle + ".";
        }
        if ("Selected Entrants".equals(notificationType)) {
            return "There is an update for selected entrants in " + eventTitle + ".";
        }
        if ("Sign-Up To Lottery Winners".equals(notificationType)) {
            return "Lottery winners for " + eventTitle + " have a new update.";
        }
        return "There is an update for entrants on the waiting list of " + eventTitle + ".";
    }

    /**
     * Parses the waitlist string into a list of user IDs
     * @param rawWaitlist waitlist string
     * @return list of user IDs
     * */
    @NonNull
    private List<String> parseWaitlist(@Nullable String rawWaitlist) {
        Set<String> userIds = new LinkedHashSet<>();
        if (rawWaitlist == null || rawWaitlist.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String[] splitIds = rawWaitlist.split(",");
        for (String splitId : splitIds) {
            if (splitId == null) {
                continue;
            }

            String cleanedId = splitId.trim();
            if (!cleanedId.isEmpty()) {
                userIds.add(cleanedId);
            }
        }

        return new ArrayList<>(userIds);
    }

    /**
     * Gets waitlist string from event document
     * @param document Firestore document for the event
     * @return raw waitlist string if exists, null if not
     * */
    @Nullable
    private String resolveWaitlistValue(@NonNull DocumentSnapshot document) {
        return resolveWaitlistValue(document.getData());
    }

    /**
     * Gets waitlist string from event data
     * @param eventData event data
     * @return raw waitlist string if exists, null if not
     * */
    @Nullable
    String resolveWaitlistValue(@Nullable Map<String, Object> eventData) {
        if (eventData == null) {
            return null;
        }

        String waitlistValue = readStringField(eventData, WAITLIST_FIELD);
        if (waitlistValue != null && !waitlistValue.trim().isEmpty()) {
            return waitlistValue;
        }

        return readStringField(eventData, LEGACY_WAITLIST_FIELD);
    }

    /**
     * Returns a safe value from a string
     * @param value value to check
     * @param fallback string to use if value is null or empty
     * @return the original value if not null or empty, fallback otherwise
     * */
    @NonNull
    private String safeValue(@Nullable String value, @NonNull String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    /**
     * Reports a successfull notifcation
     * @param notifiedCount number of notified users
     * @param callback result callback
     * */
    private void dispatchSuccess(int notifiedCount, @Nullable NotificationCallback callback) {
        if (callback != null) {
            callback.onSuccess(notifiedCount);
        }
    }

    /**
     * reports a failed notification
     * @param exception exception to report
     * @param callback result callback
     * */
    private void dispatchFailure(@Nullable Exception exception, @Nullable NotificationCallback callback) {
        if (callback != null) {
            callback.onFailure(exception != null ? exception : new RuntimeException("Notification send failed"));
        }
    }

    /**
     * returns Firestore instance
     * @return Firestore instance
     * */
    @NonNull
    private FirebaseFirestore getFirestore() {
        if (db != null) {
            return db;
        }
        return FirebaseFirestore.getInstance();
    }

    /**
     * Read string value from map for field name
     * @param eventData map with event data
     * @param fieldName key of field to read
     * @return value of field if exists, null otherwise
     * */
    @Nullable
    private String readStringField(@NonNull Map<String, Object> eventData, @NonNull String fieldName) {
        Object value = eventData.get(fieldName);
        if (value instanceof String) {
            return (String) value;
        }
        return null;
    }
}
