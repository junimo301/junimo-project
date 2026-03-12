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
 * RecyclerView Adapter for browsing organizers as an admin
 * Takes list of OrganizerItem objects, binds them to layout for display
 */
public class AdminOrganizerAdapter extends RecyclerView.Adapter<AdminOrganizerAdapter.OrganizerViewHolder> {

    /**
     * simple class for holding one organizer row
     */
    public static class OrganizerItem {
        public final String documentId;
        public final String name;
        public final String email;
        //PLACEHOLDER - FOR FUTURE IMPLEMENTATION
        public final boolean flagged;

        /**
         * Constructs a new OrganizerItem
         * @param documentId Firestore doc ID (user device ID)
         * @param name organizer's name
         * @param email organizer's email
         * @param flagged placeholder, whether or not the organizer has been flagged for policy violations
         */
        public OrganizerItem(String documentId, String name, String email, boolean flagged) {
            this.documentId = documentId;
            this.name = name;
            this.email = email;
            this.flagged = flagged;
        }
    }

    /**
     * interface for remove organizer status button clicks (demote button)
     */
    public interface OnDemoteClickListener {
        /**
         * called when demote button clicked
         * @param organizer OrganizerItem associated w/ clicked row
         */
        void onDemoteClick(OrganizerItem organizer);
    }

    private List<OrganizerItem> organizerList;
    private final OnDemoteClickListener demoteListener;

    /**
     * Constructs adapter
     * @param organizerList initial list of organizers to display
     * @param demoteListener listener for demote button
     */
    public AdminOrganizerAdapter(List<OrganizerItem> organizerList, OnDemoteClickListener demoteListener) {
        this.organizerList = organizerList;
        this.demoteListener = demoteListener;
    }

    @NonNull
    @Override
    public OrganizerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_organizer, parent, false);
        return new OrganizerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrganizerViewHolder holder, int position) {
        OrganizerItem organizer = organizerList.get(position);
        holder.bind(organizer, demoteListener);
    }

    @Override
    public int getItemCount() {
        return organizerList.size();
    }

    /**
     * updates list of organizers diaplayed by adapter based on search
     * @param filteredList new, filtered list of organizers
     */
    public void filterList(List<OrganizerItem> filteredList) {
        this.organizerList = filteredList;
        notifyDataSetChanged();
    }

    /**
     * VieHolder class for one organizer row, chaches references for efficiency
     */
    public static class OrganizerViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, emailText, flaggedStatusText;
        Button demoteButton;

        public OrganizerViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.adminOrganizerName);
            emailText = itemView.findViewById(R.id.adminOrganizerEmail);
            flaggedStatusText = itemView.findViewById(R.id.adminOrganizerFlaggedStatus);
            demoteButton = itemView.findViewById(R.id.adminOrganizerDemoteButton);
        }

        /**
         * binds data from OrganizerItem to views
         * @param organizer organizer data
         * @param listener listener for demote button
         */
        public void bind(final OrganizerItem organizer, final OnDemoteClickListener listener) {
            nameText.setText(organizer.name);
            emailText.setText(organizer.email);
            //display flagged status, placeholder for now
            flaggedStatusText.setText(organizer.flagged ? "Flagged: Yes" : "Flagged: No");
            demoteButton.setOnClickListener(v -> listener.onDemoteClick(organizer));
        }
    }
}
