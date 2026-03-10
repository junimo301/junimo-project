package com.example.junimoapp.admin;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.junimoapp.R;
import com.example.junimoapp.firebase.FirebaseManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

/**
 * AdminBrowseEventsActivity allows an administrator to view all events stored
 * in Firestore and permanently delete any of them.
 *
 * This Activity follows the same structure as AdminBrowseProfilesActivity
 *
 * Layout: activity_admin_browse_events.xml
 * Adapter: AdminEventAdapter.java
 *
 * User stories implemented:
 *   - US 03.01.01: As an administrator, I want to be able to remove events.
 *   - US 03.04.01: As an administrator, I want to be able to browse events.
 */
public class AdminBrowseEventsActivity extends AppCompatActivity {

    // tag used for Logcat filtering when debugging this Activity
    private static final String TAG = "AdminBrowseEvents";

    // the RecyclerView that displays the list of events
    private RecyclerView recyclerView;

    // the adapter that connects our eventList data to the RecyclerView rows
    private AdminEventAdapter adapter;

    // the in-memory list of events fetched from Firestore
    // the adapter reads from this list to draw each row
    private List<AdminEventAdapter.EventItem> eventList;

    // Firestore database reference - obtained via FirebaseManager singleton
    private FirebaseFirestore db;

    /**
     * Called when the Activity is first created.
     * Sets up the layout, Firestore connection, RecyclerView, and loads events.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // connect this Activity to its XML layout
        setContentView(R.layout.activity_admin_browse_events);

        // get the shared Firestore instance from FirebaseManager
        // using a singleton avoids creating multiple database connections
        db = FirebaseManager.getDB();

        // find the RecyclerView in the layout and set it to scroll vertically
        recyclerView = findViewById(R.id.adminEventsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // initialize the empty list that will hold events once loaded
        eventList = new ArrayList<>();

        // create the adapter, passing the list and a method reference for delete clicks
        // "this::onDeleteEventClicked" means: when delete is clicked, call our method below
        adapter = new AdminEventAdapter(eventList, this::onDeleteEventClicked);
        recyclerView.setAdapter(adapter);

        // fetch events from Firestore and populate the list
        loadEvents();
    }

    /**
     * Queries the Firestore "events" collection and loads all documents into eventList.
     *
     * For each document, we read the "title" and "description" fields.
     * If either field is missing or empty, we substitute a placeholder string
     * so the admin still sees a meaningful row instead of a blank one.
     *
     * After loading, we call adapter.notifyDataSetChanged() to tell the
     * RecyclerView to redraw all rows with the new data.
     */
    private void loadEvents() {
        db.collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // clear any old data before loading fresh results
                    eventList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        // the document ID is the unique key for this event in Firestore
                        // we need it later to delete the right document
                        String docId = doc.getId();

                        // read the event fields from Firestore
                        // these match the field names in the Event model class
                        String title = doc.getString("title");
                        String description = doc.getString("description");

                        // handle missing or empty fields gracefully
                        // so the admin sees something meaningful in every row
                        if (title == null || title.isEmpty()) title = "(no title)";
                        if (description == null || description.isEmpty()) description = "(no description)";

                        // add a new EventItem to our list with the data from this document
                        eventList.add(new AdminEventAdapter.EventItem(docId, title, description));
                    }

                    // tell the adapter the data has changed so it redraws the list
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // log the error for debugging in Logcat
                    Log.e(TAG, "Failed to load events", e);
                    // show a brief message to the admin so they know something went wrong
                    Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Called when the admin taps "Remove" on an event row.
     * Shows an AlertDialog asking the admin to confirm before permanently deleting.
     *
     * We always confirm before deleting because this action cannot be undone.
     *
     * @param event the EventItem that the admin wants to delete
     */
    private void onDeleteEventClicked(AdminEventAdapter.EventItem event) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Event")
                // include the event title in the message so the admin knows exactly what they're deleting
                .setMessage("Are you sure you want to remove \"" + event.title + "\"? (Permanent!)")
                // if confirmed, proceed to delete from Firestore
                .setPositiveButton("Remove", (dialog, which) -> deleteEventFromFirestore(event))
                // if cancelled, dismiss the dialog and do nothing
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Permanently deletes the given event document from the Firestore "events" collection.
     *
     * On success:
     *   - Shows a Toast confirmation
     *   - Removes the event from the local eventList
     *   - Notifies the adapter so the row disappears without needing a full reload
     *
     * On failure:
     *   - Logs the error
     *   - Shows a Toast error message to the admin
     *
     * @param event the EventItem to delete (uses its documentId to target the right Firestore doc)
     */
    private void deleteEventFromFirestore(AdminEventAdapter.EventItem event) {
        db.collection("events").document(event.documentId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event removed!", Toast.LENGTH_SHORT).show();

                    // find the position of this event in the local list
                    int position = eventList.indexOf(event);
                    if (position != -1) {
                        // remove from the local list so the UI stays in sync with Firestore
                        eventList.remove(position);
                        // tell the adapter exactly which row was removed for a smooth animation
                        adapter.notifyItemRemoved(position);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting event from database", e);
                    Toast.makeText(this, "Failed to remove event.", Toast.LENGTH_SHORT).show();
                });
    }
}