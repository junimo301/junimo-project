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
 * Ayema
 * */
public class organizerNotifications extends AppCompatActivity {

    private static final String EVENTS_COLLECTION = "events";
    private static final String USERS_COLLECTION = "users";
    private static final String NOTIFICATIONS_COLLECTION = "notifications";
    private static final String WAITLIST_FIELD = "waitlist";
    private static final String LEGACY_WAITLIST_FIELD = "waitList";
    private static final String INVITED_USERS_FIELD = "invitedUsers";
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

    public interface NotificationCallback {
        void onSuccess(int notifiedCount);

        void onFailure(@NonNull Exception exception);
    }

    private interface RecipientIdsCallback {
        void onSuccess(@NonNull List<String> recipientIds);

        void onFailure(@NonNull Exception exception);
    }

    private interface EnabledRecipientsCallback {
        void onSuccess(@NonNull List<String> enabledRecipientIds);

        void onFailure(@NonNull Exception exception);
    }

    private static class EventOption {
        final String documentId;
        final String eventId;
        final String title;

        EventOption(String documentId, String eventId, String title) {
            this.documentId = documentId;
            this.eventId = eventId;
            this.title = title;
        }
    }

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

                        String documentId = doc.getId();
                        String eventId = safeValue(doc.getString("eventID"), documentId);
                        String title = safeValue(doc.getString(TITLE_FIELD), "Untitled Event");
                        addEventOption(documentId, eventId, title);
                    }

                    if (eventGroup.getChildCount() == 0) {
                        Toast.makeText(this, "No events found for this organizer", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show());
    }

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
                    safeValue(event.getEventID(), "unknown-event"),
                    safeValue(event.getTitle(), "Untitled Event")
            );
        }
    }

    private void addEventOption(@NonNull String documentId,
                                @NonNull String eventId,
                                @NonNull String title) {
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

        eventOptions.put(radioButton.getId(), new EventOption(documentId, eventId, title));
        eventGroup.addView(radioButton);

        if (eventGroup.getCheckedRadioButtonId() == -1) {
            radioButton.setChecked(true);
        }
    }

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
        sendNotificationByType(selectedEvent.documentId, selectedEvent.eventId, selectedEvent.title, selectedType, customMessage, new NotificationCallback() {
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

    private void sendNotificationByType(@NonNull String eventDocumentId,
                                        @NonNull String eventId,
                                        @NonNull String eventTitle,
                                        @NonNull String notificationType,
                                        @Nullable String customMessage,
                                        @Nullable NotificationCallback callback) {
        String title = notificationType;
        String message = safeValue(customMessage, buildDefaultMessage(notificationType, eventTitle));

        fetchRecipientIds(eventDocumentId, eventId, notificationType, new RecipientIdsCallback() {
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
        sendNotificationToWaitingList(eventId, eventId, null, null, callback);
    }

    /**
     * Sends a notification to every user id stored in the event's waitlist field.
     *
     * @param eventId event document id
     * @param notificationTitle optional notification title, defaults to event title
     * @param notificationMessage optional custom message, defaults to a waitlist update message
     * @param callback result callback
     */
    public void sendNotificationToWaitingList(@NonNull String eventDocumentId,
                                              @NonNull String eventId,
                                              @Nullable String notificationTitle,
                                              @Nullable String notificationMessage,
                                              @Nullable NotificationCallback callback) {
        FirebaseFirestore firestore = getFirestore();
        firestore.collection(EVENTS_COLLECTION).document(eventDocumentId).get()
                .addOnCompleteListener(task -> handleEventLoaded(
                        task,
                        eventId,
                        notificationTitle,
                        notificationMessage,
                        callback
                ));
    }

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
        String resolvedTitle = safeValue(notificationTitle, eventTitle);
        String resolvedMessage = safeValue(
                notificationMessage,
                "There is an update for " + eventTitle + ". Check the app for more details."
        );

        Set<String> recipientIds = new LinkedHashSet<>(parseWaitlist(resolveWaitlistValue(document)));
        fetchUsersByEventMembership(eventId, "waitlistedEvents", new RecipientIdsCallback() {
            @Override
            public void onSuccess(@NonNull List<String> membershipIds) {
                recipientIds.addAll(membershipIds);
                fetchUsersByEventMembership(eventId, "invitedEvents", new RecipientIdsCallback() {
                    @Override
                    public void onSuccess(@NonNull List<String> invitedMembershipIds) {
                        recipientIds.addAll(invitedMembershipIds);

                        if (recipientIds.isEmpty()) {
                            fetchAllEventRecipientIds(document.getId(), eventId, new RecipientIdsCallback() {
                                @Override
                                public void onSuccess(@NonNull List<String> allRecipientIds) {
                                    if (allRecipientIds.isEmpty()) {
                                        dispatchSuccess(0, callback);
                                        return;
                                    }

                                    sendNotificationToUsers(
                                            allRecipientIds,
                                            eventId,
                                            eventTitle,
                                            resolvedTitle,
                                            resolvedMessage,
                                            "Entrants On Waiting List",
                                            callback
                                    );
                                }

                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    dispatchFailure(exception, callback);
                                }
                            });
                            return;
                        }

                        sendNotificationToUsers(
                                new ArrayList<>(recipientIds),
                                eventId,
                                eventTitle,
                                resolvedTitle,
                                resolvedMessage,
                                "Entrants On Waiting List",
                                callback
                        );
                    }

                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        dispatchFailure(exception, callback);
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
                dispatchFailure(exception, callback);
            }
        });
    }

    private void fetchRecipientIds(@NonNull String eventDocumentId,
                                   @NonNull String eventId,
                                   @NonNull String notificationType,
                                   @NonNull RecipientIdsCallback callback) {
        if ("Cancelled Entrants".equals(notificationType)) {
            getFirestore().collection(EVENTS_COLLECTION).document(eventDocumentId)
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

        if ("Selected Entrants".equals(notificationType)) {
            getFirestore().collection(EVENTS_COLLECTION).document(eventDocumentId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Set<String> recipientIds = new LinkedHashSet<>(
                                parseWaitlist(readStringField(documentSnapshot.getData(), INVITED_USERS_FIELD))
                        );
                        fetchUsersByEventMembership(eventId, "invitedEvents", new RecipientIdsCallback() {
                            @Override
                            public void onSuccess(@NonNull List<String> membershipIds) {
                                recipientIds.addAll(membershipIds);
                                callback.onSuccess(new ArrayList<>(recipientIds));
                            }

                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                callback.onFailure(exception);
                            }
                        });
                    })
                    .addOnFailureListener(callback::onFailure);
            return;
        }

        if ("Sign-Up To Lottery Winners".equals(notificationType)) {
            getFirestore().collection(EVENTS_COLLECTION).document(eventDocumentId)
                    .collection("acceptedUsers")
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

        if ("Entrants On Waiting List".equals(notificationType)) {
            fetchUsersByEventMembership(eventId, "waitlistedEvents", new RecipientIdsCallback() {
                @Override
                public void onSuccess(@NonNull List<String> membershipIds) {
                    fetchUsersByEventMembership(eventId, "invitedEvents", new RecipientIdsCallback() {
                        @Override
                        public void onSuccess(@NonNull List<String> invitedMembershipIds) {
                            Set<String> recipientIds = new LinkedHashSet<>(membershipIds);
                            recipientIds.addAll(invitedMembershipIds);
                            callback.onSuccess(new ArrayList<>(recipientIds));
                        }

                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            callback.onFailure(exception);
                        }
                    });
                }

                @Override
                public void onFailure(@NonNull Exception exception) {
                    callback.onFailure(exception);
                }
            });
            return;
        }

        callback.onSuccess(new ArrayList<>());
    }

    private void fetchUsersByEventMembership(@NonNull String eventId,
                                             @NonNull String membershipField,
                                             @NonNull RecipientIdsCallback callback) {
        getFirestore().collection(USERS_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> recipientIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String userId = safeValue(doc.getString("deviceId"), doc.getId());
                        String membershipValue = doc.getString(membershipField);
                        if (containsListValue(membershipValue, eventId) && !recipientIds.contains(userId)) {
                            recipientIds.add(userId);
                        }
                    }
                    callback.onSuccess(recipientIds);
                })
                .addOnFailureListener(callback::onFailure);
    }

    private void fetchAllEventRecipientIds(@NonNull String eventDocumentId,
                                           @NonNull String eventId,
                                           @NonNull RecipientIdsCallback callback) {
        Set<String> recipientIds = new LinkedHashSet<>();

        getFirestore().collection(EVENTS_COLLECTION).document(eventDocumentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    recipientIds.addAll(parseWaitlist(resolveWaitlistValue(documentSnapshot)));
                    recipientIds.addAll(parseWaitlist(readStringField(documentSnapshot.getData(), INVITED_USERS_FIELD)));

                    getFirestore().collection(EVENTS_COLLECTION).document(eventDocumentId)
                            .collection("acceptedUsers")
                            .get()
                            .addOnSuccessListener(acceptedSnapshots -> {
                                for (QueryDocumentSnapshot doc : acceptedSnapshots) {
                                    recipientIds.add(doc.getId());
                                }

                                getFirestore().collection(EVENTS_COLLECTION).document(eventDocumentId)
                                        .collection("declinedUsers")
                                        .get()
                                        .addOnSuccessListener(declinedSnapshots -> {
                                            for (QueryDocumentSnapshot doc : declinedSnapshots) {
                                                recipientIds.add(doc.getId());
                                            }

                                            getFirestore().collection(USERS_COLLECTION)
                                                    .get()
                                                    .addOnSuccessListener(userSnapshots -> {
                                                        for (QueryDocumentSnapshot doc : userSnapshots) {
                                                            String userId = safeValue(doc.getString("deviceId"), doc.getId());
                                                            if (containsListValue(doc.getString("waitlistedEvents"), eventId)
                                                                    || containsListValue(doc.getString("invitedEvents"), eventId)
                                                                    || containsListValue(doc.getString("cancelledEvents"), eventId)) {
                                                                recipientIds.add(userId);
                                                            }
                                                        }
                                                        callback.onSuccess(new ArrayList<>(recipientIds));
                                                    })
                                                    .addOnFailureListener(callback::onFailure);
                                        })
                                        .addOnFailureListener(callback::onFailure);
                            })
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    private boolean containsListValue(@Nullable String rawValue, @NonNull String expectedValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return false;
        }

        String[] values = rawValue.split(",");
        for (String value : values) {
            if (expectedValue.equals(value != null ? value.trim() : null)) {
                return true;
            }
        }
        return false;
    }

    private void sendNotificationToUsers(@NonNull List<String> recipientIds,
                                         @NonNull String eventId,
                                         @NonNull String eventTitle,
                                         @NonNull String notificationTitle,
                                         @NonNull String notificationMessage,
                                         @NonNull String notificationType,
                                         @Nullable NotificationCallback callback) {
        filterEnabledRecipients(recipientIds, new EnabledRecipientsCallback() {
            @Override
            public void onSuccess(@NonNull List<String> enabledRecipientIds) {
                if (enabledRecipientIds.isEmpty()) {
                    dispatchSuccess(0, callback);
                    return;
                }

                WriteBatch batch = getFirestore().batch();
                Timestamp sentAt = Timestamp.now();
                String organizerName = resolveOrganizerName();

                for (String userId : enabledRecipientIds) {
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
                            organizerName,
                            sentAt
                    ));
                }

                batch.commit()
                        .addOnSuccessListener(unused -> dispatchSuccess(enabledRecipientIds.size(), callback))
                        .addOnFailureListener(exception -> dispatchFailure(exception, callback));
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
                dispatchFailure(exception, callback);
            }
        });
    }

    private void filterEnabledRecipients(@NonNull List<String> recipientIds,
                                         @NonNull EnabledRecipientsCallback callback) {
        List<String> uniqueIds = new ArrayList<>(new LinkedHashSet<>(recipientIds));
        if (uniqueIds.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        List<String> enabledIds = new ArrayList<>();
        final int[] pending = {uniqueIds.size()};
        final boolean[] failed = {false};

        for (String userId : uniqueIds) {
            getFirestore().collection(USERS_COLLECTION).document(userId).get()
                    .addOnSuccessListener(snapshot -> {
                        if (failed[0]) {
                            return;
                        }

                        if (snapshot.exists()) {
                            Boolean enabled = snapshot.getBoolean("notificationsEnabled");
                            if (enabled == null || enabled) {
                                enabledIds.add(userId);
                            }
                        }

                        pending[0]--;
                        if (pending[0] == 0) {
                            callback.onSuccess(enabledIds);
                        }
                    })
                    .addOnFailureListener(exception -> {
                        if (failed[0]) {
                            return;
                        }
                        failed[0] = true;
                        callback.onFailure(exception);
                    });
        }
    }

    private Map<String, Object> buildNotificationPayload(@NonNull String eventId,
                                                         @NonNull String eventTitle,
                                                         @NonNull String notificationTitle,
                                                         @NonNull String notificationMessage,
                                                         @NonNull String notificationType,
                                                         @Nullable String organizerName,
                                                         @NonNull Timestamp sentAt) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("eventID", eventId);
        notification.put("eventId", eventId);
        notification.put("eventTitle", eventTitle);
        notification.put("title", notificationTitle);
        notification.put("message", notificationMessage);
        notification.put("type", notificationType);
        notification.put("organizerName", organizerName);
        notification.put("timestamp", sentAt.toDate().getTime());
        notification.put("sentAt", sentAt);
        notification.put("read", false);
        return notification;
    }

    @Nullable
    private String resolveOrganizerName() {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        return currentUser.getName();
    }

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

    @Nullable
    private String resolveWaitlistValue(@NonNull DocumentSnapshot document) {
        return resolveWaitlistValue(document.getData());
    }

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

    @NonNull
    private String safeValue(@Nullable String value, @NonNull String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private void dispatchSuccess(int notifiedCount, @Nullable NotificationCallback callback) {
        if (callback != null) {
            callback.onSuccess(notifiedCount);
        }
    }

    private void dispatchFailure(@Nullable Exception exception, @Nullable NotificationCallback callback) {
        if (callback != null) {
            callback.onFailure(exception != null ? exception : new RuntimeException("Notification send failed"));
        }
    }

    @NonNull
    private FirebaseFirestore getFirestore() {
        if (db != null) {
            return db;
        }
        return FirebaseFirestore.getInstance();
    }

    @Nullable
    private String readStringField(@NonNull Map<String, Object> eventData, @NonNull String fieldName) {
        Object value = eventData.get(fieldName);
        if (value instanceof String) {
            return (String) value;
        }
        return null;
    }
}
