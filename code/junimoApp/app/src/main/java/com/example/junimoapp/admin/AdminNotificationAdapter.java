package com.example.junimoapp.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.junimoapp.R;

import java.util.List;

/**
 * RecyclerView adapter for AdminNotificationLogActivity
 * US 03.08.01 - as an admin, I want to review notification logs
 */
public class AdminNotificationAdapter extends RecyclerView.Adapter<AdminNotificationAdapter.NotifViewHolder> {

    /**
     * NotifItem data container for one notification row
     */
    public static class NotifItem {
        public final String message;
        public final String organizerName;
        public final String timestamp;

        /**
         * NotifItem constructor
         * @param message notification message text
         * @param organizerName organizer label string
         * @param timestamp timestamp string
         */
        public NotifItem(String message, String organizerName, String timestamp) {
            this.message = message;
            this.organizerName = organizerName;
            this.timestamp = timestamp;
        }
    }

    private final List<NotifItem> notifList;

    /**
     * adpater constructor
     * @param notifList list of NotifItem objects to display
     */
    public AdminNotificationAdapter(List<NotifItem> notifList) {
        this.notifList = notifList;
    }

    /**
     * gives RecyclerView a new row view
     * Inflates item_admin_notification, wraps in NotifViewHolder
     * @param parent parent ViewGroup
     * @param viewType view type (unused)
     * @return new NotifViewHolder
     */
    @NonNull
    @Override
    public NotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_notification, parent, false);
        return new NotifViewHolder(view);
    }

    /**
     * Called by RecyclerView to display data
     * @param holder ViewHolder to bind data to
     * @param position pos in list
     */
    @Override
    public void onBindViewHolder(@NonNull NotifViewHolder holder, int position) {
        NotifItem item = notifList.get(position);
        holder.bind(item);
    }

    /**
     * returns total number of notifications in list
     * @return item count
     */
    @Override
    public int getItemCount() {
        return notifList.size();
    }

    /**
     * NotifViewHolder holds references to views inside one notification
     */
    public static class NotifViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView organizerText;
        TextView timestampText;

        /**
         * Constructs ViewHolder
         * @param itemView inflated row view
         */
        public NotifViewHolder (@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.adminNotifMessage);
            organizerText = itemView.findViewById(R.id.adminNotifOrganizer);
            timestampText = itemView.findViewById(R.id.adminNotifTimestamp);
        }

        /**
         * Binds NotifItem to row's views
         * @param item NotifItem to display
         */
        public void bind(final NotifItem item) {
            messageText.setText(item.message);
            organizerText.setText(item.organizerName);
            timestampText.setText(item.timestamp);
        }
    }
}













