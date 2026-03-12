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

public class AdminOrganizerAdapter extends RecyclerView.Adapter<AdminOrganizerAdapter.OrganizerViewHolder> {

    public static class OrganizerItem {
        public final String documentId;
        public final String name;
        public final String email;
        //PLACEHOLDER - FOR FUTURE IMPLEMENTATION
        public final boolean flagged;

        public OrganizerItem(String documentId, String name, String email, boolean flagged) {
            this.documentId = documentId;
            this.name = name;
            this.email = email;
            this.flagged = flagged;
        }
    }

    public interface OnDemoteClickListener {
        void onDemoteClick(OrganizerItem organizer);
    }

    private List<OrganizerItem> organizerList;
    private final OnDemoteClickListener demoteListener;

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

        public void bind(final OrganizerItem organizer, final OnDemoteClickListener listener) {
            nameText.setText(organizer.name);
            emailText.setText(organizer.email);
            //display flagged status, placeholder for now
            flaggedStatusText.setText(organizer.flagged ? "Flagged: Yes" : "Flagged: No");
            demoteButton.setOnClickListener(v -> listener.onDemoteClick(organizer));
        }
    }
}
