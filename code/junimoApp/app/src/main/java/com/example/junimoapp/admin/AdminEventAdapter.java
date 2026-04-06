package com.example.junimoapp.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.junimoapp.R;
import java.util.List;

/**
 * AdminEventAdapter is a RecyclerView adapter used by AdminBrowseEventsActivity.
 *
 * It takes a list of EventItem objects (each representing one Firestore event document)
 * and binds them to the item_admin_event.xml layout so the admin can see and delete them.
 *
 * This follows the same pattern as AdminUserAdapter.
 *
 * User stories implemented:
 *   - US 03.01.01: As an administrator, I want to be able to remove events.
 *   - US 03.04.01: As an administrator, I want to be able to browse events.
 */
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.EventViewHolder> {

    /**
     * EventItem is a simple data container (a "model") for one row in the events list.
     * It holds just what the admin needs to see and act on:
     *   - documentId: the Firestore document ID, needed to delete the right document
     *   - title: the event's title, shown in bold
     *   - description: a short description of the event
     */
    public static class EventItem {
        public final String documentId; // Firestore document ID (used for deletion)
        public final String title;       // event title shown in the list
        public final String description; // event description shown below title

        /**
         * Constructor for EventItem.
         * @param documentId the Firestore document ID for this event
         * @param title      the event's title
         * @param description the event's description
         */
        public EventItem(String documentId, String title, String description) {
            this.documentId = documentId;
            this.title = title;
            this.description = description;
        }
    }

    /**
     * OnDeleteClickListener is a callback interface.
     * The Activity (AdminBrowseEventsActivity) implements this via a lambda,
     * so when the delete button is clicked in the adapter, the Activity handles
     * showing the confirmation dialog and calling Firestore.
     * This keeps deletion logic OUT of the adapter (separation of concerns).
     */
    public interface OnDeleteClickListener {
        void onDeleteClick(EventItem event);
    }

    /**
     * callback interface for tapping a row (not the delete button)
     */
    public interface OnRowClickListener {
        void onRowClick(EventItem event);
    }

    // the list of events to display - passed in from the Activity
    private final List<EventItem> eventList;

    // the callback that fires when the admin taps "Remove" on an event row
    private final OnDeleteClickListener deleteListener;

    private final OnRowClickListener rowClickListener;

    /**
     * Constructor for AdminEventAdapter.
     * @param eventList      the list of EventItem objects to display
     * @param deleteListener callback to handle delete button clicks (handled by Activity)
     * @param rowClickListener callback for row clicks
     */
    public AdminEventAdapter(List<EventItem> eventList, OnDeleteClickListener deleteListener, OnRowClickListener rowClickListener) {
        this.eventList = eventList;
        this.deleteListener = deleteListener;
        this.rowClickListener = rowClickListener;
    }

    /**
     * Called by RecyclerView when it needs a new row view.
     * Inflates item_admin_event.xml and wraps it in an EventViewHolder.
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate the XML layout for a single event row
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Called by RecyclerView to display data at a specific position.
     * Gets the EventItem at that position and tells the ViewHolder to bind it.
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventItem event = eventList.get(position);
        holder.bind(event, deleteListener, rowClickListener);
    }

    /**
     * Returns how many items are in the list.
     * RecyclerView uses this to know how many rows to draw.
     */
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * replaces displayed list with a new (filtered) list, refreshes UI
     * Called whenever search text changes
     * @param filteredList the new list to display
     */
    public void filterList(List<EventItem> filteredList) {
        this.eventList.clear();
        this.eventList.addAll(filteredList);
        notifyDataSetChanged();
    }


    /**
     * EventViewHolder holds references to the views inside one event row (item_admin_event.xml).
     * RecyclerView reuses ViewHolders as you scroll, so finding views by ID only
     * happens once per ViewHolder rather than on every scroll.
     */
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;    // shows the event title
        TextView descText;     // shows the event description
        Button deleteButton;   // red "Remove" button

        /**
         * Constructor: finds and stores references to views in item_admin_event.xml.
         * @param itemView the inflated row view
         */
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.adminEventTitle);
            descText = itemView.findViewById(R.id.adminEventDescription);
            deleteButton = itemView.findViewById(R.id.adminEventDeleteButton);
        }

        /**
         * Binds one EventItem's data to this row's views.
         * Also sets the delete button's click listener to fire the callback.
         * @param event    the EventItem whose data we're displaying
         * @param listener the callback to fire when delete is clicked
         */
        public void bind(final EventItem event, final OnDeleteClickListener listener, final OnRowClickListener rowClickListener) {
            titleText.setText(event.title);
            descText.setText(event.description);
            // when delete is clicked, pass this event back to the Activity to handle
            deleteButton.setOnClickListener(v -> listener.onDeleteClick(event));
            itemView.setOnClickListener(v -> rowClickListener.onRowClick(event));
        }
    }
}